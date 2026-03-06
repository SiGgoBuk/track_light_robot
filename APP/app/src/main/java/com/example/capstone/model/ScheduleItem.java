package com.example.capstone.model;

public class ScheduleItem {
    private long id;
    private String day;
    private String time;
    private String status;
    private boolean isEnabled;

    public ScheduleItem(long id, String day, String time, String status, boolean isEnabled) {
        this.id = id;
        this.day = day;
        this.time = time;
        this.status = status;
        this.isEnabled = isEnabled;
    }

    public long getId() { return id; }
    public String getDay() { return day; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
    public boolean isEnabled() { return isEnabled; }

    public void setEnabled(boolean enabled) { isEnabled = enabled; }
}
