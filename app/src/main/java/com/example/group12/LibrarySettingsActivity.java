package com.example.group12;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityLibrarySettingsBinding;

public class LibrarySettingsActivity extends AppCompatActivity {

    public static final String PREF_NAME = "library_prefs";
    public static final String KEY_DISPLAY_MODE = "pref_display_mode";
    public static final String KEY_SHOW_PROGRESS = "pref_show_progress";
    public static final String KEY_SHOW_AUDIO = "pref_show_audio";
    public static final String KEY_WIFI_ONLY = "pref_wifi_only";

    private ActivityLibrarySettingsBinding binding;
    private boolean isGridMode = true;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = ActivityLibrarySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Load saved preferences
        String savedMode = preferences.getString(KEY_DISPLAY_MODE, "grid");
        isGridMode = !"list".equalsIgnoreCase(savedMode);

        binding.switchShowProgress.setChecked(preferences.getBoolean(KEY_SHOW_PROGRESS, true));
        binding.switchShowAudioBadge.setChecked(preferences.getBoolean(KEY_SHOW_AUDIO, true));
        binding.switchWifiOnly.setChecked(preferences.getBoolean(KEY_WIFI_ONLY, false));

        updateDisplayModeUI();

        binding.btnCloseSettings.setOnClickListener(v -> finish());

        // Chuyển đổi Grid vs List mode
        binding.btnGridMode.setOnClickListener(v -> {
            isGridMode = true;
            updateDisplayModeUI();
        });

        binding.btnListMode.setOnClickListener(v -> {
            isGridMode = false;
            updateDisplayModeUI();
        });

        binding.btnSaveSettings.setOnClickListener(v -> {
            preferences.edit()
                    .putString(KEY_DISPLAY_MODE, isGridMode ? "grid" : "list")
                    .putBoolean(KEY_SHOW_PROGRESS, binding.switchShowProgress.isChecked())
                    .putBoolean(KEY_SHOW_AUDIO, binding.switchShowAudioBadge.isChecked())
                    .putBoolean(KEY_WIFI_ONLY, binding.switchWifiOnly.isChecked())
                    .apply();

            Toast.makeText(this, "Đã lưu cài đặt thư viện thành công!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });
    }

    private void updateDisplayModeUI() {
        int blueColor = Color.parseColor("#2563EB");
        int grayColor = Color.parseColor("#4B5563");
        int whiteColor = Color.parseColor("#FFFFFF");

        if (isGridMode) {
            binding.btnGridMode.setBackgroundTintList(ColorStateList.valueOf(blueColor));
            binding.btnGridMode.setTextColor(whiteColor);
            binding.btnGridMode.setIconTint(ColorStateList.valueOf(whiteColor));

            binding.btnListMode.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            binding.btnListMode.setTextColor(grayColor);
            binding.btnListMode.setIconTint(ColorStateList.valueOf(grayColor));
        } else {
            binding.btnListMode.setBackgroundTintList(ColorStateList.valueOf(blueColor));
            binding.btnListMode.setTextColor(whiteColor);
            binding.btnListMode.setIconTint(ColorStateList.valueOf(whiteColor));

            binding.btnGridMode.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            binding.btnGridMode.setTextColor(grayColor);
            binding.btnGridMode.setIconTint(ColorStateList.valueOf(grayColor));
        }
    }
}
