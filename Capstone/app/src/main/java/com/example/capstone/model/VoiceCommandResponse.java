package com.example.capstone.model;

public class VoiceCommandResponse {
    private Long id;
    private String inputText;
    private String actionName;

    // 기본 생성자
    public VoiceCommandResponse() {}

    // getter 및 setter
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getInputText() {
        return inputText;
    }
    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public String getActionName() {
        return actionName;
    }
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
}
