package com.example.capstone;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstone.api.LightControlApi;
import com.example.capstone.model.LightColorRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ColorPickerActivity extends AppCompatActivity {

    private SeekBar seekBarRed, seekBarGreen, seekBarBlue;
    private View colorPreview;
    private ImageView imageView;
    private int red = 255, green = 255, blue = 255;

    private long productId = -1;
    private LightControlApi lightControlApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        // 🔹 Intent에서 productId 받아오기
        productId = getIntent().getLongExtra("productId", -1);
        if (productId == -1) {
            Toast.makeText(this, "제품 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        seekBarRed = findViewById(R.id.seekBarRed);
        seekBarGreen = findViewById(R.id.seekBarGreen);
        seekBarBlue = findViewById(R.id.seekBarBlue);
        colorPreview = findViewById(R.id.colorPreview);
        imageView = findViewById(R.id.imageView6);
        TextView homeText = findViewById(R.id.homeText);
        Button applyColorButton = findViewById(R.id.applyColorButton);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://lightproject.duckdns.org/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        lightControlApi = retrofit.create(LightControlApi.class);

        SeekBar.OnSeekBarChangeListener colorChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                red = seekBarRed.getProgress();
                green = seekBarGreen.getProgress();
                blue = seekBarBlue.getProgress();
                updateColorPreview();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        };

        seekBarRed.setOnSeekBarChangeListener(colorChangeListener);
        seekBarGreen.setOnSeekBarChangeListener(colorChangeListener);
        seekBarBlue.setOnSeekBarChangeListener(colorChangeListener);

        applyColorButton.setOnClickListener(view -> {
            int color = Color.rgb(red, green, blue);
            imageView.setColorFilter(color);

            LightColorRequest request = new LightColorRequest(productId, red, green, blue);
            lightControlApi.sendColor(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ColorPickerActivity.this, "색상 전송 성공", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ColorPickerActivity.this, "전송 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(ColorPickerActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        updateColorPreview();

        homeText.setOnClickListener(view -> {
            Intent intent = new Intent(ColorPickerActivity.this, LightModeSelectActivity.class);
            intent.putExtra("productId", productId); // 🔹 되돌아갈 때도 전달
            startActivity(intent);
            finish();
        });
    }

    private void updateColorPreview() {
        int color = Color.rgb(red, green, blue);
        colorPreview.setBackgroundColor(color);
    }
}
