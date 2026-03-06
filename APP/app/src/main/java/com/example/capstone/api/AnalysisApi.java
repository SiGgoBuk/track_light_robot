package com.example.capstone.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AnalysisApi {
    @GET("analysis/summary")
    Call<Map<String, Object>> getSummary(@Query("productId") long productId);
}
