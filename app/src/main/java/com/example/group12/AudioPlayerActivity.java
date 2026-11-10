package com.example.group12;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityAudioPlayerBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class AudioPlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAudioPlayerBinding binding = ActivityAudioPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Playlist / Danh sách chương -> Hiển thị BottomSheetDialog danh sách chương
        binding.btnPlaylist.setOnClickListener(v -> {
            BottomSheetDialog sheetDialog = new BottomSheetDialog(AudioPlayerActivity.this);
            sheetDialog.setContentView(R.layout.dialog_chapters);
            sheetDialog.show();
        });
    }
}
