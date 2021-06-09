package com.example.dadn_app.helpers;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.dadn_app.ml.HandPatternRecognition;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class HandPatternRecognitionHelper {
    private Context mContext;
    private static HandPatternRecognition model;
    private TensorBuffer inputFeature;

    public HandPatternRecognitionHelper(Context context) {
        this.mContext = context;
        this.initialize();
    }

    public void initialize() {
        if (model == null) {
            try {
                model = HandPatternRecognition.newInstance(this.mContext);
            } catch (IOException e) {
                // TODO Handle the exception
            }
        }
        inputFeature = TensorBuffer.createFixedSize(new int[]{1, 3, 21, 21}, DataType.FLOAT32);
    }

    public void close() {
        model.close();
        model = null;
    }

    public int infer(ByteBuffer inputBuffer) {
        if (model == null) return -1;

        this.inputFeature.loadBuffer(inputBuffer);

        // Runs model inference and gets result.
        HandPatternRecognition.Outputs outputs = model.process(this.inputFeature);
        TensorBuffer outputFeature = outputs.getOutputFeature0AsTensorBuffer();
        float[] outputArray = outputFeature.getFloatArray();
        return this.getMaxIndex(outputArray);
    }

    private int getMaxIndex(float[] array) {
        int maxAt = 0;

        for (int i = 0; i < array.length; i++) {
            maxAt = array[i] > array[maxAt] ? i : maxAt;
        }

        return maxAt;
    }
}
