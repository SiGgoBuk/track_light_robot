package com.example.capstone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstone.api.LedScheduleApi;
import com.example.capstone.api.UserApi;
import com.example.capstone.model.ApiResponse;
import com.example.capstone.model.ScheduleItem;
import com.example.capstone.model.ScheduleResponse;
import com.example.capstone.model.UserProfile;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyPageActivity extends AppCompatActivity {

    private RecyclerView scheduleRecyclerView;
    private ScheduleAdapter adapter;
    private ArrayList<ScheduleItem> scheduleList;
    private TextView userNameText, userEmailText, homeText, logoutText;

    private long productId = -1;
    private String productName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        // 인텐트에서 productId, productName 받기
        productId = getIntent().getLongExtra("productId", -1);
        productName = getIntent().getStringExtra("productName");

        if (productId == -1 || productName == null) {
            Toast.makeText(this, "제품 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userNameText = findViewById(R.id.userNameText);
        userEmailText = findViewById(R.id.userEmailText);
        homeText = findViewById(R.id.homeText);
        logoutText = findViewById(R.id.logoutText);
        scheduleRecyclerView = findViewById(R.id.scheduleRecyclerView);
        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        scheduleList = new ArrayList<>();

        Button analysisButton = findViewById(R.id.analysisButton);
        Button voiceButton = findViewById(R.id.voiceButton);  // 👈 보이스 버튼 추가

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://lightproject.duckdns.org/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LedScheduleApi ledApi = retrofit.create(LedScheduleApi.class);

        adapter = new ScheduleAdapter(scheduleList, new ScheduleAdapter.OnItemActionListener() {
            @Override
            public void onDelete(int position) {
                ScheduleItem item = scheduleList.get(position);
                long scheduleId = item.getId();

                ledApi.deleteSchedule(scheduleId).enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            scheduleList.remove(position);
                            adapter.notifyItemRemoved(position);
                            Toast.makeText(MyPageActivity.this, "일정 삭제 완료", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MyPageActivity.this, "삭제 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        Toast.makeText(MyPageActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onToggle(int position, boolean isOn) {
                scheduleList.get(position).setEnabled(isOn);
                // 상태 토글 서버 연동은 필요 시 구현 가능
            }
        });

        scheduleRecyclerView.setAdapter(adapter);

        // 사용자 정보 불러오기
        SharedPreferences userPrefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
        long userId = userPrefs.getLong("userId", -1);

        UserApi userApi = retrofit.create(UserApi.class);
        userApi.getUserProfile(userId).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile profile = response.body();
                    userNameText.setText("이름: " + profile.getFirstName());
                    userEmailText.setText("이메일: " + profile.getUsername());
                } else {
                    Toast.makeText(MyPageActivity.this, "프로필 불러오기 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                Toast.makeText(MyPageActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // 일정 불러오기
        ledApi.getSchedules(productId).enqueue(new Callback<List<ScheduleResponse>>() {
            @Override
            public void onResponse(Call<List<ScheduleResponse>> call, Response<List<ScheduleResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (ScheduleResponse s : response.body()) {
                        String[] parts = s.getScheduledTime().split("T");
                        String day = parts[0];
                        String time = parts.length > 1 ? parts[1] : "00:00";
                        String status = (s.getLedColorR() > 0 || s.getLedColorG() > 0 || s.getLedColorB() > 0) ? "ON" : "OFF";

                        scheduleList.add(new ScheduleItem(s.getId(), day, time, status, true));
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MyPageActivity.this, "일정 불러오기 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ScheduleResponse>> call, Throwable t) {
                Toast.makeText(MyPageActivity.this, "일정 로드 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        homeText.setOnClickListener(v -> {
            Intent intent = new Intent(MyPageActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        logoutText.setOnClickListener(v -> {
            SharedPreferences loginPrefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = loginPrefs.edit();

            if (loginPrefs.getBoolean("remember", false)) {
                String rememberedEmail = loginPrefs.getString("email", "");
                editor.clear();
                editor.putString("email", rememberedEmail);
                editor.putBoolean("remember", true);
            } else {
                editor.clear();
            }

            editor.apply();

            Intent intent = new Intent(MyPageActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        analysisButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyPageActivity.this, AnalysisActivity.class);
            intent.putExtra("productId", productId);  // productId 꼭 전달!
            startActivity(intent);
        });

        // 보이스 버튼 클릭 시 VoiceCommandActivity 이동
        voiceButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyPageActivity.this, VoiceCommandActivity.class);
            intent.putExtra("productId", productId);
            intent.putExtra("productName", productName);
            startActivity(intent);
        });
    }
}
