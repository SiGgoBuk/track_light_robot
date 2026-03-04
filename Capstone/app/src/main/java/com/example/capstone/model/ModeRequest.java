package com.example.capstone.model;

public class ModeRequest {
    private long productId;
    private String mode;

    public ModeRequest(long productId, String mode) {
        this.productId = productId;
        this.mode = mode;
    }
}
