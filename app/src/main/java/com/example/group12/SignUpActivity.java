package com.example.group12;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.group12.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivitySignUpBinding binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Link Đăng nhập
        binding.tvNavLogin.setOnClickListener(v -> finish());

        // Nút Đăng ký -> Kiểm tra thông tin & Chuyển về LoginActivity
        binding.btnSignUp.setOnClickListener(v -> {
            String fullName = binding.edtFullName.getText() != null ? binding.edtFullName.getText().toString().trim() : "";
            String email = binding.edtEmail.getText() != null ? binding.edtEmail.getText().toString().trim() : "";

            binding.tilFullName.setError(null);
            binding.tilEmail.setError(null);

            boolean valid = true;
            if (fullName.isEmpty()) {
                binding.tilFullName.setError("Vui lòng nhập họ và tên!");
                valid = false;
            }
            if (email.isEmpty()) {
                binding.tilEmail.setError("Vui lòng nhập địa chỉ email!");
                valid = false;
            }
            if (!binding.cbTerms.isChecked()) {
                Toast.makeText(this, "Bạn cần đồng ý với Điều khoản sử dụng!", Toast.LENGTH_SHORT).show();
                valid = false;
            }

            if (valid) {
                Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });
    }
}
