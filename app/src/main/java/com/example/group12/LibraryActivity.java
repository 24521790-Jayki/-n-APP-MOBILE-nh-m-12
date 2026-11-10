package com.example.group12;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.group12.databinding.ActivityLibraryBinding;

public class LibraryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityLibraryBinding binding = ActivityLibraryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Nút Lọc (Filter) -> Cài đặt thư viện Pop-up
        binding.btnFilter.setOnClickListener(v -> {
            Intent intent = new Intent(LibraryActivity.this, LibrarySettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Nút Sắp xếp (Sort) -> Sắp xếp thư viện Pop-up
        binding.btnSort.setOnClickListener(v -> {
            Intent intent = new Intent(LibraryActivity.this, LibrarySortActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Nút Tải Sách Mới Lên
        binding.btnUploadBook.setOnClickListener(v -> {
            Intent intent = new Intent(LibraryActivity.this, UploadBookActivity.class);
            startActivity(intent);
        });

        // Link các nút Chip danh mục ở Tủ sách
        binding.btnChipAudio.setOnClickListener(v -> {
            Intent intent = new Intent(LibraryActivity.this, AudioPlayerActivity.class);
            startActivity(intent);
        });

        binding.btnChipOffline.setOnClickListener(v -> {
            Intent intent = new Intent(LibraryActivity.this, OfflineLibraryActivity.class);
            startActivity(intent);
        });

        binding.btnChipCommunity.setOnClickListener(v -> {
            Intent intent = new Intent(LibraryActivity.this, UploadBookActivity.class);
            startActivity(intent);
        });

        // Nút Cuộn lên đầu (^)
        binding.btnScrollTop.setOnClickListener(v -> binding.scrollViewLibrary.smoothScrollTo(0, 0));

        // Lắng nghe sự kiện cuộn để ẩn/hiện nút Upload và nút ScrollTop
        binding.scrollViewLibrary.setOnScrollChangeListener((androidx.core.widget.NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > oldScrollY + 8) {
                // Cuộn xuống -> Ẩn nút
                binding.btnUploadBook.setVisibility(android.view.View.GONE);
                binding.btnScrollTop.setVisibility(android.view.View.GONE);
            } else if (scrollY < oldScrollY - 8) {
                // Cuộn lên -> Hiện nút
                binding.btnUploadBook.setVisibility(android.view.View.VISIBLE);
                binding.btnScrollTop.setVisibility(android.view.View.VISIBLE);
            }
        });

        // Thanh điều hướng dưới cùng
        binding.navHome.setOnClickListener(v -> {
            Intent intent = new Intent(LibraryActivity.this, DashboardActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0); // Giữ thanh nav cố định không trượt
            finish();
        });

        binding.navLibrary.setOnClickListener(v -> {
            // Đang ở Thư viện
        });

        binding.navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(LibraryActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }
}
