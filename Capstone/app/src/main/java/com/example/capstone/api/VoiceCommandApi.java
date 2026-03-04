package com.example.capstone.api;

import com.example.capstone.model.VoiceCommandRequest;
import com.example.capstone.model.VoiceCommandResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface VoiceCommandApi {

    @POST("voice-command")
    Call<Void> registerCommand(@Body VoiceCommandRequest request);

    @GET("voice-command")
    Call<List<VoiceCommandResponse>> getCommands(@Query("productId") long productId);

    @DELETE("voice-command/{id}")
    Call<Void> deleteCommand(@Path("id") long id);
}

