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

/**
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
      ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}


// js for templated slideshows
let slideIndex = 0;
let show_title = 'b';
let imgs = []
for (i = 1; i <=5; i++) {
        imgs.push("/images/"+show_title+i+".jpg")
}

function renderSlides(show) {
    imgs = [];
    show_title = show;
    for (i = 1; i <=5; i++) {
        imgs.push("/images/"+show+i+".jpg")
    }
    let w = '300px';
    let h = '400px';
    if (show == 'p') {
        [w, h] = [h, w];
    }
    const slideshowArea = document.getElementById("slideshow-container-container");
    slideshowArea.style.display = "flex";
    const container = document.getElementById('slideshow-container');
    container.style.display = "block";
    container.style.width = w;
    container.style.height = h;
    container.innerHTML = `<div class='slides' style='display:block'> 
    <img id='slide-img' src=${imgs[0]} style='width:${w}; height:${h}; object-fit: cover;'>`
}

function changeSlides(dir) {
    if (dir == 0) {
        slideIndex = 0;
    }
    else if (slideIndex == 0 && dir == -1) {
        slideIndex = imgs.length-1;
    }
    else if (slideIndex == imgs.length-1 && dir == 1) {
        slideIndex = 0;
    }
    else {
        slideIndex+=dir;
    }
    const slideImage = document.getElementById('slide-img');
    slideImage.src = imgs[slideIndex];

}

const options =["Baking", "Places", "Me and Other People"];
let showIndex = 0;
function changeShow(dir) {
    if (dir == 0) {
        showIndex = 0;
    }
    else if (showIndex == 0 && dir == -1) {
        showIndex = options.length-1;
    }
    else if (showIndex == options.length-1 && dir == 1) {
        showIndex = 0;
    }
    else {
        showIndex+=dir;
    }
    show_title = options[showIndex][0].toLowerCase();
    const slidesTitle = document.getElementById('slides-title');
    slidesTitle.innerHTML = options[showIndex];
    renderSlides(show_title, 0);
}

function toggleProjectOn(id) {
    descriptions = [`Through a student-org I am in called Code the Change, I worked on a web dev project featuring an interactive journal 
            to aid students with college application process as part of the PLUS ME Project, using React, Express, Node.js, and Postgres.`,
            `Through another student-org I am in, CAIS++, I worked on a group project to create a generalized classifier for genomic data.`,
            `This was my project for Google CSSI last summer that used the Google Maps API to find restaurants in the user's area based on user food preferences.`,
            `I worked in a research lab at USC where I studied ML and RL techniques with the goal of modeling human-inspired multirobot coordination.`]
    
    const ind = id.split("-")[1]-1;
    const img = id.split("-")[0]+"-img";

    // don't have an image for research square
    if (img != "research-img") {
        const background = document.getElementById(img);
        background.style.display = "none";
    }

    const square = document.getElementById(id);
    square.innerHTML = descriptions[ind];
    square.style.fontSize = "18px";
    square.style.lineHeight = "18px";
    square.style.paddingTop = "80px";
    square.style.height = "220px";
    square.style.background = "rgb(64, 78, 77, .6)";

}

function toggleProjectOff(id) {
    titles = ["PlusMe Project", "gsec", "Todo Restaurants", "Research Lab"];

    const ind = id.split("-")[1]-1;
    const img = id.split("-")[0]+"-img";
    
    // don't have an image for research square
    if (img != "research-img") {
        const background = document.getElementById(img);
        background.style.display = "block";
    }
    
    const square = document.getElementById(id);
    square.innerHTML=titles[ind];
    square.style.fontSize = "30px";
    square.style.lineHeight = "300px";
    square.style.paddingTop = "0px";
    square.style.height = "300px";
    square.style.background = "rgb(64, 78, 77, .25)";
}

let page_num = 1;
async function getComments(pageInc=0, numComments=0) {
    await checkLoginStatus();
    numComments = restoreNumComments(numComments);
    page_num+=pageInc;
    if (page_num == 0) page_num=1;
    const response = await fetch(`/data?max=${numComments}&page=${page_num}`);
    // will catch case when page is out of bounds
    let result;
    try {
        result = await response.json();
    } catch(e) {
        page_num-=1;
        console.log(e);
        return -1;
    }
    let board = document.getElementById("comments-board");
    setCommentBoardSize(numComments);
    board.innerText = '';
    for (msg of result["comments"]) {
        board.appendChild(createComment(result["user"], msg));
    }
    return 0;
}

function createComment(user, msg) {
    const comment = document.createElement('div');
    comment.className = "comment";
    comment.id = msg.id;

    comment.appendChild(createCommentInitial(msg));
    comment.appendChild(createCommentMessage(msg));
    if (user && user["userId"] == msg["user_id"]) {
        comment.appendChild(createCommentDeleteButton(msg));

        comment.addEventListener("mouseover", ()=> {
        const id = comment.id+"-delete-btn";
        const x = document.getElementById(id);
        x.style.display = "block";
        })

        comment.addEventListener("mouseout", ()=> {
        const id = comment.id+"-delete-btn";
        const x = document.getElementById(id);
        x.style.display = "none";
        })
    }

    

    return comment;
}

function createCommentDeleteButton(msg) {
    const x = document.createElement('div');
    x.className = "comment-delete-btn";
    x.id = msg.id+"-delete-btn";
    x.innerHTML = "x&nbsp;&nbsp;&nbsp;";
    x.addEventListener("click", ()=>{
        deleteSingleComment(msg.id);
    });
    return x;
}

function createCommentMessage(msg) {
    const message = document.createElement('div');
    message.className = "comment-message";
    message.innerText = msg.message;
    return message;
}

function createCommentInitial(msg) {
    const initial = document.createElement('div');
    initial.className = "comment-initial";
    if (msg.name == "") msg.name = "?";
    initial.innerText = msg.name[0].toUpperCase();
    initial.id = msg.id + "-initial";

    initial.appendChild(createCommentPopup(msg));
    initial.onmouseover = function() { showCommentInfo(initial.id); };
    initial.onmouseout = function() { hideCommentInfo(initial.id); };

    return initial;
}

function createCommentPopup(msg) {
    let msg_date = new Date(msg.timestamp).toDateString();
    const popup = document.createElement('div');
    popup.className = "comment-popup";
    popup.id = msg.id + "-popup";
    
    popup.appendChild(createCommentPopupName(msg));
    popup.innerHTML += `<p id=${msg.id}-popup-text-date class="comment-popup-text" style="display:none">${msg_date}</p>
    <div class="popup-triangle"></div>`;
    
    return popup;
}

function createCommentPopupName(msg) {
    const popupTextName = document.createElement('p');
    popupTextName.className = "comment-popup-text";
    popupTextName.id = `${msg.id}-popup-text-name`;
    const name = msg.name.split("@")[0];
    popupTextName.innerText = name;
    return popupTextName;
}

async function clearComments() {
    page_num = 0;
    const request = new Request('/delete-data?id=-1', {method: 'DELETE'});
    await fetch(request);
    getComments();
}

async function deleteSingleComment(id) {
    const request = new Request(`/delete-data?id=${id}`, {method: 'DELETE'});
    await fetch(request);
    let result = await getComments();
    if (result == -1) {
        getComments();
    }
}

function restoreNumComments(numComments) {
    // prevent resetting of dropdown selection on refresh/submit
    let maxSelection = document.getElementById("max-selection");
    numComments = getMaxFromStorage(numComments);
    maxSelection.value = numComments;
    return numComments;
}

function getMaxFromStorage(numComments) {
    // use default 0 to indicate that user has not selected a # of comments
    if (numComments == 0) {
        if (!sessionStorage.numComments) {
            // if nothing in session storage, show 5 comments
            numComments = 5;
            sessionStorage.numComments = numComments;
        }
        else numComments = sessionStorage.numComments;
    }
    else {
        sessionStorage.numComments = numComments;
    }
    return numComments;
}

function setCommentBoardSize(numComments) {
    const board = document.getElementById("comments-board");
    if (numComments == 5) board.style.minHeight = "30%";
    else if (numComments == 10) board.style.minHeight = "59%";
    else board.style.minHeight = "118%";
}

function showCommentInfo(id) {
    id = id.split("-")[0]+"-popup";
    let popup = document.getElementById(id);
    const popupName = document.getElementById(id+'-text-name');
    const popupDate = document.getElementById(id+'-text-date');
    popup.style.display = "block";
    toggleCommentInfo(0, popup, popupName, popupDate, true);
}  

function toggleCommentInfo(time, popup, popupName, popupDate, name) {
    // alternate info between comment author and date
    setTimeout(()=> {
        if (name) {
            popupName.style.display = "block";
            popupDate.style.display = "none";
        }
        else {
            popupName.style.display = "none";
            popupDate.style.display = "block";
        }
        if (popup.style.display == "block") {
            toggleCommentInfo(4000, popup, popupName, popupDate, !name);
        }
    }, time);
}

function hideCommentInfo(id) {
    id = id.split("-")[0]+"-popup";
    const popup = document.getElementById(id);
    popup.style.display = "none";
}


async function checkLoginStatus() {
    const request = new Request('/login-status', {method: 'GET'});
    const response = await fetch(request);

    const json = await response.json();
    const commentForm = document.getElementById("comments-form");
    const loginBtn = document.getElementById("login-button");
    if (json["loggedIn"] == "false") {
        commentForm.style.display = "none";
        loginBtn.innerText = "Login";
    }
    else {
        commentForm.style.display = "block";
        loginBtn.innerText = "Logout";
    }
    
    loginBtn.addEventListener("click", () => {
        // window.location = `../${json["url"]}`;
        window.location = json["url"];
    });
}

let geocoder;
let map;
let panorama;
let sv;

/** Creates a map and adds it to the page. */
function createMap(center) {
  geocoder = new google.maps.Geocoder();

  sv = new google.maps.StreetViewService();
  panorama = new google.maps.StreetViewPanorama(document.getElementById('street-view'));

  if (center == "usc") createUSCMap();
  else createHPMap();
}

function createUSCMap() {
  const usc = {lat: 34.0223519, lng: -118.2873057};

  map = new google.maps.Map(
      document.getElementById('map'),
      {center: usc, zoom: 16});
  sv.getPanorama({location: usc, radius: 50}, processSVData);

  const bakedBear = {lat: 34.025022485125334, lng: -118.28533725561974};
  const tcc = {lat: 34.0203071, lng:-118.2860911};
  const tommyTrojan = {lat: 34.0203229, lng: -118.2854093};
  const sal = {lat: 34.01945, lng: -118.289145};
  const fertitta = {lat: 34.0189079, lng: -118.2825777};

  addMarker("Baked Bear", bakedBear, "Really good ice cream sandwiches");
  addMarker("Campus Center", tcc, "Where to get food & (more importantly) coffee");
  addMarker("Tommy Trojan", tommyTrojan, "Tommy Trojan, of course");
  addMarker("SAL", sal, "Where CS kids spend hours debugging");
  addMarker("Fertitta", fertitta, "Fancy building for the business kids");
}

function createHPMap() {
    const hp = {lat: 42.1767167, lng: -87.7960146};

    map = new google.maps.Map(
      document.getElementById('map'),
      {center: hp, zoom: 13});
    sv.getPanorama({location: hp, radius: 50}, processSVData);

    const hphs = {lat: 42.1922954, lng: -87.8009056};
    const lfg = {lat: 42.1849674, lng: -87.7980549};
    const rosewood = {lat: 42.1670046, lng: -87.7685842};
    const homeAloneHouse = {lat: 42.1097311, lng: -87.7336786};
    const bobolink = {lat: 42.1762734, lng: -87.8030299};

    addMarker("Highland Park High School", hphs, "Where I went to high school");
    addMarker("That Little French Guy", lfg, "Cafe with really good eclairs");
    addMarker("Rosewood Beach", rosewood, "Rosewood Beach");
    addMarker("Bob-O-Link Rd", bobolink, "Broke my wrist here");
}

function addMarker(title, loc, content) {
    const marker = new google.maps.Marker({position: loc, map: map, title: title});
    const infowindow = new google.maps.InfoWindow({
        content: content
    });  
    marker.addListener('click', function() {
        console.log(marker.getPosition());
        map.setZoom(20);
        map.setCenter(marker.getPosition());
        infowindow.open(map, marker);
        sv.getPanorama({location: marker.getPosition(), radius: 50}, processSVData);
    });
    map.addListener('zoom_changed', function() {
      infowindow.close();
    })
}

function processSVData(data, status) {
  if (status === 'OK') {
    var marker = new google.maps.Marker({
      position: data.location.latLng,
      map: map,
      title: data.location.description
    });

    panorama.setPano(data.location.pano);
    panorama.setPov({
      heading: 270,
      pitch: 0
    });
    panorama.setVisible(true);

    marker.addListener('click', function() {
      var markerPanoID = data.location.pano;
      // Set the Pano to use the passed panoID.
      panorama.setPano(markerPanoID);
      panorama.setPov({
        heading: 270,
        pitch: 0
      });
      panorama.setVisible(true);
    });
    marker.setMap(null);
  } else {
    console.error('Street View data not found for this location.');
  }
}
