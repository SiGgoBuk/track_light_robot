package com.example.capstone.model;

import com.google.gson.annotations.SerializedName;

public class VoiceCommandItem {
    @SerializedName("id")
    private Long id;

    @SerializedName("inputText")
    private String commandText;

    @SerializedName("actionName")
    private String function;

    public VoiceCommandItem(Long id, String commandText, String function) {
        this.id = id;
        this.commandText = commandText;
        this.function = function;
    }

    public Long getId() {
        return id;
    }

    public String getCommandText() {
        return commandText;
    }

    public String getFunction() {
        return function;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCommandText(String commandText) {
        this.commandText = commandText;
    }

    public void setFunction(String function) {
        this.function = function;
    }
}
