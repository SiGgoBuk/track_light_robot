package com.example.capstone.api;

import com.example.capstone.model.ApiResponse;
import com.example.capstone.model.ScheduleRequest;
import com.example.capstone.model.ScheduleResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LedScheduleApi {

    @POST("led/schedule")
    Call<ApiResponse> createSchedule(@Body ScheduleRequest request);

    @GET("led/schedule")
    Call<List<ScheduleResponse>> getSchedules(@Query("productId") long productId);

    @DELETE("led/schedule/{id}")
    Call<ApiResponse> deleteSchedule(@Path("id") long id);

}
