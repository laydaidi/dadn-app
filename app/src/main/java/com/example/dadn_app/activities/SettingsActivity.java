package com.example.dadn_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
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

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
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
                    editBirthday.setText(data.getString("birthday"));
                    editAddress.setText(data.getString("address"));

                    switch (data.getInt("gender")) {
                        case 1:
                            radioGender.check(R.id.radioSettingGenderMale);
                            break;
                        case 2:
                            radioGender.check(R.id.radioSettingGenderFemale);
                            break;
                        default:
                            radioGender.check(R.id.radioSettingGenderOther);
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            },
            error -> {
                if (error == null || error.networkResponse == null) {
                    Helper.showToast(getApplicationContext(), "Cant connect to the server");
                    return;
                }
                int statusCode = error.networkResponse.statusCode;
                try {
                    String message = (new JSONObject(new String(error.networkResponse.data))).getString("message");
                    if (statusCode == 403) {
                        if (message.equals("Invalid token")) {
                            // Reset token here
                            Helper.resetToken(
                                queue,
                                getSharedPreferences("com.example.dadn_app", Context.MODE_PRIVATE),
                                this::renderInformation,
                                this::switchToLogin
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

    private void backToTextSpeechActivity() {
        Intent i = new Intent(SettingsActivity.this, TextSpeechActivity.class);
        startActivity(i);
        finish();
    }

    private void onClickBtnSaveInformation() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Helper.buildAPIURL("/users/save-information");

        Map<String, String> requestObj = new HashMap<>();
        requestObj.put("email", editEmail.getText().toString());
        requestObj.put("phone", editPhone.getText().toString());
        requestObj.put("birthday", editBirthday.getText().toString());
        requestObj.put("address", editAddress.getText().toString());

        int genderSelectedId = radioGender.getCheckedRadioButtonId();
        switch (genderSelectedId) {
            case R.id.radioSettingGenderMale:
                requestObj.put("gender", "1");
                break;
            case R.id.radioSettingGenderFemale:
                requestObj.put("gender", "2");
                break;
            default:
                requestObj.put("gender", "0");
                break;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(requestObj),
            response -> {
                Helper.showToast(getApplicationContext(), "Update successfully");
                backToTextSpeechActivity();
            },
            error -> {
                if (error == null || error.networkResponse == null) {
                    Helper.showToast(getApplicationContext(), "Cant connect to the server");
                    return;
                }
                int statusCode = error.networkResponse.statusCode;
                try {
                    String message = (new JSONObject(new String(error.networkResponse.data))).getString("message");
                    if (statusCode == 403) {
                        if (message.equals("Invalid token")) {
                            // Reset token here
                            Helper.resetToken(
                                queue,
                                getSharedPreferences("com.example.dadn_app", Context.MODE_PRIVATE),
                                this::onClickBtnSaveInformation,
                                this::switchToLogin
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

    private void onClickBtnLogout() {
        SharedPreferences prefs = getSharedPreferences("com.example.dadn_app", Context.MODE_PRIVATE);
        String refreshToken = prefs.getString("refreshToken", null);

        if (refreshToken != null) {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = Helper.buildAPIURL("/users/logout");

            Map<String, String> requestObject = new HashMap<>();
            requestObject.put("token", refreshToken);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(requestObject),
                response -> this.switchToLogin(),
                error -> {}
            );
            queue.add(request);
        } else {
            this.switchToLogin();
        }
    }

    private void switchToLogin() {
        Intent i = new Intent(SettingsActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}