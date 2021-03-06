package com.company;

import java.io.IOException;

public class Main{

    public static void main(String[] args) {
        OpenWeatherMapAPI owm = new OpenWeatherMapAPI("Philadelphia", "2a51d00bd4d6240e7428871625d80ff5");
        APITools tools = new APITools();
        try{
            owm.openCurrentWeatherConnection(); // This one must be called first because it grabs the lat and lon for the next two connections
            owm.openForecastWeatherConnection();
            owm.openHistoricalWeatherConnection();
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println(tools.getCwList() + "\n");
        System.out.println(owm.getFwList() + "\n");
        System.out.println(tools.getHwList() + "\n");
    }
}
