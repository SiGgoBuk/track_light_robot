package com.example.capstone.model;

public class ScheduleRequest {
    private long productId;
    private String scheduledTime;  // LocalDateTime은 ISO-8601 문자열 형식으로 전송
    private int ledColorR;
    private int ledColorG;
    private int ledColorB;

    public ScheduleRequest(long productId, String scheduledTime, int ledColorR, int ledColorG, int ledColorB) {
        this.productId = productId;
        this.scheduledTime = scheduledTime;
        this.ledColorR = ledColorR;
        this.ledColorG = ledColorG;
        this.ledColorB = ledColorB;
    }

    public long getProductId() { return productId; }
    public String getScheduledTime() { return scheduledTime; }
    public int getLedColorR() { return ledColorR; }
    public int getLedColorG() { return ledColorG; }
    public int getLedColorB() { return ledColorB; }
}
