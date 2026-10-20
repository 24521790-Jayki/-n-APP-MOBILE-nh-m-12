package com.example.group12;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityAudioPlayerBinding;

public class AudioPlayerActivity extends AppCompatActivity {

    private ActivityAudioPlayerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAudioPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());
    }
}
