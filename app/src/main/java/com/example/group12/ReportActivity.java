package com.example.group12;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityReportBinding;

public class ReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityReportBinding binding = ActivityReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Gửi Báo cáo
        binding.btnSubmitReport.setOnClickListener(v -> {
            Toast.makeText(this, "Cảm ơn bạn đã gửi báo cáo vi phạm!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
