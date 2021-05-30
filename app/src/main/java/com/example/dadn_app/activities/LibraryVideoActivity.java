package com.example.dadn_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.dadn_app.R;
import com.example.dadn_app.helpers.Helper;
import com.example.dadn_app.helpers.Record;
import com.example.dadn_app.helpers.RecordAdapter;
import com.example.dadn_app.helpers.VideoStreamVolleyRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class LibraryVideoActivity extends AppCompatActivity {

    TextView textViewName;
    TextView textViewDescription;
    VideoView videoView;

    File videoFile;

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
                            this.loadVideo(jsonObject.getString("url"));
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


    private void loadVideo(String videoUrl) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Helper.buildAPIURL("/video/" + videoUrl);
        Log.d("LESSON URL: ", url);
        VideoStreamVolleyRequest request = new VideoStreamVolleyRequest(Request.Method.GET, url,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        // TODO handle the response
                        try {
                            if (response!=null) {
                                String filePrefix = videoUrl.substring(0, videoUrl.lastIndexOf("."));
                                String fileSuffix = videoUrl.substring(videoUrl.lastIndexOf(".") + 1);
                                File tempVideo = File.createTempFile(filePrefix, fileSuffix, getCacheDir());
                                tempVideo.deleteOnExit();
                                FileOutputStream fos = new FileOutputStream(tempVideo);
                                fos.write(response);
                                fos.close();

                                videoView.setVideoPath(tempVideo.getAbsolutePath());
                                MediaController mediaController = new MediaController(LibraryVideoActivity.this);
                                videoView.setMediaController(mediaController);
                                mediaController.setMediaPlayer(videoView);
                                videoView.setVisibility(View.VISIBLE);
                                videoView.start();
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
                            e.printStackTrace();
                        }
                    }
                } ,new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO handle the error
                error.printStackTrace();
            }
        }, null);

        queue.add(request);

    }

    private void switchToLogin() {
        Intent i = new Intent(LibraryVideoActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}