package com.company;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;

public class OpenWeatherMapAPI extends APITools{

    // ArrayLists
    // Values are added in order like such for both -> 0: description, 1: temp, 2: temp_min, 3: temp_max, 4: rain amount in mm
    private ArrayList<Object> cwList = new ArrayList<>();
    private ArrayList<Object> fwList = new ArrayList<>();
    private ArrayList<Object> hwList = new ArrayList<>();

    // Variables
    // This is the base url needed for the current weather notice the "weather" after 2.5/
    private String baseCurrentWeather = "http://api.openweathermap.org/data/2.5/weather?q=";
    // This is the base url needed for the forecast notice the "forecast" after 2.5/
    private String baseForecastWeather = "http://api.openweathermap.org/data/2.5/forecast?q=";
    // This is the base url needed for historical data
    private String baseHistoricalWeather = "https://api.openweathermap.org/data/2.5/onecall/timemachine?lat=";
    // This is the entire url needed for either the current or forecasted weather
    private String apiUrl;
    // Sets the JSON values being sent from the server in a JSONObject for java readability
    // Returns things in inches, feet, yards, etc instead of meters
    private String units = "imperial";
    // Makes sure that everything is in english
    private String lang = "en";
    // The location of the user
    private String location;
    // The unique API key
    private String API_KEY;
    // For the amount of days to look at
    private int days;

    // Used to create a connection with the API
    // URL
    private URL url;
    // Create the connection
    private HttpURLConnection urlConnection;
    // String builders needed
    private StringBuilder requestConnection;
    private StringBuilder response;
    // Buffered needed to get the information from server
    private BufferedReader reader;
    // This is set to the value from reader
    private String readLine;

    // Constructor to set the needed variables for the API to work
    // This sets the location of the user, and the unique key
    // This one is for only the current weather using OWMAPI
    public OpenWeatherMapAPI(String location, String API_KEY){
        this.location = location;
        this.API_KEY = API_KEY;
    }

    // This one is only used for the forecasted weather using OWMAPI
    public OpenWeatherMapAPI(String location, String API_KEY, int days){
        this.location = location;
        this.API_KEY = API_KEY;
        this.days = days;
    }

    // Opens a connection for the current weather at a given location
    public void openCurrentWeatherConnection() throws IOException {

        apiUrl = (baseCurrentWeather + URLEncoder.encode(location, "utf-8") + "&appid=" + API_KEY + "&units=" + units
                + "&lang=" + lang);

        // This adds the link to the requestConnection
        requestConnection = new StringBuilder(apiUrl);
        // These parts grab the url and creates a connection with the url : sets the request method type in this case "GET"
        url = new URL(requestConnection.toString());
        urlConnection = (HttpURLConnection)(url.openConnection());
        urlConnection.setRequestMethod("GET"); // Can either be GET or POST

        // This adds the information being passed
        response = new StringBuilder();
        // Linked to urlConnection which is linked to the requestConnection StringBuilder -> this receives the information then appends
        // to the response StringBuilder
        reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        while((readLine = reader.readLine()) != null){
            response.append(readLine);
            //System.out.println(response); // debugging purposes
        }

        // Closes the connection and disconnects
        reader.close();
        urlConnection.disconnect();
        // We then interpret the data using a parse method
        parseCurrentWeather(response.toString());

        // For the historical data, will grab the lon and lat
        setCoordinates(response.toString());
    }

    // Opens a connection for the forecasted weather at a given location for x amount of days
    public void openForecastWeatherConnection() throws IOException{

        apiUrl = (baseForecastWeather + URLEncoder.encode(location, "utf-8") + "&appid=" + API_KEY + "&units=" + units
                + "&lang=" + lang + "&cnt=" + days);

        // This adds the link to the requestConnection
        requestConnection = new StringBuilder(apiUrl);
        // These parts grab the url and creates a connection with the url : sets the request method type in this case "GET"
        url = new URL(requestConnection.toString());
        urlConnection = (HttpURLConnection)(url.openConnection());
        urlConnection.setRequestMethod("GET"); // Can either be GET or POST

        // This adds the information being passed
        response = new StringBuilder();
        // Linked to urlConnection which is linked to the requestConnection StringBuilder -> this receives the information then appends
        // to the response StringBuilder
        reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        while((readLine = reader.readLine()) != null){
            response.append(readLine);
            //System.out.println(response); // For debugging purposes will delete later
        }

        // Closes the connection and disconnects it
        reader.close();
        urlConnection.disconnect();

        // We then interpret the data using a parse method
        parseForecastWeather(response.toString(), days);
    }

    // Opens a connection for the historical weather at a given location for the past three days based off the current date
    public void openHistoricalWeatherConnection() throws IOException{

        // This is used to get the current system time in UNIX
        Calendar calendar = Calendar.getInstance();
        // This will be used to provide the last five days
        long msInDay = 86400000;
        long currentDayInMS = calendar.getTimeInMillis();
        long date;

        // Set the lat and lon to be used in the apiURL
        double lat = getLat();
        double lon = getLon();

        for(int i = 3; i > 0; i--){
            date = ((currentDayInMS - (msInDay * i)) / 1000);
            apiUrl = (baseHistoricalWeather + lat + "&lon=" + lon + "&dt=" + date + "&appid=" + API_KEY + "&units=" + units);

            // This adds the link to the requestConnection
            requestConnection = new StringBuilder(apiUrl);
            // These parts grab the url and creates a connection with the url : sets the request method type in this case "GET"
            url = new URL(requestConnection.toString());
            urlConnection = (HttpURLConnection)(url.openConnection());
            urlConnection.setRequestMethod("GET"); // Can either be GET or POST

            // This adds the information being passed
            response = new StringBuilder();
            // Linked to urlConnection which is linked to the requestConnection StringBuilder -> this receives the information then appends
            // to the response StringBuilder
            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            while((readLine = reader.readLine()) != null){
                response.append(readLine);
                //System.out.println(response + "\n"); // debugging purposes
            }

            // Since each day is one API call, this has to be in the for loop -> each day is received separately
            parseHistoricalWeather(response.toString());

        }

        // Closes the connection and disconnects
        reader.close();
        urlConnection.disconnect();
    }

    // This method receives the information from response StringBuilder, then is found in the JSON object
    // and sets the cwList to the values needed/wanted for that day
    public void parseCurrentWeather(String response){
        // This will take in all the information, and then is stored in JSON Arrays
        JSONObject currentWeather = new JSONObject(response);

        Object cwDT = currentWeather.get("dt");
        JSONObject cwWind = currentWeather.getJSONObject("wind");
        Object cwVis = currentWeather.get("visibility");
        // These will grab data corresponding to their key
        JSONArray cwWeatherValues = currentWeather.getJSONArray("weather");
        // This will hold the objects(more like Strings) from cwWeatherValues -> cwWeatherValues and cwMV go together
        JSONObject cwWV = cwWeatherValues.getJSONObject(0);
        // This doesn't require to get information from a JSONArray, skip that part
        JSONObject cwMV = currentWeather.getJSONObject("main");

        cwList.add(cwDT);
        cwList.add(cwWind.get("speed"));
        cwList.add(cwVis);
        cwList.add(cwWV.get("description"));
        cwList.add(cwMV.get("temp"));
        cwList.add(cwMV.get("temp_min"));
        cwList.add(cwMV.get("temp_max"));

        if(currentWeather.has("rain")){
            // Stores the raining stat in cwRVGrab
            JSONObject cwRVGrab = currentWeather.getJSONObject("rain");
            // If both aren't true then there's no rain
            if(cwRVGrab.has("3h")){
                cwList.add(cwRVGrab.get("3h") + " -> 3h"); // We can come up with a better way for this, right now is for debugging purposes
            }else if(cwRVGrab.has("1h")){
                cwList.add(cwRVGrab.get("1h") + " -> 1h");
            }
        }

        // Passes the cwList to OpenWeatherMapTools
        setCwListValues(cwList);
    }

    // This method receives the information from response StringBuilder, then is found in the JSON object
    // and sets the fwList to the values needed/wanted for the x(7 in this case) amount of days we want the forcasted weather
    public void parseForecastWeather(String response, int days){
        // This will take in all the information, and then is stored in JSON Arrays
        JSONObject forecastWeather = new JSONObject(response);

        // These will grab data corresponding to their key
        JSONArray fwListValues = forecastWeather.getJSONArray("list");

        // Needs to start at 0 because index 0 is actually the next day then it goes 1 being two days, etc...
        for(int i = 0; i < days; i++){
            // Linked to the JSONArray above this one will get us the forecasted weather for the x day
            JSONObject fwListValuesDay = fwListValues.getJSONObject(i);

            // These will be ordered the same way as the list
            Object fwDT = fwListValuesDay.get("dt");
            JSONObject fwWind = fwListValuesDay.getJSONObject("wind");
            Object fwVis = fwListValuesDay.get("visibility");
            // Values in the JSONArray "weather" are stored in this JSONArray
            JSONArray fwWeatherValues = fwListValuesDay.getJSONArray("weather");
            JSONObject fwWV = fwWeatherValues.getJSONObject(0);
            // This doesn't require to get information from a JSONArray, skip that part
            JSONObject fwMV = fwListValuesDay.getJSONObject("main");

            fwList.add(fwDT);
            fwList.add(fwWind.get("speed"));
            fwList.add(fwVis);
            fwList.add(fwWV.get("description"));
            fwList.add(fwMV.get("temp"));
            fwList.add(fwMV.get("temp_min"));
            fwList.add(fwMV.get("temp_max"));

            // Checks to see if the current location is raining MUST GO LAST
            if(fwListValuesDay.has("rain")){
                // This is used to grab the rain and it's broken down to just get the value instead of the entire JSONObject
                // for example without fwRVGrab the format would look like this: {3h:0.46}
                JSONObject fwRV = (JSONObject) fwListValues.get(i);
                JSONObject fwRVGrab = (JSONObject) fwRV.get("rain");
                // If both aren't true then there's no rain
                if(fwRVGrab.has("3h")){
                    fwList.add(fwRVGrab.get("3h") + " -> 3h"); // We can come up with a better way for this, right now is for debugging purposes
                }else if(fwRVGrab.has("1h")){
                    fwList.add(fwRVGrab.get("1h") + " -> 1h");
                }
            }
        }

        // Passes the fwList to OpenWeatherMapTools
        setFwListValues(fwList);
    }

    public void parseHistoricalWeather(String response){
        JSONObject historicalWeather = new JSONObject(response);

        JSONObject hwCurrent = historicalWeather.getJSONObject("current");
        JSONArray hwWeatherValues = hwCurrent.getJSONArray("weather");
        JSONObject hwWV = hwWeatherValues.getJSONObject(0);

        // Add values wanted to the list
        hwList.add(hwCurrent.get("dt")); // Time
        hwList.add(hwCurrent.get("wind_speed"));
        if(hwCurrent.has("visibility")){
            hwList.add(hwCurrent.get("visibility")); // Sometimes it works, sometimes it doesn't
        }
        hwList.add(hwWV.get("description"));
        hwList.add(hwCurrent.get("temp"));
        hwList.add(hwCurrent.get("humidity"));

        if(hwCurrent.has("rain")){
            JSONObject hwRain = hwCurrent.getJSONObject("rain");

            if(hwRain.has("3h")){
                hwList.add(hwRain.get("3h") + " -> 3h"); // We can come up with a better way for this, right now is for debugging purposes
            }else if(hwRain.has("1h")){
                hwList.add(hwRain.get("1h") + " -> 1h");
            }
        }

        // Passes the hwList to APITools
        setHwListValues(hwList);
    }
}
