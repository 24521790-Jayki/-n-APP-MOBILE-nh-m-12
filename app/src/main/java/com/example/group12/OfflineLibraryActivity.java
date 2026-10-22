package com.example.group12;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityOfflineLibraryBinding;

public class OfflineLibraryActivity extends AppCompatActivity {

    private ActivityOfflineLibraryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOfflineLibraryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Đọc/Nghe sách -> Chuyển đến AudioPlayerActivity
        binding.btnRead1.setOnClickListener(v -> {
            Intent intent = new Intent(OfflineLibraryActivity.this, AudioPlayerActivity.class);
            startActivity(intent);
        });

        binding.btnRead2.setOnClickListener(v -> {
            Intent intent = new Intent(OfflineLibraryActivity.this, AudioPlayerActivity.class);
            startActivity(intent);
        });

        binding.btnRead3.setOnClickListener(v -> {
            Intent intent = new Intent(OfflineLibraryActivity.this, AudioPlayerActivity.class);
            startActivity(intent);
        });
    }
}
