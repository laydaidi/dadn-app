package com.example.dadn_app.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.dadn_app.R;

public class BmpProducer extends Thread {

    CustomFrameAvailableListner customFrameAvailableListner;

    public int height = 800, width = 600;
    Bitmap bmp;

    BmpProducer(Context context, ESP32Helper esp32Helper){
//        byte[] byteImage = esp32Helper.getImage();
//        bmp = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.img2);
        bmp = Bitmap.createScaledBitmap(bmp,600,800,true);
        height = bmp.getHeight();
        width = bmp.getWidth();

        start();
    }

    public void setCustomFrameAvailableListener(CustomFrameAvailableListner customFrameAvailableListner){
        this.customFrameAvailableListner = customFrameAvailableListner;
    }

    public static final String TAG="BmpProducer";
    @Override
    public void run() {
        super.run();
        while ((true)){
            if(bmp==null || customFrameAvailableListner == null)
                continue;
            Log.d(TAG,"Writing frame");
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

