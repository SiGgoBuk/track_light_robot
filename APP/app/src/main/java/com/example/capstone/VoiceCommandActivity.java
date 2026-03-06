package com.example.capstone;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstone.api.VoiceCommandApi;
import com.example.capstone.model.VoiceCommandRequest;
import com.example.capstone.model.VoiceCommandResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class VoiceCommandActivity extends AppCompatActivity {

    private EditText commandInput;
    private Spinner functionSpinner;
    private Button registerButton;
    private RecyclerView commandRecyclerView;
    private VoiceCommandAdapter adapter;
    private List<VoiceCommandResponse> commandList = new ArrayList<>();

    private long productId;

    private final Map<String, Long> functionToActionIdMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_command);

        productId = getIntent().getLongExtra("productId", -1);

        commandInput = findViewById(R.id.commandEditText);
        functionSpinner = findViewById(R.id.functionSpinner);
        registerButton = findViewById(R.id.registerButton);
        commandRecyclerView = findViewById(R.id.voiceCommandRecyclerView);
        TextView homeText = findViewById(R.id.homeText);

        commandRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new VoiceCommandAdapter(commandList, position -> {
            long commandId = commandList.get(position).getId();
            deleteCommand(commandId);
        });
        commandRecyclerView.setAdapter(adapter);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.command_functions, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        functionSpinner.setAdapter(spinnerAdapter);

        setupFunctionToActionIdMap();

        registerButton.setOnClickListener(v -> {
            String commandText = commandInput.getText().toString().trim();
            String function = functionSpinner.getSelectedItem().toString();

            if (commandText.isEmpty()) {
                Toast.makeText(this, "명령어를 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            Long actionId = functionToActionIdMap.get(function);
            if (actionId == null) {
                Toast.makeText(this, "알 수 없는 기능입니다", Toast.LENGTH_SHORT).show();
                return;
            }

            registerCommand(commandText, actionId);
        });

        loadCommands();

        homeText.setOnClickListener(v -> finish());
    }

    private void setupFunctionToActionIdMap() {
        functionToActionIdMap.put("켜기", 1L);
        functionToActionIdMap.put("끄기", 2L);
        functionToActionIdMap.put("밝기 증가", 3L);
        functionToActionIdMap.put("밝기 감소", 4L);
        functionToActionIdMap.put("추적", 5L);
        functionToActionIdMap.put("정지", 6L);
    }

    private void registerCommand(String commandText, long actionId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://lightproject.duckdns.org/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        VoiceCommandApi api = retrofit.create(VoiceCommandApi.class);

        VoiceCommandRequest request = new VoiceCommandRequest(productId, actionId, commandText);
        api.registerCommand(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(VoiceCommandActivity.this, "등록 성공", Toast.LENGTH_SHORT).show();
                    commandInput.setText("");
                    loadCommands();
                } else {
                    Toast.makeText(VoiceCommandActivity.this, "등록 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(VoiceCommandActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCommands() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://lightproject.duckdns.org/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        VoiceCommandApi api = retrofit.create(VoiceCommandApi.class);

        api.getCommands(productId).enqueue(new Callback<List<VoiceCommandResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<VoiceCommandResponse>> call, @NonNull Response<List<VoiceCommandResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    commandList.clear();
                    commandList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<VoiceCommandResponse>> call, @NonNull Throwable t) {
                Toast.makeText(VoiceCommandActivity.this, "불러오기 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteCommand(long commandId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://lightproject.duckdns.org/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        VoiceCommandApi api = retrofit.create(VoiceCommandApi.class);

        api.deleteCommand(commandId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(VoiceCommandActivity.this, "삭제 완료", Toast.LENGTH_SHORT).show();
                    loadCommands();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(VoiceCommandActivity.this, "삭제 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
