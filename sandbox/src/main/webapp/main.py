import cred
import json
import urllib.request, urllib.parse


def getElevation(lat, long):
    url = "https://maps.googleapis.com/maps/api/elevation/json?locations="\
        +str(lat)+","+str(long)+"&key="+cred.API_KEY

    with urllib.request.urlopen(url) as response:
        elevation = response.read().decode('utf-8')

    elevation = json.loads(elevation)

    return elevation


def getElevationOnPath(lat1, long1, lat2, long2):
    url = "https://maps.googleapis.com/maps/api/elevation/json?path="+\
        str(lat1)+","+str(long1)+"|"+str(lat2)+","+str(long2)+"&samples=100"+"&key="+cred.API_KEY
    print(url)
    with urllib.request.urlopen(url) as response:
        results = response.read().decode('utf-8')
    results = json.loads(results)

    elevationArray = []
    for resultset in results['results']:
        elevationArray.append(resultset['elevation'])

    return elevationArray


def getChart(chartData, chartDataScaling="-500,5000", chartType="lc",chartLabel="Elevation in Meters",chartSize="500x160",chartColor="orange", **chart_args):
    chart_args.update({
      'cht': chartType,
      'chs': chartSize,
      'chl': chartLabel,
      'chco': chartColor,
      'chds': chartDataScaling,
      'chxt': 'x,y',
      'chxr': '1,'+str(min(chartData))+','+str(max(chartData))
    })
    dataString = 't:' + ','.join(str(x) for x in chartData)
    chart_args['chd'] = dataString.strip(',')

    chartUrl = 'https://chart.apis.google.com/chart?' + urllib.parse.urlencode(chart_args)

    print(chartUrl)


print(getElevation(42.176498, -87.798125))
print(getElevation(42.176135, -87.808397))
print(getElevationOnPath(42.176498, -87.798125, 42.176135, -87.808397))

sample = getElevationOnPath(36.578581,-118.291994, 36.23998,-116.83171)
print(max(sample), min(sample))

getChart(chartData=getElevationOnPath(36.578581,-118.291994, 36.23998,-116.83171))

getChart(chartData=getElevationOnPath(42.176498, -87.798125, 42.176135, -87.808397))
