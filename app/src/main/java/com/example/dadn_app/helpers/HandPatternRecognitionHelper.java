package com.example.dadn_app.helpers;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import com.example.dadn_app.R;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class HandPatternRecognitionHelper {
    private Context mContext;
    Interpreter model;
    private int NUM_PATTERNS;

    public HandPatternRecognitionHelper(Context context) {
        this.mContext = context;
        this.NUM_PATTERNS = context.getResources().getInteger(R.integer.num_patterns);
        this.initialize();
    }

    private MappedByteBuffer loadModelFile() throws IOException{
        AssetFileDescriptor fileDescriptor = this.mContext.getAssets().openFd("hand_pattern_recognition.tflite");
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset=fileDescriptor.getStartOffset();
        long declareLength=fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declareLength);
    }

    public void initialize() {
        try {
            this.model = new Interpreter(this.loadModelFile());
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void close() {
        this.model.close();
    }

    public int doInference(float[][][] inputBuffer) {
        float[][] output = new float[1][NUM_PATTERNS];
        try {
            this.model.run(inputBuffer, output);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return this.getMaxIndex(output[0]);
    }

    private int getMaxIndex(float[] array) {
        int maxAt = 0;

        for (int i = 0; i < array.length; i++) {
            maxAt = (array[i] > array[maxAt]) ? i : maxAt;
        }

        return maxAt;
    }
}

