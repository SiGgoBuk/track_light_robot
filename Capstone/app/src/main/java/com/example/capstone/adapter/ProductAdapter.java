package com.example.capstone.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstone.R;
import com.example.capstone.model.ProductDto;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    public interface OnProductClickListener {
        void onClick(ProductDto product);
    }

    private final List<ProductDto> products;
    private final OnProductClickListener listener;

    public ProductAdapter(List<ProductDto> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(products.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameView, numberView;

        ViewHolder(View view) {
            super(view);
            nameView = view.findViewById(R.id.productName);
            numberView = view.findViewById(R.id.productNumber);
        }

        void bind(ProductDto product, OnProductClickListener listener) {
            nameView.setText(product.getName());
            numberView.setText("제품 번호: " + product.getProductNumber());
            itemView.setOnClickListener(v -> listener.onClick(product));
        }
    }
}

