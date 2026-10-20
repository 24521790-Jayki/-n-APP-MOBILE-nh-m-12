package com.example.group12;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo Cloudflare Turnstile Captcha
        TurnstileHelper.setupTurnstile(binding.wvTurnstile, TurnstileHelper.DEMO_SITE_KEY, new TurnstileHelper.TurnstileListener() {
            @Override
            public void onVerified(String token) {
                binding.cbHCaptcha.setChecked(true);
                binding.tvHCaptchaText.setText("Xác minh Cloudflare Turnstile thành công!");
            }

            @Override
            public void onExpired() {
                binding.cbHCaptcha.setChecked(false);
                binding.tvHCaptchaText.setText("Xác minh Cloudflare Turnstile đã hết hạn. Vui lòng thử lại.");
            }
        });

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnBackToLogin.setOnClickListener(v -> finish());

        binding.btnSendReset.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, ForgotPasswordSentActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
