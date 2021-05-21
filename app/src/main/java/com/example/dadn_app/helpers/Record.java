package com.example.dadn_app.helpers;

public class Record {
    private String txt;
    private int audioImage;
    private int videoImage;

    public Record(String txt, int audioImage, int videoImage) {
        this.txt = txt;
        this.audioImage = audioImage;
        this.videoImage = videoImage;
    }

    public Record(Record record) {
        txt = record.txt;
        audioImage = record.audioImage;
        videoImage = record.videoImage;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public int getAudioImage() {
        return audioImage;
    }

    public void setAudioImage(int audioImage) {
        this.audioImage = audioImage;
    }

    public int getVideoImage() {
        return videoImage;
    }

    public void setVideoImage(int videoImage) {
        this.videoImage = videoImage;
    }
}
