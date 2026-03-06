package com.example.capstone.api;

import com.example.capstone.model.ModeRequest;
import com.example.capstone.model.MotorCommandRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MotorApi {
    @POST("motor-mode")
    Call<Void> setMode(@Body ModeRequest request);

    @POST("motor-control")
    Call<Void> sendMotorCommand(@Body MotorCommandRequest request);

}
