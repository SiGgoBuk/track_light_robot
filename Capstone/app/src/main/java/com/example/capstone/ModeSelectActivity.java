package com.example.capstone;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstone.api.MotorApi;
import com.example.capstone.model.ModeRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ModeSelectActivity extends AppCompatActivity {

    private MotorApi motorApi;
    private long productId = -1;  // 전달받을 productId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_select);

        // productId 전달받기
        productId = getIntent().getLongExtra("productId", -1);
        if (productId == -1) {
            Toast.makeText(this, "제품 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Retrofit 초기화
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://lightproject.duckdns.org/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        motorApi = retrofit.create(MotorApi.class);

        // 모드 버튼 클릭 리스너
        findViewById(R.id.manual).setOnClickListener(v -> sendMode("MANUAL"));
        findViewById(R.id.stop).setOnClickListener(v -> sendMode("STOP"));
        findViewById(R.id.track).setOnClickListener(v -> sendMode("TRACK"));

        // 홈으로 돌아가기
        TextView homeText = findViewById(R.id.homeText);
        homeText.setOnClickListener(v -> finish());
    }

    private void sendMode(String mode) {
        ModeRequest request = new ModeRequest(productId, mode);
        motorApi.setMode(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ModeSelectActivity.this, "모드 전송 성공: " + mode, Toast.LENGTH_SHORT).show();

                    // MANUAL 선택 시 ControllerActivity로 이동
                    if ("MANUAL".equals(mode)) {
                        Intent intent = new Intent(ModeSelectActivity.this, ControllerActivity.class);
                        intent.putExtra("productId", productId); // 전달
                        startActivity(intent);
                        finish();
                    }

                } else {
                    Toast.makeText(ModeSelectActivity.this, "모드 전송 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ModeSelectActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
