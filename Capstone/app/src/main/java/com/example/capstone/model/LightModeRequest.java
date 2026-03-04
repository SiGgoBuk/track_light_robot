package com.example.capstone.model;

public class LightModeRequest {
    private long productId;
    private String mode;

    public LightModeRequest(long productId, String mode) {
        this.productId = productId;
        this.mode = mode;
    }

    public long getProductId() { return productId; }
    public String getMode() { return mode; }
}
