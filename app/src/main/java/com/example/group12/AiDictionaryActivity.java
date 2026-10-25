package com.example.group12;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityAiDictionaryBinding;

public class AiDictionaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAiDictionaryBinding binding = ActivityAiDictionaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Hiển thị văn bản được chọn từ ReaderActivity
        if (getIntent() != null && getIntent().hasExtra("selected_text")) {
            String text = getIntent().getStringExtra("selected_text");
            if (text != null && !text.isEmpty()) {
                binding.tvSelectedText.setText(text);
            }
        }

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Đóng
        binding.btnClose.setOnClickListener(v -> finish());
    }
}
