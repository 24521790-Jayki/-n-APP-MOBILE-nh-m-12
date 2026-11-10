package com.example.group12;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityOfflineLibraryBinding;

public class OfflineLibraryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityOfflineLibraryBinding binding = ActivityOfflineLibraryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        // Tính toán % bộ nhớ thiết bị: 850 MB / 19.25 GB (~4.4%)
        long usedMB = 850;
        long totalMB = 19250;
        int percent = (int) ((usedMB * 100) / totalMB);
        binding.progressStorage.setProgress(percent);
        binding.tvStorageInfo.setText("Đã dùng 850 MB / Trống 18.4 GB (" + percent + "%)");

        // Gán sự kiện cho các chip lọc
        binding.chipAll.setOnClickListener(v -> {
            filterBooks("all", binding);
            Toast.makeText(this, "Hiển thị tất cả sách offline", Toast.LENGTH_SHORT).show();
        });

        binding.chipTextBooks.setOnClickListener(v -> {
            filterBooks("text", binding);
            Toast.makeText(this, "Hiển thị Sách chữ", Toast.LENGTH_SHORT).show();
        });

        binding.chipAudioBooks.setOnClickListener(v -> {
            filterBooks("audio", binding);
            Toast.makeText(this, "Hiển thị Sách nói / Audio", Toast.LENGTH_SHORT).show();
        });

        // Nút 3 chấm tùy chọn -> PopupMenu (Xem chi tiết, Báo cáo, Xóa khỏi offline)
        binding.btnMore1.setOnClickListener(v -> showBookPopupMenu(v, binding.cardBookOffline1));
        binding.btnMore2.setOnClickListener(v -> showBookPopupMenu(v, binding.cardBookOffline2));
        binding.btnMore3.setOnClickListener(v -> showBookPopupMenu(v, binding.cardBookOffline3));

        // Xóa cache
        binding.btnClearCache.setOnClickListener(v -> Toast.makeText(this, "Đã xóa bộ nhớ đệm thành công!", Toast.LENGTH_SHORT).show());

        // Nút Đọc/Nghe sách -> Chuyển đến AudioPlayerActivity hoặc ReaderActivity
        binding.btnRead1.setOnClickListener(v -> {
            Intent intent = new Intent(OfflineLibraryActivity.this, ReaderActivity.class);
            startActivity(intent);
        });

        binding.btnRead2.setOnClickListener(v -> {
            Intent intent = new Intent(OfflineLibraryActivity.this, ReaderActivity.class);
            startActivity(intent);
        });

        binding.btnRead3.setOnClickListener(v -> {
            Intent intent = new Intent(OfflineLibraryActivity.this, AudioPlayerActivity.class);
            startActivity(intent);
        });
    }

    private void showBookPopupMenu(View anchorView, View cardView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenu().add("Xem chi tiết sách");
        popup.getMenu().add("Báo cáo vi phạm / lỗi");
        popup.getMenu().add("Xóa khỏi bộ nhớ offline");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle() != null ? item.getTitle().toString() : "";
            if ("Xem chi tiết sách".equals(title)) {
                Intent intent = new Intent(OfflineLibraryActivity.this, BookDetailActivity.class);
                startActivity(intent);
                return true;
            } else if ("Báo cáo vi phạm / lỗi".equals(title)) {
                Intent intent = new Intent(OfflineLibraryActivity.this, ReportActivity.class);
                startActivity(intent);
                return true;
            } else if ("Xóa khỏi bộ nhớ offline".equals(title)) {
                if (cardView != null) {
                    cardView.setVisibility(View.GONE);
                }
                Toast.makeText(this, "Đã xóa sách khỏi bộ nhớ offline!", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void filterBooks(String category, ActivityOfflineLibraryBinding binding) {
        int activeBg = Color.parseColor("#059669");
        int activeText = Color.parseColor("#FFFFFF");
        int inactiveBg = Color.parseColor("#F1F5F9");
        int inactiveText = Color.parseColor("#4B5563");

        // Reset màu tất cả chip
        binding.chipAll.setBackgroundTintList(ColorStateList.valueOf(inactiveBg));
        binding.chipAll.setTextColor(inactiveText);
        binding.chipTextBooks.setBackgroundTintList(ColorStateList.valueOf(inactiveBg));
        binding.chipTextBooks.setTextColor(inactiveText);
        binding.chipAudioBooks.setBackgroundTintList(ColorStateList.valueOf(inactiveBg));
        binding.chipAudioBooks.setTextColor(inactiveText);

        switch (category) {
            case "text":
                binding.chipTextBooks.setBackgroundTintList(ColorStateList.valueOf(activeBg));
                binding.chipTextBooks.setTextColor(activeText);

                binding.cardBookOffline1.setVisibility(View.VISIBLE);
                binding.cardBookOffline2.setVisibility(View.VISIBLE);
                binding.cardBookOffline3.setVisibility(View.GONE);
                break;

            case "audio":
                binding.chipAudioBooks.setBackgroundTintList(ColorStateList.valueOf(activeBg));
                binding.chipAudioBooks.setTextColor(activeText);

                binding.cardBookOffline1.setVisibility(View.VISIBLE);
                binding.cardBookOffline2.setVisibility(View.GONE);
                binding.cardBookOffline3.setVisibility(View.VISIBLE);
                break;

            case "all":
            default:
                binding.chipAll.setBackgroundTintList(ColorStateList.valueOf(activeBg));
                binding.chipAll.setTextColor(activeText);

                binding.cardBookOffline1.setVisibility(View.VISIBLE);
                binding.cardBookOffline2.setVisibility(View.VISIBLE);
                binding.cardBookOffline3.setVisibility(View.VISIBLE);
                break;
        }
    }
}
