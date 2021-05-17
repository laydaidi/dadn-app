package com.example.dadn_app.helpers;

public class SharedImageBuffer {
    private static final ImageBuffer imageBuffer = new ImageBuffer(10);

    public static ImageBuffer getImageBuffer() {
        return imageBuffer;
    }
}
