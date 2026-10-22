package com.example.group12;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
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

        // Xử lý nút con mắt mật khẩu: Khi hiển thị mật khẩu -> Khóa không cho nhập. Chỉ cho nhập khi ẩn mật khẩu.
        binding.tilPassword.setEndIconOnClickListener(v -> {
            boolean isVisible = binding.edtPassword.getTransformationMethod() instanceof HideReturnsTransformationMethod;
            if (isVisible) {
                // Chuyển sang Ẩn mật khẩu -> Cho phép nhập
                binding.edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                binding.edtPassword.setFocusable(true);
                binding.edtPassword.setFocusableInTouchMode(true);
                binding.edtPassword.setEnabled(true);
            } else {
                // Chuyển sang Hiển thị mật khẩu -> Khóa nhập
                binding.edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                binding.edtPassword.setFocusable(false);
                binding.edtPassword.setFocusableInTouchMode(false);
                binding.edtPassword.setEnabled(false);
            }
            if (binding.edtPassword.getText() != null) {
                binding.edtPassword.setSelection(binding.edtPassword.getText().length());
            }
        });

        // Nút Đăng nhập -> Sang Dashboard
        binding.btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        });

        // Đăng nhập bằng Google
        binding.btnGoogle.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        });

        // Đăng nhập bằng Facebook
        binding.btnFacebook.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        });

        // Nút Quên mật khẩu
        binding.tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Link Đăng ký ngay
        binding.tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}
