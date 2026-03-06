package com.example.capstone;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstone.api.ProductSettingApi;
import com.example.capstone.model.ProductSettingDto;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SettingActivity extends AppCompatActivity {

    private SeekBar motorSpeedInput, ultrasonicDistanceInput;
    private SeekBar redSeekBar, greenSeekBar, blueSeekBar;
    private CheckBox irLightCheckbox;
    private View colorPreview;
    private Button saveButton;
    private TextView homeText;

    private TextView motorSpeedValue, ultrasonicDistanceValue;
    private TextView redValue, greenValue, blueValue;

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "deviceSettings";

    private long productId = -1;
    private ProductSettingApi settingApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // productId 받기
        productId = getIntent().getLongExtra("productId", -1);
        if (productId == -1) {
            Toast.makeText(this, "제품 ID가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        motorSpeedInput = findViewById(R.id.motorSpeedInput);
        ultrasonicDistanceInput = findViewById(R.id.ultrasonicDistanceInput);
        redSeekBar = findViewById(R.id.redSeekBar);
        greenSeekBar = findViewById(R.id.greenSeekBar);
        blueSeekBar = findViewById(R.id.blueSeekBar);
        irLightCheckbox = findViewById(R.id.irLightCheckbox);
        colorPreview = findViewById(R.id.colorPreview);
        saveButton = findViewById(R.id.saveButton);
        homeText = findViewById(R.id.homeText);

        motorSpeedValue = findViewById(R.id.motorSpeedValue);
        ultrasonicDistanceValue = findViewById(R.id.ultrasonicDistanceValue);
        redValue = findViewById(R.id.redValue);
        greenValue = findViewById(R.id.greenValue);
        blueValue = findViewById(R.id.blueValue);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://lightproject.duckdns.org/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        settingApi = retrofit.create(ProductSettingApi.class);

        SeekBar.OnSeekBarChangeListener updateListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar == motorSpeedInput) motorSpeedValue.setText(String.valueOf(progress));
                else if (seekBar == ultrasonicDistanceInput) ultrasonicDistanceValue.setText(progress + "cm");
                else if (seekBar == redSeekBar) redValue.setText(String.valueOf(progress));
                else if (seekBar == greenSeekBar) greenValue.setText(String.valueOf(progress));
                else if (seekBar == blueSeekBar) blueValue.setText(String.valueOf(progress));
                updateColorPreview();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        };

        motorSpeedInput.setOnSeekBarChangeListener(updateListener);
        ultrasonicDistanceInput.setOnSeekBarChangeListener(updateListener);
        redSeekBar.setOnSeekBarChangeListener(updateListener);
        greenSeekBar.setOnSeekBarChangeListener(updateListener);
        blueSeekBar.setOnSeekBarChangeListener(updateListener);

        saveButton.setOnClickListener(v -> saveSettings());
        homeText.setOnClickListener(v -> finish());

        loadSettings();
    }

    private void updateColorPreview() {
        int r = redSeekBar.getProgress();
        int g = greenSeekBar.getProgress();
        int b = blueSeekBar.getProgress();
        colorPreview.setBackgroundColor(Color.rgb(r, g, b));
    }

    private void saveSettings() {
        List<ProductSettingDto> settings = Arrays.asList(
                new ProductSettingDto(productId, "motorSpeed", String.valueOf(motorSpeedInput.getProgress())),
                new ProductSettingDto(productId, "ultrasonicThresholdCm", String.valueOf(ultrasonicDistanceInput.getProgress())),
                new ProductSettingDto(productId, "irLightAutoOn", String.valueOf(irLightCheckbox.isChecked())),
                new ProductSettingDto(productId, "led_r", String.valueOf(redSeekBar.getProgress())),
                new ProductSettingDto(productId, "led_g", String.valueOf(greenSeekBar.getProgress())),
                new ProductSettingDto(productId, "led_b", String.valueOf(blueSeekBar.getProgress()))
        );

        for (ProductSettingDto dto : settings) {
            settingApi.saveOrUpdate(dto).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(SettingActivity.this, "저장 실패: " + dto.getSettingKey(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(SettingActivity.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        Toast.makeText(this, "설정이 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void loadSettings() {
        settingApi.getSettings(productId).enqueue(new Callback<List<ProductSettingDto>>() {
            @Override
            public void onResponse(Call<List<ProductSettingDto>> call, Response<List<ProductSettingDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (ProductSettingDto dto : response.body()) {
                        switch (dto.getSettingKey()) {
                            case "motorSpeed": motorSpeedInput.setProgress(Integer.parseInt(dto.getSettingValue())); break;
                            case "ultrasonicThresholdCm": ultrasonicDistanceInput.setProgress(Integer.parseInt(dto.getSettingValue())); break;
                            case "irLightAutoOn": irLightCheckbox.setChecked(Boolean.parseBoolean(dto.getSettingValue())); break;
                            case "led_r": redSeekBar.setProgress(Integer.parseInt(dto.getSettingValue())); break;
                            case "led_g": greenSeekBar.setProgress(Integer.parseInt(dto.getSettingValue())); break;
                            case "led_b": blueSeekBar.setProgress(Integer.parseInt(dto.getSettingValue())); break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ProductSettingDto>> call, Throwable t) {
                Toast.makeText(SettingActivity.this, "설정 불러오기 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
