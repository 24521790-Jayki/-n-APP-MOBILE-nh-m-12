package com.example.group12;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityAiChatBinding;

public class AiChatActivity extends AppCompatActivity {

    private ActivityAiChatBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAiChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Gửi tin nhắn AI
        binding.btnSend.setOnClickListener(v -> {
            String msg = binding.edtMessage.getText() != null ? binding.edtMessage.getText().toString().trim() : "";
            if (!msg.isEmpty()) {
                binding.edtMessage.setText("");
                Toast.makeText(this, "AI đang suy nghĩ câu trả lời...", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
