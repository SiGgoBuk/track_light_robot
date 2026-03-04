// ProductApi.java
package com.example.capstone.api;

import com.example.capstone.model.ProductDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ProductApi {
    @GET("/api/product/list")
    Call<List<ProductDto>> getProductsByUserId(@Query("userId") long userId);
}
