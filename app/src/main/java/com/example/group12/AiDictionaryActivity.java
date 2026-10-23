package com.example.group12;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityAiDictionaryBinding;

public class AiDictionaryActivity extends AppCompatActivity {

    private ActivityAiDictionaryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAiDictionaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Đóng
        binding.btnClose.setOnClickListener(v -> finish());
    }
}
