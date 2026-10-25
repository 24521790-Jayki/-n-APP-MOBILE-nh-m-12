package com.example.group12;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.group12.databinding.ActivityDashboardBinding;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityDashboardBinding binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Avatar -> Trang cá nhân
        binding.imgAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        // Ô Tìm Kiếm -> Mở SearchActivity
        binding.edtSearch.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SearchActivity.class);
            startActivity(intent);
        });
        binding.edtSearch.setFocusable(false);
        binding.edtSearch.setClickable(true);

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

        // Thẻ Streak & Thống kê -> Mở StreakActivity
        binding.cardStreak.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, StreakActivity.class);
            startActivity(intent);
        });

        // Gợi ý sách AI -> Mở BookDetailActivity
        binding.cardRec1.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, BookDetailActivity.class);
            startActivity(intent);
        });

        binding.cardRec2.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, BookDetailActivity.class);
            startActivity(intent);
        });

        binding.cardRec3.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, BookDetailActivity.class);
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
