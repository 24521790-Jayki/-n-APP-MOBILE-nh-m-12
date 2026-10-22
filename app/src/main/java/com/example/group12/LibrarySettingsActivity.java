package com.example.group12;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityLibrarySettingsBinding;

public class LibrarySettingsActivity extends AppCompatActivity {

    private ActivityLibrarySettingsBinding binding;
    private boolean isGridMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = ActivityLibrarySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnCloseSettings.setOnClickListener(v -> finish());
        binding.btnSaveSettings.setOnClickListener(v -> finish());

        // Chuyển đổi Grid vs List mode
        binding.btnGridMode.setOnClickListener(v -> {
            isGridMode = true;
            updateDisplayModeUI();
        });

        binding.btnListMode.setOnClickListener(v -> {
            isGridMode = false;
            updateDisplayModeUI();
        });
    }

    private void updateDisplayModeUI() {
        if (isGridMode) {
            binding.btnGridMode.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2563EB")));
            binding.btnGridMode.setTextColor(Color.parseColor("#FFFFFF"));
            binding.btnGridMode.setIconTint(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));

            binding.btnListMode.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            binding.btnListMode.setTextColor(Color.parseColor("#4B5563"));
            binding.btnListMode.setIconTint(ColorStateList.valueOf(Color.parseColor("#4B5563")));
        } else {
            binding.btnListMode.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2563EB")));
            binding.btnListMode.setTextColor(Color.parseColor("#FFFFFF"));
            binding.btnListMode.setIconTint(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));

            binding.btnGridMode.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            binding.btnGridMode.setTextColor(Color.parseColor("#4B5563"));
            binding.btnGridMode.setIconTint(ColorStateList.valueOf(Color.parseColor("#4B5563")));
        }
    }
}
