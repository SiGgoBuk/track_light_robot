package com.example.capstone;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstone.api.AnalysisApi;
import com.example.capstone.api.VoiceCommandApi;
import com.example.capstone.model.VoiceCommandResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AnalysisActivity extends AppCompatActivity {

    private TextView avgBrightnessText, avgSensorText, mostUsedLightModeText;
    private TextView avgMotorSpeedText, mostUsedMotorModeText, avgDistanceText;
    private LinearLayout voiceCommandContainer;

    private long productId = -1;

    private VoiceCommandApi voiceCommandApi;
    private AnalysisApi analysisApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        // 뷰 초기화
        avgBrightnessText = findViewById(R.id.avgBrightness);
        avgSensorText = findViewById(R.id.avgSensor);
        mostUsedLightModeText = findViewById(R.id.mostUsedLightMode);
        avgMotorSpeedText = findViewById(R.id.avgMotorSpeed);
        mostUsedMotorModeText = findViewById(R.id.mostUsedMotorMode);
        avgDistanceText = findViewById(R.id.avgDistance);
        voiceCommandContainer = findViewById(R.id.voiceCommandContainer);

        TextView backText = findViewById(R.id.backText);
        backText.setOnClickListener(v -> finish());

        productId = getIntent().getLongExtra("productId", -1);
        if (productId == -1) {
            Toast.makeText(this, "제품 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Retrofit 객체 생성 (한 번만 생성하여 재사용)
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://lightproject.duckdns.org/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        voiceCommandApi = retrofit.create(VoiceCommandApi.class);
        analysisApi = retrofit.create(AnalysisApi.class);

        fetchAnalysisData();
        fetchVoiceCommandList();
    }

    private void fetchAnalysisData() {
        analysisApi.getSummary(productId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    try {
                        int avgBrightness = ((Double) data.get("avgBrightness")).intValue();
                        int avgSensor = ((Double) data.get("avgSensor")).intValue();
                        String mostUsedMode = (String) data.get("mostUsedMode");

                        int left = ((Double) data.get("avgLeftSpeed")).intValue();
                        int right = ((Double) data.get("avgRightSpeed")).intValue();
                        String mostUsedMotor = (String) data.get("mostUsedMotorMode");
                        int ultrasonic = ((Double) data.get("avgUltrasonic")).intValue();

                        avgBrightnessText.setText("💡 평균 밝기: " + avgBrightness + " / 765");
                        avgSensorText.setText("🌥 평균 조도 센서 값: " + avgSensor);
                        mostUsedLightModeText.setText("🧭 가장 많이 사용된 모드: " + mostUsedMode);

                        avgMotorSpeedText.setText("⚙ 평균 좌/우 속도: " + left + " / " + right);
                        mostUsedMotorModeText.setText("📘 가장 많이 사용된 모드: " + mostUsedMotor);
                        avgDistanceText.setText("🦭 평균 초음파 거리: " + ultrasonic + " cm");
                    } catch (Exception e) {
                        Toast.makeText(AnalysisActivity.this, "데이터 처리 오류", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AnalysisActivity.this, "분석 정보 로드 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(AnalysisActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchVoiceCommandList() {
        voiceCommandApi.getCommands(productId).enqueue(new Callback<List<VoiceCommandResponse>>() {
            @Override
            public void onResponse(Call<List<VoiceCommandResponse>> call, Response<List<VoiceCommandResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    voiceCommandContainer.removeAllViews();  // 기존 뷰 초기화
                    for (VoiceCommandResponse cmd : response.body()) {
                        TextView item = new TextView(AnalysisActivity.this);
                        item.setText("음성: " + cmd.getInputText() + " | 액션: " + cmd.getActionName());
                        item.setTextSize(14f);
                        item.setPadding(0, 10, 0, 0);
                        item.setTextColor(getResources().getColor(android.R.color.white));
                        voiceCommandContainer.addView(item);
                    }
                } else {
                    Toast.makeText(AnalysisActivity.this, "음성 명령 로드 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<VoiceCommandResponse>> call, Throwable t) {
                Toast.makeText(AnalysisActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
