package com.example.dadn_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.dadn_app.R;
import com.example.dadn_app.helpers.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private EditText editUsername, editEmail, editPhone, editBirthday, editAddress;
    private RadioGroup radioGender;
    private Button btnSaveInformation, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        editUsername = (EditText) findViewById(R.id.editSettingUsername);
        editEmail = (EditText) findViewById(R.id.editSettingEmail);
        editPhone = (EditText) findViewById(R.id.editSettingPhone);
        editBirthday = (EditText) findViewById(R.id.editSettingBirthday);
        editAddress = (EditText) findViewById(R.id.editSettingAddress);

        radioGender = (RadioGroup) findViewById(R.id.radioSettingGender);

        btnSaveInformation = (Button) findViewById(R.id.btnSaveInformation);
        btnLogout = (Button) findViewById(R.id.btnLogout);

        btnSaveInformation.setOnClickListener(v -> onClickBtnSaveInformation());
        btnLogout.setOnClickListener(v -> onClickBtnLogout());

        this.renderInformation();
    }

    private void renderInformation() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Helper.buildAPIURL("/users/information");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                try {
                    JSONObject data = response.getJSONObject("info");
                    editUsername.setText(data.getString("name"));
                    editEmail.setText(data.getString("email"));
                    editPhone.setText(data.getString("phone"));
                    editBirthday.setText(data.getString("gender"));
                    editAddress.setText(data.getString("address"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            },
            error -> {
                int statusCode = error.networkResponse.statusCode;
                try {
                    String message = (new JSONObject(new String(error.networkResponse.data))).getString("message");
                    if (statusCode == 403) {
                        if (message.equals("Invalid token")) {
                            // Reset token here
                            Helper.resetToken(
                                queue,
                                getSharedPreferences("com.example.dadn_app", Context.MODE_PRIVATE),
                                () -> this.renderInformation(),
                                () -> this.switchToLogin()
                            );
                        } else {
                            Helper.showToast(getApplicationContext(), message);
                        }
                    } else if (statusCode == 401) {
                        Helper.showToast(getApplicationContext(), message);
                    } else {
                        Helper.showToast(getApplicationContext(), "Cant connect to the server");
                    }
                } catch (JSONException e) {
                    this.switchToLogin();
                }
            }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return Helper.buildAuthorizationHeader();
            }
        };
        queue.add(request);
    }

    private void onClickBtnSaveInformation() {

    }

    private void onClickBtnLogout() {
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
            } catch (JSONException e) {
                this.switchToLogin();
            }
        } else {
            this.switchToLogin();
        }
    }

    private void switchToLogin() {
        Intent i = new Intent(SettingsActivity.this, LoginActivity.class);
        startActivity(i);
    }
}