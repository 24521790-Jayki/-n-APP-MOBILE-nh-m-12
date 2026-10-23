package com.example.group12;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityReviewRepliesBinding;

public class ReviewRepliesActivity extends AppCompatActivity {

    private ActivityReviewRepliesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReviewRepliesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Gửi phản hồi
        binding.btnSendReply.setOnClickListener(v -> {
            String text = binding.etReplyInput.getText() != null ? binding.etReplyInput.getText().toString().trim() : "";
            if (!text.isEmpty()) {
                binding.etReplyInput.setText("");
                Toast.makeText(this, "Đã gửi bình luận!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
