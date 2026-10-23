package com.example.group12;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityStreakBinding;

public class StreakActivity extends AppCompatActivity {

    private ActivityStreakBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStreakBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Chia sẻ thành tích
        binding.btnShare.setOnClickListener(v -> 
            Toast.makeText(this, "Chia sẻ thành tích chuỗi đọc sách!", Toast.LENGTH_SHORT).show()
        );

        // Nút Đọc sách ngay để giữ Streak -> Chuyển sang ReaderActivity
        binding.btnReadNow.setOnClickListener(v -> {
            Intent intent = new Intent(StreakActivity.this, ReaderActivity.class);
            startActivity(intent);
        });
    }
}
