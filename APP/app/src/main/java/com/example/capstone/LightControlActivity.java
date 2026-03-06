package com.example.capstone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LightControlActivity extends AppCompatActivity {

    private Switch lightSwitch;
    private TextView brightnessPercent;
    private ImageView lampImage;
    private SeekBar brightnessSeekBar;
    private Button colorPickerButton;
    private TextView homeText;

    private boolean isLightOn = true;
    private int currentBrightness = 90;
    private int savedBrightness = 90;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_control);

        // UI 연결
        lightSwitch = findViewById(R.id.lightSwitch);
        brightnessPercent = findViewById(R.id.brightnessPercent);
        lampImage = findViewById(R.id.lampImage);
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar);
        colorPickerButton = findViewById(R.id.colorPickerButton);
        homeText = findViewById(R.id.homeText);

        // 저장된 상태 불러오기
        loadState();

        // UI 초기화
        lightSwitch.setChecked(isLightOn);
        updateUI();

        // 스위치 상태 변경
        lightSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isLightOn = isChecked;

            if (!isLightOn) {
                savedBrightness = currentBrightness;
                currentBrightness = 0;
            } else {
                currentBrightness = savedBrightness;
            }

            updateUI();
            saveState();
        });

        // 밝기 변경
        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isLightOn) {
                    currentBrightness = progress;
                    savedBrightness = progress;
                    updateUI();
                    saveState();
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // 컬러 선택 페이지 이동
        colorPickerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ColorPickerActivity.class);
            startActivity(intent);
        });

        // 홈 화면 이동
        homeText.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        });
    }

    private void updateUI() {
        brightnessPercent.setText(currentBrightness + "%");
        brightnessSeekBar.setProgress(currentBrightness);

        if (isLightOn) {
            float alpha = Math.max(0.2f, currentBrightness / 100f);
            lampImage.setAlpha(alpha);
        } else {
            lampImage.setAlpha(0.1f);
        }
    }

    private void saveState() {
        SharedPreferences prefs = getSharedPreferences("light_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("light_on", isLightOn);
        editor.putInt("brightness", savedBrightness);
        editor.apply();
    }

    private void loadState() {
        SharedPreferences prefs = getSharedPreferences("light_prefs", MODE_PRIVATE);
        isLightOn = prefs.getBoolean("light_on", true);
        savedBrightness = prefs.getInt("brightness", 90);
        currentBrightness = isLightOn ? savedBrightness : 0;
    }
}
