package com.example.capstone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstone.api.UserApi;
import com.example.capstone.model.ApiResponse;
import com.example.capstone.model.LoginRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private RadioButton loginTab, registerTab;
    private TextView findTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // UI 연결
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        loginTab = findViewById(R.id.loginTab);
        registerTab = findViewById(R.id.registerTab);
        findTextView = findViewById(R.id.findTextView);

        loginButton.setOnClickListener(v -> {
            String inputEmail = emailInput.getText().toString().trim();
            String inputPassword = passwordInput.getText().toString().trim();

            if (inputEmail.isEmpty() || inputPassword.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            LoginRequest request = new LoginRequest(inputEmail, inputPassword);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://lightproject.duckdns.org/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            UserApi userApi = retrofit.create(UserApi.class);

            userApi.login(request).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse res = response.body();
                        if (res.isSuccess()) {
                            Long userId = res.getData();

                            // 사용자 정보 저장
                            SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
                            prefs.edit()
                                    .putLong("userId", userId)
                                    .putString("username", inputEmail)
                                    .putString("firstName", "사용자") // 임시값. 서버에서 받으면 교체 가능
                                    .apply();

                            Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();

                            // 제품 선택 화면으로 이동
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.putExtra("userId", userId); // 전달 (사실상 없어도 SharedPreferences 사용 가능)
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(LoginActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "서버 응답 오류", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        registerTab.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        findTextView.setOnClickListener(v -> {
            Toast.makeText(this, "아이디/비밀번호 찾기 기능은 준비 중입니다.", Toast.LENGTH_SHORT).show();
        });
    }
}
