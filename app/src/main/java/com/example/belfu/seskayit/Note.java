package com.example.belfu.seskayit;

/**
 * Created by belfu on 27.02.2018.
 */

public class Note {
    String voice;
    String note;
    String location;

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Note(String voice, String note, String location) {
        this.voice = voice;
        this.note = note;
        this.location = location;
    }
   /* public String toString(){
        return;
    }*/
}
