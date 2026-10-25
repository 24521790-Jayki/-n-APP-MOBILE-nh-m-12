package com.example.group12;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityQuoteGeneratorBinding;

public class QuoteGeneratorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityQuoteGeneratorBinding binding = ActivityQuoteGeneratorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Lưu ảnh
        binding.btnSaveImage.setOnClickListener(v -> 
            Toast.makeText(this, "Đã lưu ảnh Quote vào thư viện ảnh!", Toast.LENGTH_SHORT).show()
        );

        // Nút Chia sẻ ảnh
        binding.btnShareImage.setOnClickListener(v -> 
            Toast.makeText(this, "Mở ứng dụng chia sẻ ảnh...", Toast.LENGTH_SHORT).show()
        );
    }
}
