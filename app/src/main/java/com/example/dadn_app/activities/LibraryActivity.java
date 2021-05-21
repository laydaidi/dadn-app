package com.example.dadn_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SearchView;

import com.example.dadn_app.R;
import com.example.dadn_app.helpers.Record;
import com.example.dadn_app.helpers.RecordAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class LibraryActivity extends AppCompatActivity {
    ListView recordListView;
    ArrayList<Record> recordList;
    RecordAdapter recordAdapter;

    SearchView editLibSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        // prepare data
        recordList = new ArrayList<Record>(Arrays.asList(
                new Record("Hello", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
                new Record("Good byte", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
                new Record("Run", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
                new Record("Eat", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
                new Record("Sleep", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
                new Record("Write", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
                new Record("Read", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
                new Record("See", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
                new Record("Hear", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
                new Record("Thank you", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
                new Record("Hello", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
                new Record("Good byte", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
                new Record("Run", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
                new Record("Eat", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
                new Record("Sleep", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
                new Record("Write", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
                new Record("Read", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
                new Record("See", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
                new Record("Hear", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
                new Record("Thank you", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24)
        ));


        // bind view to variable
        recordListView = (ListView) findViewById(R.id.libraryRecordListView);

        recordAdapter = new RecordAdapter(this, R.layout.library_record, recordList);
        recordListView.setAdapter(recordAdapter);

        editLibSearch = (SearchView) findViewById(R.id.editLibSearch);
        editLibSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String text = newText;
                recordAdapter.filter(text);
                return false;
            }
        });
    }

    public void onClickBtnBack(View view) {
        Intent i = new Intent(LibraryActivity.this, TextSpeechActivity.class);
        startActivity(i);
        finish();
    }
}