package com.example.group12;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityBookReviewsBinding;

public class BookReviewsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityBookReviewsBinding binding = ActivityBookReviewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Viết đánh giá / Phản hồi -> Chuyển sang ReviewRepliesActivity
        binding.btnWriteReview.setOnClickListener(v -> {
            Intent intent = new Intent(BookReviewsActivity.this, ReviewRepliesActivity.class);
            startActivity(intent);
        });
    }
}
