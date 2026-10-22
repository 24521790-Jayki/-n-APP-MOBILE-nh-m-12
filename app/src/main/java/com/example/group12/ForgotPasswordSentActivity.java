package com.example.group12;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityForgotPasswordSentBinding;

public class ForgotPasswordSentActivity extends AppCompatActivity {

    private ActivityForgotPasswordSentBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordSentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Gửi lại email -> Mở lại ForgotPasswordActivity
        binding.btnResendEmail.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordSentActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
            finish();
        });

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnBackToLogin.setOnClickListener(v -> finish());
    }
}
