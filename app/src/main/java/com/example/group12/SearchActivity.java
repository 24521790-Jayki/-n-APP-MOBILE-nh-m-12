package com.example.group12;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivitySearchBinding;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySearchBinding binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Mở Tìm kiếm Nâng cao
        binding.btnAdvancedSearch.setOnClickListener(v -> {
            Intent intent = new Intent(SearchActivity.this, AdvancedSearchActivity.class);
            startActivity(intent);
        });
    }
}
