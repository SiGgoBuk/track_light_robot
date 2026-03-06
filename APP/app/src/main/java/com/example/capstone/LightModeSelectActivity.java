package com.example.capstone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstone.api.LightApi;
import com.example.capstone.model.LightModeRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LightModeSelectActivity extends AppCompatActivity {

    private long productId;
    private LightApi lightApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_mode_select);

        // 선택된 productId 가져오기
        productId = getIntent().getLongExtra("productId", -1);
        if (productId == -1) {
            Toast.makeText(this, "제품 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://lightproject.duckdns.org/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        lightApi = retrofit.create(LightApi.class);

        // 조명 모드 버튼 클릭 리스너
        findViewById(R.id.app).setOnClickListener(v -> sendMode("APP"));
        findViewById(R.id.auto).setOnClickListener(v -> sendMode("AUTO"));
        findViewById(R.id.manual).setOnClickListener(v -> sendMode("MANUAL"));

        TextView homeText = findViewById(R.id.homeText);
        homeText.setOnClickListener(v -> finish());
    }

    private void sendMode(String mode) {
        Log.d("LightDebug", "전송 productId: " + productId + ", mode: " + mode);

        LightModeRequest request = new LightModeRequest(productId, mode);
        lightApi.setLightMode(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LightModeSelectActivity.this, "조명 모드 설정 성공: " + mode, Toast.LENGTH_SHORT).show();

                    if ("APP".equals(mode)) {
                        Intent intent = new Intent(LightModeSelectActivity.this, ColorPickerActivity.class);
                        intent.putExtra("productId", productId);  // 필요 시 다음 화면에도 전달
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(LightModeSelectActivity.this, "설정 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(LightModeSelectActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
