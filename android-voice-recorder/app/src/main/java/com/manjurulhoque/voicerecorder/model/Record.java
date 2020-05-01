package com.manjurulhoque.voicerecorder.model;

public class Record {

    private String name;
    private String minute;

    public Record() {
    }

    public Record(String name, String minute) {
        this.name = name;
        this.minute = minute;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }
}
