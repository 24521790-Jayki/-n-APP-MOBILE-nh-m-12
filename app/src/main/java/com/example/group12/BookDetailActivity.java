package com.example.group12;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityBookDetailBinding;

public class BookDetailActivity extends AppCompatActivity {

    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityBookDetailBinding binding = ActivityBookDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nhận dữ liệu Tên sách & Tác giả từ Intent
        String title = getIntent().getStringExtra("book_title");
        String author = getIntent().getStringExtra("book_author");

        if (title != null && !title.isEmpty()) {
            binding.tvBookTitle.setText(title);
        }
        if (author != null && !author.isEmpty()) {
            binding.tvBookAuthor.setText(author);
        }

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Yêu thích (Tim)
        binding.btnFavorite.setOnClickListener(v -> {
            if (isFavorite) {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                Toast.makeText(this, "Đã bỏ khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                isFavorite = false;
            } else {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite);
                Toast.makeText(this, "Đã thêm vào danh sách yêu thích!", Toast.LENGTH_SHORT).show();
                isFavorite = true;
            }
        });

        // Nút Đọc sách ngay -> Chuyển sang ReaderActivity
        binding.btnReadNow.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailActivity.this, ReaderActivity.class);
            startActivity(intent);
        });

        // Click các chương sách -> Chuyển sang ReaderActivity
        binding.layoutChapter1.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailActivity.this, ReaderActivity.class);
            intent.putExtra("chapter_title", "Chương 1: 11 giờ 22 phút đêm");
            startActivity(intent);
        });

        binding.layoutChapter2.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailActivity.this, ReaderActivity.class);
            intent.putExtra("chapter_title", "Chương 2: Sách sự sống");
            startActivity(intent);
        });

        binding.layoutChapter3.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailActivity.this, ReaderActivity.class);
            intent.putExtra("chapter_title", "Chương 3: Bãi đỗ xe của thư viện");
            startActivity(intent);
        });

        // Nút Nghe Audio -> Chuyển sang AudioPlayerActivity
        binding.btnListenAudio.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailActivity.this, AudioPlayerActivity.class);
            startActivity(intent);
        });
    }
}
