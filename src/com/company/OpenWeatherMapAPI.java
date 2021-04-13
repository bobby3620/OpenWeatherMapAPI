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
    private String baseForecastWeather = "https://api.openweathermap.org/data/2.5/onecall?lat=";
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
    private String part = "daily";
    // For the lat and lon of a location
    private double lat;
    private double lon;

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

        lat = getLat();
        lon = getLon();

        apiUrl = (baseForecastWeather + lat + "&lon=" + lon + "&appid=" + API_KEY + "&units=" + units
                + "&lang=" + lang + "&exclude=current" + "&exclude=minutely" + "&exclude=hourly" + "&exclude=alerts");

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
            parseForecastWeather(response.toString());
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
        lat = getLat();
        lon = getLon();

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

        Object cwDT = currentWeather.getLong("dt");
        JSONObject cwWind = currentWeather.getJSONObject("wind");
        Object cwVis = currentWeather.getLong("visibility");
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
                cwList.add(cwRVGrab.get("3h")); // We can come up with a better way for this, right now is for debugging purposes
            }else if(cwRVGrab.has("1h")){
                cwList.add(cwRVGrab.get("1h"));
            }
        }

        // Passes the cwList to OpenWeatherMapTools
        setCwListValues(cwList);
        convertCwListValues(response);
    }

    // This method receives the information from response StringBuilder, then is found in the JSON object
    // and sets the fwList to the values needed/wanted for the x(7 in this case) amount of days we want the forcasted weather
    public void parseForecastWeather(String response){
        // This will take in all the information, and then is stored in JSON Arrays
        JSONObject forecastWeather = new JSONObject(response);

        JSONArray fwListValues = forecastWeather.getJSONArray("daily");

        for(int i = 1; i <= 5; i++){
            JSONObject fwListValuesDay = fwListValues.getJSONObject(i);

            // These will be ordered the same way as the list
            Object fwDT = fwListValuesDay.getLong("dt");
            Object fwWind = fwListValuesDay.get("wind_speed");
            JSONArray fwWeatherValues = fwListValuesDay.getJSONArray("weather");
            JSONObject fwWV = fwWeatherValues.getJSONObject(0);
            // This doesn't require to get information from a JSONArray, skip that part
            JSONObject fwMV = fwListValuesDay.getJSONObject("temp");

            fwList.add(fwDT);
            fwList.add(fwWind);
            fwList.add(fwWV.getString("description"));
            fwList.add(fwMV.getDouble("min"));
            fwList.add(fwMV.getDouble("max"));

            //Checks to see if the current location is raining MUST GO LAST
            if(fwListValuesDay.has("rain")){
                // This is used to grab the rain value
                Object fwRV = fwListValuesDay.get("rain");

                fwList.add(fwRV);
            }
        }

        // Passes the fwList to OpenWeatherMapTools
        setFwListValues(fwList);
        convertFwListValues(fwListValues);
    }

    public void parseHistoricalWeather(String response){
        JSONObject historicalWeather = new JSONObject(response);

        JSONObject hwCurrent = historicalWeather.getJSONObject("current");
        JSONArray hwWeatherValues = hwCurrent.getJSONArray("weather");
        JSONObject hwWV = hwWeatherValues.getJSONObject(0);

        // Add values wanted to the list
        hwList.add(hwCurrent.getLong("dt")); // Time
        hwList.add(hwCurrent.getDouble("wind_speed"));
        if(hwCurrent.has("visibility")){
            hwList.add(hwCurrent.getLong("visibility")); // Sometimes it works, sometimes it doesn't
        }
        hwList.add(hwWV.getString("description"));
        hwList.add(hwCurrent.getDouble("temp"));
        hwList.add(hwCurrent.getDouble("humidity"));

        if(hwCurrent.has("rain")){
            JSONObject hwRain = hwCurrent.getJSONObject("rain");
            if(hwRain.has("3h")){
                hwList.add(hwRain.getDouble("3h")); // We can come up with a better way for this, right now is for debugging purposes
            }else if(hwRain.has("1h")){
                hwList.add(hwRain.getDouble("1h"));
            }
        }

        // Passes the hwList to APITools
        setHwListValues(hwList);
        convertHwListValues(hwCurrent);
    }
}
