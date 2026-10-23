package com.example.group12;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityReplyReviewBinding;

public class ReplyReviewActivity extends AppCompatActivity {

    private ActivityReplyReviewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReplyReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Gửi phản hồi
        binding.btnSubmitReply.setOnClickListener(v -> {
            Toast.makeText(this, "Đã gửi phản hồi đánh giá!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
