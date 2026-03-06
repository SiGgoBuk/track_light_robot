package com.example.capstone.api;

import com.example.capstone.model.ProductSettingDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ProductSettingApi {
    @GET("product-settings")
    Call<List<ProductSettingDto>> getSettings(@Query("productId") long productId);

    @POST("product-settings")
    Call<Void> saveOrUpdate(@Body ProductSettingDto dto);
}
