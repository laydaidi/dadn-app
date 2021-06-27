package com.example.dadn_app.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import com.example.dadn_app.R;

public class BmpProducer extends Thread {

    CustomFrameAvailableListner customFrameAvailableListner;

    public int height, width;
    Bitmap bmp;
    Context context;
    ESP32Helper esp32Helper;
    byte[] byteImage;
    Matrix transformMatrix;

    BmpProducer(Context context, ESP32Helper esp32Helper){
        this.context = context;
        this.esp32Helper = esp32Helper;
        this.height = context.getResources().getInteger(R.integer.image_height);
        this.width = context.getResources().getInteger(R.integer.image_width);
        transformMatrix = new Matrix();
        transformMatrix.preScale(1.0f, -1.0f);

        this.bmp = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.toi);
        this.bmp = Bitmap.createScaledBitmap(this.bmp, this.width, this.height,true);
        this.bmp = Bitmap.createBitmap(this.bmp, 0, 0, this.bmp.getWidth(), this.bmp.getHeight(), transformMatrix, true);

        start();
    }

    public void setCustomFrameAvailableListener(CustomFrameAvailableListner customFrameAvailableListner){
        this.customFrameAvailableListner = customFrameAvailableListner;
    }

    public void removeListener() {
        this.customFrameAvailableListner = null;
    }

    private void update_bitmap() {
        this.byteImage = this.esp32Helper.getImage();
        if (this.byteImage == null) return;
        this.bmp = BitmapFactory.decodeByteArray(this.byteImage, 0, this.byteImage.length);
        this.bmp = Bitmap.createScaledBitmap(this.bmp, this.width, this.height,true);
        this.bmp = Bitmap.createBitmap(this.bmp, 0, 0, this.bmp.getWidth(), this.bmp.getHeight(), transformMatrix, true);
    }

    public static final String TAG="BmpProducer";
    @Override
    public void run() {
        super.run();
        while (true) {
            if (bmp == null || customFrameAvailableListner == null)
                continue;
//            Log.d(TAG,"Writing frame");

//            update_bitmap();
            customFrameAvailableListner.onFrame(bmp);

            /*OTMainActivity.imageView.post(new Runnable() {
                @Override
                public void run() {
                    OTMainActivity.imageView.setImageBitmap(bg);
                }
            });*/

            try{
                Thread.sleep(10);
            }catch (Exception e){
                Log.d(TAG,e.toString());
            }
        }
    }
}

