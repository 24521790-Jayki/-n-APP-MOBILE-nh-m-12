package com.example.group12;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Sách đã tải lên
        binding.cardUploadedBooks.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, UploadBookActivity.class);
            startActivity(intent);
        });

        // Thư viện offline / Lưu trữ
        binding.cardOfflineStorage.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, OfflineLibraryActivity.class);
            startActivity(intent);
        });

        // Nút Đăng xuất
        binding.btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finishAffinity();
        });

        // Thanh điều hướng dưới cùng (Bottom Navigation) giữ nguyên vị trí, không trượt trang
        binding.navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, DashboardActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        binding.navLibrary.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LibraryActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        binding.navProfile.setOnClickListener(v -> {
            // Đang ở Cá nhân
        });
    }
}
