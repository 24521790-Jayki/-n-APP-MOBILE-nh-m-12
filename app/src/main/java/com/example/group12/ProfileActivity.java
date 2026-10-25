package com.example.group12;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.group12.databinding.ActivityProfileBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityProfileBinding binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

        // Bộ sưu tập ảnh Quote
        binding.cardQuotes.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, QuoteGeneratorActivity.class);
            startActivity(intent);
        });

        // Cài đặt chế độ đọc -> Mở BottomSheetDialog dialog_reading_settings
        binding.cardSettings.setOnClickListener(v -> {
            BottomSheetDialog sheetDialog = new BottomSheetDialog(ProfileActivity.this);
            View view = LayoutInflater.from(ProfileActivity.this).inflate(R.layout.dialog_reading_settings, null);
            sheetDialog.setContentView(view);
            sheetDialog.show();
        });

        // Hỗ trợ & Đóng góp
        binding.cardSupport.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ReportActivity.class);
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
