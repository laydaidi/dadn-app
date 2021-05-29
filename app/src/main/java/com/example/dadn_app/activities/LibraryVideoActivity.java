package com.example.dadn_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.dadn_app.R;
import com.example.dadn_app.helpers.Helper;
import com.example.dadn_app.helpers.Record;
import com.example.dadn_app.helpers.RecordAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class LibraryVideoActivity extends AppCompatActivity {

    TextView textViewName;
    TextView textViewDescription;
    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_video);

        textViewName = (TextView) findViewById(R.id.textViewName);
        textViewDescription = (TextView) findViewById(R.id.textViewDescription);
        videoView = (VideoView) findViewById(R.id.videoView2);

        this.loadLesson();

//        textViewName.setText("Xin chào");
//        textViewDescription.setText("Tay phải giơ lên cao ngang tầm đầu bên phải, lòng bàn tay hướng ra trước rồi vẩy tay nhẹ qua lại hai lần.");
//        videoView.setVideoURI(Uri.parse("https://youtu.be/bdCZaL_VyqM"));
    }

    public void onClickBtnBackLibraryVideoActivity(View view) {
        Intent i = new Intent(LibraryVideoActivity.this, LibraryActivity.class);
        startActivity(i);
        finish();
    }

    private void loadLesson() {
        RequestQueue queue = Volley.newRequestQueue(this);
        Intent i = getIntent();
        String lessonId = i.getStringExtra("id");
        String url = Helper.buildAPIURL("/lessons/view/" + lessonId);
        Log.d("LESSON URL: ", url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject jsonObject = (JSONObject) response.getJSONObject("lessons");

                        if (jsonObject != null) {
                            textViewName.setText(jsonObject.getString("name"));
                            textViewDescription.setText(jsonObject.getString("description"));
                            videoView.setVideoURI(Uri.parse(jsonObject.getString("url")));
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
                                        this::loadLesson,
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

    private void switchToLogin() {
        Intent i = new Intent(LibraryVideoActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}