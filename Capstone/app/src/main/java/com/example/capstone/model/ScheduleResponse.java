package com.example.capstone.model;

public class ScheduleResponse {
    private long id;
    private String scheduledTime;
    private int ledColorR;
    private int ledColorG;
    private int ledColorB;

    public long getId() { return id; }
    public String getScheduledTime() { return scheduledTime; }
    public int getLedColorR() { return ledColorR; }
    public int getLedColorG() { return ledColorG; }
    public int getLedColorB() { return ledColorB; }

    public boolean isOn() {
        return ledColorR > 0 || ledColorG > 0 || ledColorB > 0;
    }

    public String getDay() {
        return scheduledTime.split("T")[0];
    }

    public String getTime() {
        return scheduledTime.split("T")[1];
    }

    public String getStatus() {
        return isOn() ? "ON" : "OFF";
    }
}

