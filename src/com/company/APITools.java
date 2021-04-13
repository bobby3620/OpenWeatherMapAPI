package com.company;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class APITools{

    // Used to convert the epoch time to date
    Date date;
    DateFormat dateFormat;
    String dateFormatted;

    // Used to convert objects in their correct data type
    String stringToConvert;
    Long convertedLong;
    Double convertedDouble;

    // Used to convert the visibility from meters to yards
    NumberFormat numberFormat = new DecimalFormat("#0.00");
    String storeFormattedNumber;

    // Used to grab the last index
    private int lastIndex;

    // Used to store values in the JSON response
    private Object temp;

    // Used to store the rain values in historical list
    Object tempRainGrab;

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

    /**
     *
     *  CONVERSION METHODS
     *
     *  */
    public void convertCwListValues(String response){
        // This will convert the time to the date in the format of -> MM/dd/yyyy
        stringToConvert = String.valueOf(getCwList().get(0));
        convertedLong = Long.parseLong(stringToConvert);
        date = new Date((convertedLong) * 1000);
        dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        dateFormatted = dateFormat.format(date);
        // Sets the date in cwList
        getCwList().set(0, dateFormatted);

        // This will convert the visibility from meters to yards
        stringToConvert = String.valueOf(getCwList().get(2));
        convertedDouble = Double.parseDouble(stringToConvert);
        convertedDouble /= (1609);
        storeFormattedNumber = numberFormat.format(convertedDouble);
        // Sets the visibility in cwList from meters to miles
        getCwList().set(2, storeFormattedNumber);

        // Sets the rain from mm to inches
        lastIndex = (getCwList().size() - 1);

        if(response.contains("rain")){
            stringToConvert = String.valueOf(getCwList().get(lastIndex));
            convertedDouble = Double.parseDouble(stringToConvert);
            convertedDouble /= (25.4);
            storeFormattedNumber = numberFormat.format(convertedDouble);
            getCwList().set(lastIndex, storeFormattedNumber);
        }
    }

    public void convertFwListValues(JSONArray fwListValues){

        // This will convert the time to the date in the format of -> MM/dd/yyyy
        for(int i = 0; i < fwListValues.length(); i++){
            JSONObject fwListValuesDay = fwListValues.getJSONObject(i);
            temp = fwListValuesDay.get("dt");
            stringToConvert = String.valueOf(temp);
            for(int j = 0; j < getFwList().size(); j++){
                if(stringToConvert.equals(getFwList().get(j).toString())){
                    convertedLong = Long.parseLong(stringToConvert);
                    date = new Date((convertedLong) * 1000);
                    dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                    dateFormatted = dateFormat.format(date);
                    // Sets the date in fwList
                    getFwList().set(j, dateFormatted);
                }
            }

            // This will convert the rain from mm to inches
            if(fwListValuesDay.has("rain")){
                temp = fwListValuesDay.get("rain");
                stringToConvert = String.valueOf(temp);
                for(int j = 0; j < getFwList().size(); j++){
                    if(stringToConvert.equals(getFwList().get(j).toString())){
                        convertedDouble = Double.parseDouble(stringToConvert);
                        convertedDouble /= (25.4);
                        storeFormattedNumber = numberFormat.format(convertedDouble);
                        getFwList().set(j, storeFormattedNumber);
                    }
                }
            }
        }
    }

    public void convertHwListValues(JSONObject hwCurrentValues){
        // Date conversion
        temp = hwCurrentValues.get("dt");
        stringToConvert = String.valueOf(temp);
        for(int i = 0; i < getHwList().size(); i++){
            if(stringToConvert.equals(getHwList().get(i).toString())){
                convertedLong = Long.parseLong(stringToConvert);
                date = new Date((convertedLong) * 1000);
                dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                dateFormatted = dateFormat.format(date);
                // Sets the date in fwList
                getHwList().set(i, dateFormatted);
            }
        }

        // Visibility conversion
        temp = hwCurrentValues.get("visibility");
        stringToConvert = String.valueOf(temp);
        for(int i = 0; i < getHwList().size(); i++){
            if(stringToConvert.equals(getHwList().get(i).toString())){
                convertedDouble = Double.parseDouble(stringToConvert);
                convertedDouble /= (1609);
                storeFormattedNumber = numberFormat.format(convertedDouble);
                getHwList().set(i, storeFormattedNumber);
            }
        }

        // If contains rain -> Rain conversion
        if(hwCurrentValues.has("rain")){
            JSONObject tempRain = hwCurrentValues.getJSONObject("rain");
            if(tempRain.has("3h")){
                tempRainGrab = tempRain.get("3h");
            }else if(tempRain.has("1h")){
                tempRainGrab = tempRain.get("1h");
            }
            stringToConvert = String.valueOf(tempRainGrab);

            for(int i = 0; i < getHwList().size(); i++){
                if(stringToConvert.equals(getHwList().get(i).toString())){
                    convertedDouble = Double.parseDouble(stringToConvert);
                    convertedDouble /= (25.4);
                    storeFormattedNumber = numberFormat.format(convertedDouble);
                    getHwList().set(i, storeFormattedNumber);
                }
            }
        }
    }

}
