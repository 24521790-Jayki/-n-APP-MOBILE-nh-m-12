package com.example.group12;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityBookDetailBinding;

public class BookDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityBookDetailBinding binding = ActivityBookDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Yêu thích
        binding.btnFavorite.setOnClickListener(v -> 
            Toast.makeText(this, "Đã thêm vào danh sách yêu thích!", Toast.LENGTH_SHORT).show()
        );

        // Nút Đọc sách ngay -> Chuyển sang ReaderActivity
        binding.btnReadNow.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailActivity.this, ReaderActivity.class);
            startActivity(intent);
        });

        // Nút Nghe Audio -> Chuyển sang AudioPlayerActivity
        binding.btnListenAudio.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailActivity.this, AudioPlayerActivity.class);
            startActivity(intent);
        });
    }
}
