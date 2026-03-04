package com.example.capstone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HomeActivity extends AppCompatActivity {

    private CardView myPageCard, productCard;
    private TextView logoutTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // CardView와 TextView 연결
        myPageCard = findViewById(R.id.mypage);
        productCard = findViewById(R.id.product);
        logoutTextView = findViewById(R.id.logoutTextView);

        // 마이페이지 이동
        myPageCard.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("productPrefs", MODE_PRIVATE);
            long productId = prefs.getLong("productId", -1);
            String productName = prefs.getString("productName", null);

            if (productId == -1 || productName == null) {
                Toast.makeText(this, "제품 정보가 없어 마이페이지로 이동할 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(HomeActivity.this, MyPageActivity.class);
            intent.putExtra("productId", productId);
            intent.putExtra("productName", productName);
            startActivity(intent);
        });


        // 제품 선택 화면 이동
        productCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProductListActivity.class);
            startActivity(intent);
        });

        // 로그아웃 처리
        logoutTextView.setOnClickListener(v -> {
            SharedPreferences loginPrefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = loginPrefs.edit();

            // remember가 체크되어 있으면 이메일과 remember 값은 유지
            if (loginPrefs.getBoolean("remember", false)) {
                String rememberedEmail = loginPrefs.getString("email", "");
                editor.clear(); // 먼저 전체 초기화
                editor.putString("email", rememberedEmail);
                editor.putBoolean("remember", true);
            } else {
                editor.clear(); // remember 체크 안 되어 있으면 전부 삭제
            }

            editor.apply();

            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
