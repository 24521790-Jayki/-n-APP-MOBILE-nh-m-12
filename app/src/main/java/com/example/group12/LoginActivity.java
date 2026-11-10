
package com.example.group12;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.group12.api.ApiClient;
import com.example.group12.api.AuthApi;
import com.example.group12.api.LoginRequest;
import com.example.group12.api.LoginResponse;
import com.example.group12.api.SessionManager;
import com.example.group12.databinding.ActivityLoginBinding;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

private ActivityLoginBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);

        // Khởi tạo Cloudflare Turnstile Captcha
        TurnstileHelper.setupTurnstile(
                binding.wvTurnstile,
                TurnstileHelper.DEMO_SITE_KEY,
                new TurnstileHelper.TurnstileListener() {

                    @Override
                    public void onVerified(String token) {
                        binding.cbHCaptcha.setChecked(true);
                        binding.tvHCaptchaText.setText(
                                "Xác minh Cloudflare Turnstile thành công!"
                        );
                    }

                    @Override
                    public void onExpired() {
                        binding.cbHCaptcha.setChecked(false);
                        binding.tvHCaptchaText.setText(
                                "Xác minh Cloudflare Turnstile đã hết hạn. Vui lòng thử lại."
                        );
                    }
                }
        );

        // Xử lý nút hiện/ẩn mật khẩu
        binding.tilPassword.setEndIconOnClickListener(v -> {

            boolean isVisible =
                    binding.edtPassword.getTransformationMethod()
                            instanceof HideReturnsTransformationMethod;

            if (isVisible) {
                binding.edtPassword.setTransformationMethod(
                        PasswordTransformationMethod.getInstance()
                );

                binding.edtPassword.setFocusable(true);
                binding.edtPassword.setFocusableInTouchMode(true);
                binding.edtPassword.setEnabled(true);

            } else {
                binding.edtPassword.setTransformationMethod(
                        HideReturnsTransformationMethod.getInstance()
                );

                binding.edtPassword.setFocusable(false);
                binding.edtPassword.setFocusableInTouchMode(false);
                binding.edtPassword.setEnabled(false);
            }

            if (binding.edtPassword.getText() != null) {
                binding.edtPassword.setSelection(
                        binding.edtPassword.getText().length()
                );
            }
        });

        // Đăng nhập bằng Backend API
        binding.btnLogin.setOnClickListener(v -> login());

        // Google: chưa tích hợp OAuth
        binding.btnGoogle.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Đăng nhập Google chưa được tích hợp.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        // Facebook: chưa tích hợp OAuth
        binding.btnFacebook.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Đăng nhập Facebook chưa được tích hợp.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        // Quên mật khẩu
        binding.tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    ForgotPasswordActivity.class
            );
            startActivity(intent);
        });

        // Đăng ký
        binding.tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    SignUpActivity.class
            );
            startActivity(intent);
        });
    }

    private void login() {

        String email = binding.edtUsername.getText().toString().trim();
        String password = binding.edtPassword.getText().toString();

        // Kiểm tra email
        if (TextUtils.isEmpty(email)) {
            binding.edtUsername.setError("Vui lòng nhập email");
            binding.edtUsername.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtUsername.setError("Email không hợp lệ");
            binding.edtUsername.requestFocus();
            return;
        }

        // Kiểm tra mật khẩu
        if (TextUtils.isEmpty(password)) {
            binding.edtPassword.setError("Vui lòng nhập mật khẩu");
            binding.edtPassword.requestFocus();
            return;
        }

        // Khóa nút trong khi gửi request
        binding.btnLogin.setEnabled(false);

        AuthApi authApi = ApiClient.getClient().create(AuthApi.class);

        LoginRequest request = new LoginRequest(email, password);

        authApi.login(request).enqueue(new Callback<LoginResponse>() {

            @Override
            public void onResponse(
                    Call<LoginResponse> call,
                    Response<LoginResponse> response
            ) {

                binding.btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {

                    LoginResponse result = response.body();

                    if (result.getToken() != null && result.getUser() != null) {

                        LoginResponse.User user = result.getUser();

                        // Lưu thông tin phiên đăng nhập
                        sessionManager.saveSession(
                                result.getToken(),
                                user.getUserID(),
                                user.getFullName(),
                                user.getEmail(),
                                user.getRole()
                        );

                        Toast.makeText(
                                LoginActivity.this,
                                "Đăng nhập thành công!",
                                Toast.LENGTH_SHORT
                        ).show();

                        // Chuyển sang Dashboard
                        Intent intent = new Intent(
                                LoginActivity.this,
                                DashboardActivity.class
                        );

                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(
                                LoginActivity.this,
                                "Dữ liệu đăng nhập không hợp lệ.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                } else {

                    String errorMessage = "Đăng nhập thất bại.";

                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();

                            JSONObject jsonObject = new JSONObject(errorJson);

                            errorMessage = jsonObject.optString(
                                    "message",
                                    errorMessage
                            );
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(
                            LoginActivity.this,
                            errorMessage,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<LoginResponse> call,
                    Throwable t
            ) {

                binding.btnLogin.setEnabled(true);

                Toast.makeText(
                        LoginActivity.this,
                        "Không thể kết nối Backend: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}

