package com.example.dadn_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.dadn_app.R;
import com.example.dadn_app.helpers.Helper;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private TextView txtStatus;
    private EditText editUsername, editPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtStatus = (TextView) findViewById(R.id.loginStatusText);
        editUsername = (EditText) findViewById(R.id.editUsername);
        editPassword = (EditText) findViewById(R.id.editPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> onClickBtnLogin());
    }

    public void onClickBtnCancel(View view) {
        Intent i = new Intent(LoginActivity.this, WelcomeActivity.class);
        startActivity(i);
    }

    public void onClickSignup(View view) {
        Intent i = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(i);
    }

    public void onClickForgotPassword(View view) {
        Intent i = new Intent(LoginActivity.this, FotgotPasswordActivity.class);
        startActivity(i);
    }

    private void onClickBtnLogin() {
        try {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = Helper.buildAPIURL("/users/login");

            JSONObject obj = new JSONObject();
            obj.put("username", editUsername.getText());
            obj.put("password", editPassword.getText());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    obj,
                    response -> {
                        Helper.hideStatus(txtStatus);
                        try {
                            String accessToken = response.get("accessToken").toString();
                            String refreshToken = response.get("refreshToken").toString();

                            Helper.setAccessToken(accessToken);

                            // Save refresh token
                            SharedPreferences prefs = getSharedPreferences("com.example.dadn_app", Context.MODE_PRIVATE);
                            prefs.edit().putString("refreshToken", refreshToken).apply();

                            Intent i = new Intent(LoginActivity.this, TextSpeechActivity.class);
                            startActivity(i);
                        } catch (JSONException e) {
                            Helper.showStatus(txtStatus, "Response is invalid");
                        }
                    },
                    error -> {
                        if (error.networkResponse.statusCode == 401) {
                            try {
                                JSONObject res = new JSONObject(new String(error.networkResponse.data));
                                String message = res.get("message").toString();
                                Helper.showStatus(txtStatus, message);
                            } catch (JSONException e) {
                                Helper.showStatus(txtStatus, "Response is invalid");
                            }
                        } else {
                            Helper.showStatus(txtStatus, "Cant connect to the server");
                        }

                    }
            );
            queue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}