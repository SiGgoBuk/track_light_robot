package com.example.capstone;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstone.api.LedScheduleApi;
import com.example.capstone.model.ApiResponse;
import com.example.capstone.model.ScheduleRequest;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ScheduleActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TimePicker timePicker;
    private Spinner lightStatusSpinner;
    private Button saveScheduleButton;
    private TextView homeText;
    private long selectedDate = 0;
    private long productId = -1;
    private String productName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        // 전달받은 productId 및 productName 확인
        productId = getIntent().getLongExtra("productId", -1);
        productName = getIntent().getStringExtra("productName");

        if (productId == -1 || productName == null) {
            Toast.makeText(this, "제품 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        calendarView = findViewById(R.id.calendarView);
        timePicker = findViewById(R.id.timePicker);
        lightStatusSpinner = findViewById(R.id.lightStatusSpinner);
        saveScheduleButton = findViewById(R.id.saveScheduleButton);
        homeText = findViewById(R.id.homeText);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDate = calendar.getTimeInMillis();
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.light_status, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lightStatusSpinner.setAdapter(adapter);

        saveScheduleButton.setOnClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String status = lightStatusSpinner.getSelectedItem().toString();

            if (selectedDate == 0) {
                selectedDate = calendarView.getDate();
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selectedDate);

            String scheduledTime = String.format(Locale.KOREA, "%04d-%02d-%02dT%02d:%02d",
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH),
                    hour,
                    minute);

            int r, g, b;
            if ("ON".equalsIgnoreCase(status)) {
                r = g = b = 255;
            } else {
                r = g = b = 0;
            }

            ScheduleRequest request = new ScheduleRequest(productId, scheduledTime, r, g, b);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://lightproject.duckdns.org/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            LedScheduleApi api = retrofit.create(LedScheduleApi.class);
            api.createSchedule(request).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(ScheduleActivity.this, "일정 등록 완료", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ScheduleActivity.this, MyPageActivity.class);
                        intent.putExtra("productId", productId);
                        intent.putExtra("productName", productName);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ScheduleActivity.this, "서버 오류: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(ScheduleActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        homeText.setOnClickListener(v -> {
            Intent intent = new Intent(ScheduleActivity.this, ControlHomeActivity.class);
            intent.putExtra("productId", productId);
            intent.putExtra("productName", productName);
            startActivity(intent);
            finish();
        });
    }
}
