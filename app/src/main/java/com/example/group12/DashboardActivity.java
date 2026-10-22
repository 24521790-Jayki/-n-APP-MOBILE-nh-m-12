package com.example.group12;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityDashboardBinding;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Avatar -> Trang cá nhân
        binding.imgAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        // Thẻ Nghe Âm Thanh / Sách Nói
        binding.cardAudio.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AudioPlayerActivity.class);
            startActivity(intent);
        });

        // Thẻ Tải Sách Lên
        binding.cardUpload.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, UploadBookActivity.class);
            startActivity(intent);
        });

        // Thẻ Thư Viện Ngoại Tuyến (Offline)
        binding.cardOffline.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, OfflineLibraryActivity.class);
            startActivity(intent);
        });

        // Thanh điều hướng dưới cùng (Bottom Navigation)
        binding.navHome.setOnClickListener(v -> {
            // Đang ở Trang chủ
        });

        binding.navLibrary.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, LibraryActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        binding.navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }
}
