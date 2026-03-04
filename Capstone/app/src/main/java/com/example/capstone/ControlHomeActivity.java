package com.example.capstone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ControlHomeActivity extends AppCompatActivity {

    private CardView lightCard, scheduleCard, modeCard, settingCard;
    private TextView goBackTextView, productNameTextView;
    private long productId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_home);

        // View 연결
        lightCard = findViewById(R.id.light);
        scheduleCard = findViewById(R.id.schedule);
        modeCard = findViewById(R.id.mode);
        settingCard = findViewById(R.id.setting);  // 설정 카드 추가
        goBackTextView = findViewById(R.id.gotobackText);
        productNameTextView = findViewById(R.id.productNameText);

        // 인텐트에서 제품 ID와 이름 받기
        productId = getIntent().getLongExtra("productId", -1);
        String productName = getIntent().getStringExtra("productName");

        if (productId == -1 || productName == null) {
            Toast.makeText(this, "제품 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 제품 정보 저장
        SharedPreferences prefs = getSharedPreferences("productPrefs", MODE_PRIVATE);
        prefs.edit()
                .putLong("productId", productId)
                .putString("productName", productName)
                .apply();

        productNameTextView.setText(productName);

        // 조명 제어로 이동
        lightCard.setOnClickListener(v -> {
            Intent intent = new Intent(ControlHomeActivity.this, LightModeSelectActivity.class);
            intent.putExtra("productId", productId);
            startActivity(intent);
        });

        // 일정 등록으로 이동
        scheduleCard.setOnClickListener(v -> {
            Intent intent = new Intent(ControlHomeActivity.this, ScheduleActivity.class);
            intent.putExtra("productId", productId);
            intent.putExtra("productName", productName);
            startActivity(intent);
        });

        // 모터 제어로 이동
        modeCard.setOnClickListener(v -> {
            Intent intent = new Intent(ControlHomeActivity.this, ModeSelectActivity.class);
            intent.putExtra("productId", productId);
            startActivity(intent);
        });

        // 설정 화면으로 이동
        settingCard.setOnClickListener(v -> {
            Intent intent = new Intent(ControlHomeActivity.this, SettingActivity.class);
            intent.putExtra("productId", productId);
            startActivity(intent);
        });

        // 뒤로가기 (제품 선택 화면으로)
        goBackTextView.setOnClickListener(v -> {
            Intent intent = new Intent(ControlHomeActivity.this, ProductListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
