package com.example.dadn_app.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.dadn_app.R;

import java.util.List;

public class RecordAdapter extends BaseAdapter {
    private Context context;
    private int layout;
    private List<Record> recordList;

    public RecordAdapter(Context context, int layout, List<Record> recordList) {
        this.context = context;
        this.layout = layout;
        this.recordList = recordList;
    }

    @Override
    public int getCount() {
        return recordList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(layout, null); // bind xml layout to view variable

        // bind views to variables
        TextView txt = (TextView) convertView.findViewById(R.id.libraryRecordText);
        ImageButton audioImageBtn = (ImageButton) convertView.findViewById(R.id.libraryRecordAudioImage);
        ImageButton videoImageBtn = (ImageButton) convertView.findViewById(R.id.libraryRecordVideoImage);

        // assign values
        Record record = recordList.get(position);
        txt.setText(record.getTxt());
        audioImageBtn.setImageResource(record.getAudioImage());
        videoImageBtn.setImageResource(record.getVideoImage());

        return convertView;
    }
}
