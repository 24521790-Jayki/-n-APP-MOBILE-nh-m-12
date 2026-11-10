package com.example.group12;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.group12.databinding.ActivityForgotPasswordSentBinding;

public class ForgotPasswordSentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityForgotPasswordSentBinding binding = ActivityForgotPasswordSentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.contentLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                v.getPaddingLeft(),
                systemBars.top + (int) (16 * getResources().getDisplayMetrics().density),
                v.getPaddingRight(),
                systemBars.bottom + (int) (24 * getResources().getDisplayMetrics().density)
            );
            return insets;
        });

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
