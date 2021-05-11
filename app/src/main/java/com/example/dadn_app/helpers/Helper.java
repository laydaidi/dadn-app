package com.example.dadn_app.helpers;

import android.widget.TextView;

public class Helper {

    private static String apiURL = "http://192.168.1.4:1234";
    private static String accessToken;

    public static String buildAPIURL(String url) {
        return apiURL + url;
    }

    public static void showStatus(TextView txtStatus, String statusMessage) {
        txtStatus.setText(statusMessage);
    }

    public static void hideStatus(TextView txtStatus) {
        txtStatus.setText("");
    }

    public static void setAccessToken(String at) {
        accessToken = at;
    }

    public static String getAccessToken() {
        return accessToken;
    }
}
