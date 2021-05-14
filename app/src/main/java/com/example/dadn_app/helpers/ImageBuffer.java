package com.example.dadn_app.helpers;

import java.util.concurrent.ArrayBlockingQueue;

public class ImageBuffer {
    private final int bufferSize;
    private final ArrayBlockingQueue<byte[]> queue;

    public ImageBuffer(int size) {
        bufferSize = size;
        queue = new ArrayBlockingQueue<>(bufferSize);
    }

    public void put(byte[] data, boolean dropIfFull) {
        try {
            if (dropIfFull && isFull()) {
                queue.take();
            }
            queue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public byte[] get() {
        try {
            if (!isEmpty()) {
                return queue.take();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean clear() {
        queue.clear();
        return true;
    }

    public int size() {
        return queue.size();
    }

    public int maxSize() {
        return bufferSize;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public boolean isFull() {
        return queue.size() == bufferSize;
    }
}

