package com.example.capstone.model;

public class VoiceCommandRequest {
    private long productId;
    private long actionId;
    private String inputText;

    public VoiceCommandRequest(long productId, long actionId, String inputText) {
        this.productId = productId;
        this.actionId = actionId;
        this.inputText = inputText;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public long getActionId() {
        return actionId;
    }

    public void setActionId(long actionId) {
        this.actionId = actionId;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }
}
