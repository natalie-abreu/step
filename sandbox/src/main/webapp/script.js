// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

function initMap() {
  var map = new google.maps.Map(document.getElementById('map'), {
    zoom: 4,
    center: {lat: 42.1761001,lng: -87.797331}
  });

    start = {lat: 42.1761001,lng: -87.797331}
    end = {lat: 42.1764114,lng: -87.8059424}

    let geocoder = new google.maps.Geocoder();

   var marker = new google.maps.Marker({
    position: start,
    map: map,
    title: 'Start',
    draggable: true
    });

   var marker = new google.maps.Marker({
    position: end,
    map: map,
    title: 'End',
    draggable: true
    });

   
  var directionsService = new google.maps.DirectionsService;
  var directionsRenderer = new google.maps.DirectionsRenderer({
    draggable: true,
    map: map,
    panel: document.getElementById('right-panel')
  });
  var elevator = new google.maps.ElevationService;

  directionsRenderer.addListener('directions_changed', function() {
    computeTotalDistance(directionsRenderer.getDirections());
    place_ids = []
    waypoints = directionsRenderer.getDirections().geocoded_waypoints
    for (pt of waypoints) {
        place_ids.push(pt["place_id"])
    }

    let path = [];
    


    // make this work later
    function getPath(place_ids) {
        for (id of place_ids) {
            geocoder.geocode( { 'placeId': id}, function(results, status) {
                if (status == 'OK') {
                    console.log(results[0].geometry.location)
                    res = results[0].geometry.location
                    console.log(res.lat(), res.lng())
                    console.log(res.toUrlValue())
                    path.push(res);
                    console.log("intermediate path: " + path)    
                    displayPathElevation(path, elevator, map);
                } else {
                    alert('Geocode was not successful for the following reason: ' + status);
                }
            });
          
        }
    }
    getPath(place_ids);
    console.log("final path: " + path)
  });


  displayRoute('42.176498, -87.798125', '42.176135, -87.808397', directionsService,
      directionsRenderer);
  
  let path = [
      {lat: 42.176498, lng: -87.798125},  // Mt. Whitney
      {lat: 42.176135, lng: -87.808397}];  // Badwater, Death Valley
      

  // Draw the path, using the Visualization API and the Elevation service.
  displayPathElevation(path, elevator, map);

}

function displayRoute(origin, destination, service, display) {
  service.route({
    origin: origin,
    destination: destination,
    // waypoints: [{location: '42.176498, -87.798125'}, {location: '42.176135, -87.808397'}],
    travelMode: 'DRIVING',
    avoidTolls: true
  }, function(response, status) {
    if (status === 'OK') {
      display.setDirections(response);
    } else {
      alert('Could not display directions due to: ' + status);
    }
  });
}

function computeTotalDistance(result) {
  var total = 0;
  var myroute = result.routes[0];
  for (var i = 0; i < myroute.legs.length; i++) {
    total += myroute.legs[i].distance.value;
  }
  total = total / 1000;
  document.getElementById('total').innerHTML = total + ' km';
}

// Load the Visualization API and the columnchart package.
google.load('visualization', '1', {packages: ['columnchart']});

function displayPathElevation(path, elevator, map) {
/*   // Display a polyline of the elevation path.
  new google.maps.Polyline({
    path: path,
    strokeColor: '#0000CC',
    strokeOpacity: 0.4,
    map: map
  });
   */
  // Create a PathElevationRequest object using this array.
  // Ask for 256 samples along that path.
  // Initiate the path request.
  elevator.getElevationAlongPath({
    'path': path,
    'samples': 256
  }, plotElevation);
}

// Takes an array of ElevationResult objects, draws the path on the map
// and plots the elevation profile on a Visualization API ColumnChart.
function plotElevation(elevations, status) {
  var chartDiv = document.getElementById('elevation_chart');
  if (status !== 'OK') {
    // Show the error code inside the chartDiv.
    chartDiv.innerHTML = 'Cannot show elevation: request failed because ' +
        status;
    return;
  }
  // Create a new chart in the elevation_chart DIV.
  var chart = new google.visualization.ColumnChart(chartDiv);

  // Extract the data from which to populate the chart.
  // Because the samples are equidistant, the 'Sample'
  // column here does double duty as distance along the
  // X axis.
  var data = new google.visualization.DataTable();
  data.addColumn('string', 'Sample');
  data.addColumn('number', 'Elevation');
  for (var i = 0; i < elevations.length; i++) {
    data.addRow(['', elevations[i].elevation]);
  }

  // Draw the chart using the data within its DIV.
  chart.draw(data, {
    height: 150,
    legend: 'none',
    titleY: 'Elevation (m)'
  });
}