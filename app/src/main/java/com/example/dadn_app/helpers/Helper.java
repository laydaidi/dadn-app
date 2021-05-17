package com.example.dadn_app.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Helper {

    private static String apiURL = "http://192.168.1.26:1234";
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

    public static Map<String, String> buildAuthorizationHeader() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + getAccessToken());

        return headers;
    }

    public static void showToast(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void resetToken(
            RequestQueue queue,
            SharedPreferences prefs,
            LambdaFunction successMethod,
            LambdaFunction failedMethod
    ) {
        String url = Helper.buildAPIURL("/users/token");
        String refreshToken = prefs.getString("refreshToken", null);

        if (refreshToken != null) {
            Map<String, String> requestObj = new HashMap<>();
            requestObj.put("token", refreshToken);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(requestObj),
                response -> {
                    try {
                        String accessToken = response.get("token").toString();
                        setAccessToken(accessToken);
                        successMethod.run();
                    } catch (JSONException e) { failedMethod.run(); }
                },
                error -> failedMethod.run()
            );
            queue.add(request);
        } else failedMethod.run();
    }
}
