package com.example.capstone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.capstone.api.ProductApi;
import com.example.capstone.model.ProductDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductListActivity extends AppCompatActivity {

    private LinearLayout productContainer;
    private ProductApi productApi;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        // 로그인 정보에서 userId 가져오기
        SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
        this.userId = prefs.getLong("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        productContainer = findViewById(R.id.productContainer);

        TextView goHomeTextView = findViewById(R.id.homeText);
        goHomeTextView.setOnClickListener(v -> {
            Intent intent = new Intent(ProductListActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://lightproject.duckdns.org")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        productApi = retrofit.create(ProductApi.class);
        loadProductList();
    }

    private void loadProductList() {
        productApi.getProductsByUserId(userId).enqueue(new Callback<List<ProductDto>>() {
            @Override
            public void onResponse(Call<List<ProductDto>> call, Response<List<ProductDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (ProductDto product : response.body()) {
                        addProductCard(product);
                    }
                } else {
                    Toast.makeText(ProductListActivity.this, "불러오기 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ProductDto>> call, Throwable t) {
                Toast.makeText(ProductListActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addProductCard(ProductDto product) {
        // 카드뷰 생성
        CardView cardView = new CardView(this);
        cardView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        cardView.setRadius(16);
        cardView.setCardElevation(8);
        cardView.setUseCompatPadding(true);
        cardView.setContentPadding(24, 24, 24, 24);

        // 내부 텍스트 레이아웃 생성
        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);

        TextView nameView = new TextView(this);
        nameView.setText(product.getName());
        nameView.setTextSize(20);
        nameView.setTextColor(Color.BLACK);
        nameView.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView numberView = new TextView(this);
        numberView.setText("Product Number: " + product.getProductNumber());
        numberView.setTextColor(Color.DKGRAY);

        TextView dateView = new TextView(this);
        dateView.setText("Registered: " + product.getRegisteredAt());
        dateView.setTextColor(Color.DKGRAY);

        innerLayout.addView(nameView);
        innerLayout.addView(numberView);
        innerLayout.addView(dateView);

        cardView.addView(innerLayout);
        cardView.setOnClickListener(v -> {
            Intent intent = new Intent(this, ControlHomeActivity.class);
            intent.putExtra("productId", product.getId());  // productId 전달
            intent.putExtra("productName", product.getName());  // 필요 시 추가 정보 전달
            startActivity(intent);
        });

        // 간격 추가
        LinearLayout.LayoutParams marginParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        marginParams.setMargins(0, 0, 0, 32);
        cardView.setLayoutParams(marginParams);

        productContainer.addView(cardView);
    }
}
