package com.company;

import java.io.IOException;

public class Main{

    public static void main(String[] args) {
        OpenWeatherMap owm = new OpenWeatherMap("Fargo", "2a51d00bd4d6240e7428871625d80ff5", 5);
        try{
            owm.openCurrentWeatherConnection();
            owm.openForecastWeatherConnection();
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println(owm.getCwList());
        System.out.println(owm.getFwList());
    }
}
