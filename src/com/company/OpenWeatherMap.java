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

public class OpenWeatherMap{

    // ArrayLists
    // Values are added in order like such for both -> 0: description, 1: temp, 2: temp_min, 3: temp_max, 4: rain amount in mm
    private ArrayList<Object> cwList = new ArrayList<>();
    private ArrayList<Object> fwList = new ArrayList<>();

    // Variables
    // This is the base url needed for the current weather notice the "weather" after 2.5/
    private String baseCurrentWeather = "http://api.openweathermap.org/data/2.5/weather?q=";
    // This is the base url needed for the forecast notice the "forecast" after 2.5/
    private String baseForecastWeather = "http://api.openweathermap.org/data/2.5/forecast?q=";
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
    URL url;
    // Create the connection
    HttpURLConnection urlConnection;
    // String builders needed
    StringBuilder requestConnection;
    StringBuilder response;
    // Buffered needed to get the information from server
    BufferedReader reader;
    // This is set to the value from reader
    String readLine;

    // Values needed/wanted
    String description;
    int temperature;
    int tempMin;
    int tempMax;

    // Constructor to set the needed variables for the API to work
    // This sets the location of the user, and the unique key
    // This one is for only the current weather
    public OpenWeatherMap(String location, String API_KEY){
        this.location = location;
        this.API_KEY = API_KEY;
    }

    // This one is only used for the forecasted weather
    public OpenWeatherMap(String location, String API_KEY, int days){
        this.location = location;
        this.API_KEY = API_KEY;
        this.days = days;
    }

    public void openCurrentWeatherConnection() throws IOException{

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
            System.out.println(response); // debugging purposes
        }

        // Closes the connection and disconnects
        reader.close();
        urlConnection.disconnect();
        // We then interpret the data using a parse method
        parseCurrentWeather(response.toString());
    }

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
            System.out.println(response); // For debugging purposes will delete later
        }

        // Closes the connection and disconnects it
        reader.close();
        urlConnection.disconnect();
        // We then interpret the data using a parse method
        parseForecastWeather(response.toString(), days);
    }

    // This method receives the information from response StringBuilder, then is found in the JSON object
    // and sets the cwList to the values needed/wanted for that day
    public void parseCurrentWeather(String response){
        // This will take in all the information, and then is stored in JSON Arrays
        JSONObject currentWeather = new JSONObject(response);

        // These will grab data corresponding to their key
        JSONArray cwWeatherValues = currentWeather.getJSONArray("weather");

        // This will hold the objects(more like Strings) from cwWeatherValues
        JSONObject cwWV;
        // This doesn't require to get information from a JSONArray, skip that part
        JSONObject cwMV = currentWeather.getJSONObject("main");

        // Got rid of the for loop because for this API it only has a 0th index
        cwWV = cwWeatherValues.getJSONObject(0);
        cwList.add(cwWV.get("description"));


        cwList.add(cwMV.get("temp"));
        cwList.add(cwMV.get("temp_min"));
        cwList.add(cwMV.get("temp_max"));

        if(response.contains("rain")){
            // Stores the raining stat in cwRVGrab
            JSONObject cwRVGrab = currentWeather.getJSONObject("rain");
            // If both aren't true then there's no rain
            if(cwRVGrab.has("3h")){
                cwList.add(cwRVGrab.get("3h") + " -> 3h"); // We can come up with a better way for this, right now is for debugging purposes
            }else if(cwRVGrab.has("1h")){
                cwList.add(cwRVGrab.get("1h") + " -> 1h");
            }
        }
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
            // Linked to the JSONArray above this one will get us the forecasted weather for x day
            JSONObject fwListValuesDay = fwListValues.getJSONObject(i);

            // Values in the JSONArray "weather" are stored in this JSONArray
            JSONArray fwWeatherValues = fwListValuesDay.getJSONArray("weather");

            // This will hold the objects(more like Strings) from fwWeatherValues
            JSONObject fwWV;
            // This doesn't require to get information from a JSONArray, skip that part
            JSONObject fwMV = fwListValuesDay.getJSONObject("main");

            // Only the index of zero is needed, will throw errors inside for loop -> for loop deleted
            fwWV = fwWeatherValues.getJSONObject(0);
            fwList.add(fwWV.get("description"));


            fwList.add(fwMV.get("temp"));
            fwList.add(fwMV.get("temp_min"));
            fwList.add(fwMV.get("temp_max"));

            // This is used to grab the rain and it's broken down to just get the value instead of the entire JSONObject
            // for example without fwRVGrab the format would look like this: {3h:0.46}
            JSONObject fwRV = (JSONObject) fwListValues.get(0);

            // Checks to see if the current location is raining
            if(response.contains("rain")){
                JSONObject fwRVGrab = (JSONObject) fwRV.get("rain");
                // If both aren't true then there's no rain
                if(fwRVGrab.has("3h")){
                    fwList.add(fwRVGrab.get("3h") + " -> 3h"); // We can come up with a better way for this, right now is for debugging purposes
                }else if(fwRVGrab.has("1h")){
                    fwList.add(fwRVGrab.get("1h") + " -> 1h");
                }
            }
        }
    }

    public ArrayList<Object> getCwList(){
        return cwList;
    }

    public ArrayList<Object> getFwList(){
        return fwList;
    }
}
