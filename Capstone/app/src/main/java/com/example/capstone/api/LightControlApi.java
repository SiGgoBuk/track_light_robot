package com.example.capstone.api;

import com.example.capstone.model.LightColorRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LightControlApi {
    @POST("light-control")
    Call<Void> sendColor(@Body LightColorRequest request);
}