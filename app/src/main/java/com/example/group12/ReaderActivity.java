package com.example.group12;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityReaderBinding;

public class ReaderActivity extends AppCompatActivity {

    private ActivityReaderBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReaderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Mặc định ban đầu là chế độ Bảo vệ mắt (Sepia)
        applyTheme("sepia");

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Mở Sách Nói
        binding.btnAudio.setOnClickListener(v -> {
            Intent intent = new Intent(ReaderActivity.this, AudioPlayerActivity.class);
            startActivity(intent);
        });

        // Nút Aa - Tắt / Mở cài đặt Font chữ & Theme
        binding.btnAa.setOnClickListener(v -> {
            if (binding.cardThemeSettings != null) {
                int visibility = binding.cardThemeSettings.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
                binding.cardThemeSettings.setVisibility(visibility);
            } else {
                Toast.makeText(this, "Mở cài đặt chế độ đọc", Toast.LENGTH_SHORT).show();
            }
        });

        if (binding.btnCloseSettings != null) {
            binding.btnCloseSettings.setOnClickListener(v -> {
                if (binding.cardThemeSettings != null) {
                    binding.cardThemeSettings.setVisibility(View.GONE);
                }
            });
        }

        // Chuyển trang
        binding.btnPrevPage.setOnClickListener(v -> 
            Toast.makeText(this, "Trang trước", Toast.LENGTH_SHORT).show()
        );

        binding.btnNextPage.setOnClickListener(v -> 
            Toast.makeText(this, "Trang tiếp theo", Toast.LENGTH_SHORT).show()
        );

        // Chuyển chế độ đọc / theme
        binding.themeLight.setOnClickListener(v -> {
            applyTheme("light");
            Toast.makeText(this, "Đã chuyển sang giao diện Sáng (Light)", Toast.LENGTH_SHORT).show();
            binding.cardThemeSettings.setVisibility(View.GONE);
        });

        binding.themeDark.setOnClickListener(v -> {
            applyTheme("dark");
            Toast.makeText(this, "Đã chuyển sang giao diện Tối (Dark)", Toast.LENGTH_SHORT).show();
            binding.cardThemeSettings.setVisibility(View.GONE);
        });

        binding.themeSepia.setOnClickListener(v -> {
            applyTheme("sepia");
            Toast.makeText(this, "Đã chuyển sang giao diện Bảo vệ mắt (Sepia)", Toast.LENGTH_SHORT).show();
            binding.cardThemeSettings.setVisibility(View.GONE);
        });

        binding.themeSystem.setOnClickListener(v -> {
            applyTheme("system");
            Toast.makeText(this, "Đã chuyển theo giao diện Hệ thống", Toast.LENGTH_SHORT).show();
            binding.cardThemeSettings.setVisibility(View.GONE);
        });
    }

    private void applyTheme(String themeKey) {
        if (binding == null) return;
        switch (themeKey) {
            case "light":
                binding.readerRootContainer.setBackgroundColor(Color.parseColor("#FFFFFF"));
                break;
            case "dark":
                binding.readerRootContainer.setBackgroundColor(Color.parseColor("#1F2937"));
                break;
            case "sepia":
                binding.readerRootContainer.setBackgroundColor(Color.parseColor("#FDF6E2"));
                break;
            case "system":
            default:
                binding.readerRootContainer.setBackgroundColor(Color.parseColor("#F8FAFC"));
                break;
        }
    }
}
