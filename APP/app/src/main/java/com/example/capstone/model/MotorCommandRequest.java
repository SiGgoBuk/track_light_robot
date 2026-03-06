package com.example.capstone.model;

public class MotorCommandRequest {
    private long productId;
    private String commandType;

    public MotorCommandRequest(long productId, String commandType) {
        this.productId = productId;
        this.commandType = commandType;
    }
}
