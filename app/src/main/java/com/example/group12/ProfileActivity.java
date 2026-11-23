package com.example.group12;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.group12.api.ApiClient;
import com.example.group12.api.SessionManager;
import com.example.group12.api.User;
import com.example.group12.api.UserResponse;
import com.example.group12.api.UsersApi;
import com.example.group12.databinding.ActivityProfileBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });

        // Hiển thị thông tin đã lưu trong phiên đăng nhập
        displaySessionInfo();

        // Lấy thông tin mới nhất từ Backend
        fetchUserProfile();

        // Sách đã tải lên
        binding.cardUploadedBooks.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, UploadBookActivity.class);
            startActivity(intent);
        });

        // Thư viện offline / Lưu trữ
        binding.cardOfflineStorage.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, OfflineLibraryActivity.class);
            startActivity(intent);
        });

        // Bộ sưu tập ảnh Quote
        binding.cardQuotes.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, QuoteGeneratorActivity.class);
            startActivity(intent);
        });

        // Cài đặt chế độ đọc
        binding.cardSettings.setOnClickListener(v -> {
            BottomSheetDialog sheetDialog = new BottomSheetDialog(ProfileActivity.this);
            View view = LayoutInflater.from(ProfileActivity.this)
                    .inflate(R.layout.dialog_reading_settings, null);
            sheetDialog.setContentView(view);
            sheetDialog.show();
        });

        // Hỗ trợ & Đóng góp
        binding.cardSupport.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ReportActivity.class);
            startActivity(intent);
        });

        // Đăng xuất
        binding.btnLogout.setOnClickListener(v -> {
            sessionManager.clearSession();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );
            startActivity(intent);
            finishAffinity();
        });

        // Bottom navigation
        binding.navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, DashboardActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        binding.navLibrary.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LibraryActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        binding.navProfile.setOnClickListener(v -> {
            // Đang ở Cá nhân
        });
    }

    private void displaySessionInfo() {
        binding.tvUserName.setText(sessionManager.getFullName());
        binding.tvUserEmail.setText(sessionManager.getEmail());

        displayRole(sessionManager.getRole());
    }

    private void fetchUserProfile() {
        int userId = sessionManager.getUserId();
        String token = sessionManager.getToken();

        if (userId <= 0 || token == null || token.isEmpty()) {
            Toast.makeText(
                    this,
                    "Vui lòng đăng nhập lại.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        UsersApi usersApi = ApiClient.getClient().create(UsersApi.class);

        usersApi.getUserById("Bearer " + token, userId)
                .enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(
                            Call<UserResponse> call,
                            Response<UserResponse> response
                    ) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            User user = response.body().getData();

                            if (user != null) {
                                binding.tvUserName.setText(user.getFullName());
                                binding.tvUserEmail.setText(user.getEmail());
                                displayRole(user.getRole());
                            }
                        } else if (response.code() == 401) {
                            Toast.makeText(
                                    ProfileActivity.this,
                                    "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.",
                                    Toast.LENGTH_LONG
                            ).show();
                        } else if (response.code() == 403) {
                            Toast.makeText(
                                    ProfileActivity.this,
                                    "Bạn không có quyền xem hồ sơ này.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        } else {
                            Toast.makeText(
                                    ProfileActivity.this,
                                    "Không thể tải thông tin hồ sơ.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<UserResponse> call,
                            Throwable t
                    ) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Toast.makeText(
                                ProfileActivity.this,
                                "Không thể kết nối Backend: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void displayRole(String role) {
        if ("admin".equalsIgnoreCase(role)) {
            binding.tvUserRole.setText("Quản trị viên");
        } else {
            binding.tvUserRole.setText("Độc giả");
        }
    }
}
