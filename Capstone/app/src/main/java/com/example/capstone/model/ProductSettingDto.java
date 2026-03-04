package com.example.capstone.model;

public class ProductSettingDto {
    private long productId;
    private String settingKey;
    private String settingValue;

    public ProductSettingDto(long productId, String settingKey, String settingValue) {
        this.productId = productId;
        this.settingKey = settingKey;
        this.settingValue = settingValue;
    }

    public long getProductId() { return productId; }
    public String getSettingKey() { return settingKey; }
    public String getSettingValue() { return settingValue; }

    public void setProductId(long productId) { this.productId = productId; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }
}
