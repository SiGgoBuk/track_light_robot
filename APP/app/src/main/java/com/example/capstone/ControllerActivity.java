package com.example.capstone;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstone.api.MotorApi;
import com.example.capstone.model.MotorCommandRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ControllerActivity extends AppCompatActivity {

    private TextView modeText, durationText;
    private long productId = -1;

    private MotorApi motorApi;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable durationUpdater;

    private long baseTime = 0L;
    private float heldSeconds = 0f;
    private String lastDirection = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        // 인텐트에서 productId 받아오기
        productId = getIntent().getLongExtra("productId", -1);
        if (productId == -1) {
            Toast.makeText(this, "제품 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        modeText = findViewById(R.id.modeText);
        durationText = findViewById(R.id.durationText);
        TextView backText = findViewById(R.id.backText);
        Button forwardBtn = findViewById(R.id.forward);
        Button backwardBtn = findViewById(R.id.backward);
        Button leftBtn = findViewById(R.id.left);
        Button rightBtn = findViewById(R.id.right);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://lightproject.duckdns.org/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        motorApi = retrofit.create(MotorApi.class);

        setTouchHandler(forwardBtn, "FORWARD");
        setTouchHandler(backwardBtn, "BACKWARD");
        setTouchHandler(leftBtn, "LEFT");
        setTouchHandler(rightBtn, "RIGHT");

        backText.setOnClickListener(v -> finish());
    }

    private void setTouchHandler(Button button, String direction) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!direction.equals(lastDirection)) {
                        heldSeconds = 0f;
                    }
                    lastDirection = direction;
                    baseTime = SystemClock.elapsedRealtime();
                    modeText.setText("Direction: " + direction);
                    sendCommand(direction);
                    startTimer();
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    stopTimer();
                    break;
            }
            return false;
        });
    }

    private void startTimer() {
        durationUpdater = new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.elapsedRealtime() - baseTime;
                float total = heldSeconds + (elapsed / 1000f);
                durationText.setText(String.format("Holding: %.1fs", total));
                handler.postDelayed(this, 100);
            }
        };
        handler.post(durationUpdater);
    }

    private void stopTimer() {
        handler.removeCallbacks(durationUpdater);
        long elapsed = SystemClock.elapsedRealtime() - baseTime;
        heldSeconds += (elapsed / 1000f);
    }

    private void sendCommand(String direction) {
        MotorCommandRequest request = new MotorCommandRequest(productId, direction);
        motorApi.sendMotorCommand(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(ControllerActivity.this, "전송 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ControllerActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
