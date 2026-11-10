package com.example.group12;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityAdvancedSearchBinding;

public class AdvancedSearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAdvancedSearchBinding binding = ActivityAdvancedSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Đặt lại lọc
        binding.btnReset.setOnClickListener(v -> 
            Toast.makeText(this, "Đã đặt lại bộ lọc", Toast.LENGTH_SHORT).show()
        );

        // Nút Áp dụng bộ lọc
        binding.btnApplyFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Áp dụng bộ lọc thành công!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
