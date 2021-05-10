package com.example.dadn_app.helpers;

public class Helper {

    private static String apiURL = "http://192.168.1.4";

    public static String buildAPIURL(String url) {
        return apiURL + url;
    }
}
