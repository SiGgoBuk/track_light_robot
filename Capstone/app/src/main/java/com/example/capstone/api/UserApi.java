package com.example.capstone.api;

import com.example.capstone.model.ApiResponse;
import com.example.capstone.model.LoginRequest;
import com.example.capstone.model.SignupRequest;
import com.example.capstone.model.UserProfile;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserApi {
    @POST("user/login")
    Call<ApiResponse> login(@Body LoginRequest request);

    @POST("user/signup")
    Call<ApiResponse> signup(@Body SignupRequest request);

    @GET("user/profile")
    Call<UserProfile> getUserProfile(@Query("userId") long userId);

}
