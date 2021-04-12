package com.company;

import org.json.JSONObject;
import java.util.ArrayList;

public class APITools{

    // Values are added in order like such for both -> 0: description, 1: temp, 2: temp_min, 3: temp_max, 4: rain amount in mm
    static private ArrayList<Object> cwListValues = new ArrayList<>();
    static private ArrayList<Object> fwListValues = new ArrayList<>();
    static private ArrayList<Object> hwListValues = new ArrayList<>();

    // Used to store lon and lat
    static private double lat;
    static private double lon;

    /**
     *
     *  SETTER METHODS
     *
     *  */
    public void setCwListValues(ArrayList<Object> cwListValues){
        this.cwListValues = cwListValues;
    }

    public void setFwListValues(ArrayList<Object> fwListValues){
        this.fwListValues = fwListValues;
    }

    public void setHwListValues(ArrayList<Object> hwListValues){
        this.hwListValues = hwListValues;
    }

    public void setCoordinates(String response){
        JSONObject coordinates = new JSONObject(response);
        JSONObject coord = coordinates.getJSONObject("coord");
        // These are used to temporarily store the Objects as Strings
        String tempLat = coord.get("lat").toString();
        String tempLon = coord.get("lon").toString();
        // Converts the temps into doubles for values to be passed
        lat = Double.parseDouble(tempLat);
        lon = Double.parseDouble(tempLon);
    }

    /**
     *
     *
     *  GETTER METHODS
     *
     *  */
    public ArrayList<Object> getCwList(){
        return this.cwListValues;
    }

    public ArrayList<Object> getFwList(){
        return this.fwListValues;
    }

    public ArrayList<Object> getHwList(){
        return this.hwListValues;
    }

    public double getLat(){
        return this.lat;
    }

    public double getLon(){
        return this.lon;
    }

}
