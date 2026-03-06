package com.example.capstone.api;

import com.example.capstone.model.LightModeRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LightApi {
    @POST("light-mode")
    Call<Void> setLightMode(@Body LightModeRequest request);
}
