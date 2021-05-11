package com.example.dadn_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.dadn_app.R;
import com.example.dadn_app.helpers.Helper;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void onClickBtnLogout(View v) {
        SharedPreferences prefs = getSharedPreferences("com.example.dadn_app", Context.MODE_PRIVATE);
        String refreshToken = prefs.getString("refreshToken", null);

        if (refreshToken != null) {
            try {
                RequestQueue queue = Volley.newRequestQueue(this);
                String url = Helper.buildAPIURL("/users/logout");

                JSONObject obj = new JSONObject();
                obj.put("token", refreshToken);

                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        obj,
                        response -> this.switchToLogin(),
                        error -> {}
                );
                queue.add(request);
            } catch (JSONException e) {}
        } else {}
    }

    private void switchToLogin() {
        Intent i = new Intent(SettingsActivity.this, LoginActivity.class);
        startActivity(i);
    }
}