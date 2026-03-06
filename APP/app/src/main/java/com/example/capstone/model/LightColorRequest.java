package com.example.capstone.model;

public class LightColorRequest {
    private long productId;
    private int red;
    private int green;
    private int blue;

    public LightColorRequest(long productId, int red, int green, int blue) {
        this.productId = productId;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public long getProductId() { return productId; }
    public int getRed() { return red; }
    public int getGreen() { return green; }
    public int getBlue() { return blue; }
}
