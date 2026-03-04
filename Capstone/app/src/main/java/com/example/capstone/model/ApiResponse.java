package com.example.capstone.model;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {
    private boolean success;
    private String message;

    @SerializedName("userId")  // 서버 응답의 "userId" 필드를 이 필드에 매핑
    private Long data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Long getData() {
        return data;
    }
}
