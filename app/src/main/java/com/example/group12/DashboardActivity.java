package com.example.group12;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.group12.api.ApiClient;
import com.example.group12.api.Book;
import com.example.group12.api.BooksApi;
import com.example.group12.api.BooksResponse;
import com.example.group12.api.SessionManager;
import com.example.group12.databinding.ActivityDashboardBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private SessionManager sessionManager;
    private final List<Book> recommendedBooks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Hiển thị tên người dùng
        String fullName = sessionManager.getFullName();
        if (fullName != null && !fullName.trim().isEmpty()) {
            binding.tvGreeting.setText("Xin chào, " + fullName + "!");
        }

        // Avatar -> Trang cá nhân
        binding.imgAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        // Ô Tìm Kiếm -> Mở SearchActivity
        binding.edtSearch.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SearchActivity.class);
            startActivity(intent);
        });
        binding.edtSearch.setFocusable(false);
        binding.edtSearch.setClickable(true);

        // Thẻ Nghe Âm Thanh / Sách Nói
        binding.cardAudio.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AudioPlayerActivity.class);
            startActivity(intent);
        });

        // Thẻ Tải Sách Lên
        binding.cardUpload.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, UploadBookActivity.class);
            startActivity(intent);
        });

        // Thẻ Thư Viện Ngoại Tuyến (Offline)
        binding.cardOffline.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, OfflineLibraryActivity.class);
            startActivity(intent);
        });

        // Thẻ Streak & Thống kê -> Mở StreakActivity
        binding.cardStreak.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, StreakActivity.class);
            startActivity(intent);
        });

        // Tải danh sách sách gợi ý từ Backend
        fetchRecommendedBooks();

        // Gợi ý sách AI -> Mở BookDetailActivity
        binding.cardRec1.setOnClickListener(v -> openBookDetail(0));
        binding.cardRec2.setOnClickListener(v -> openBookDetail(1));
        binding.cardRec3.setOnClickListener(v -> openBookDetail(2));

        // Thanh điều hướng dưới cùng (Bottom Navigation)
        binding.navHome.setOnClickListener(v -> {
            // Đang ở Trang chủ
        });

        binding.navLibrary.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, LibraryActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        binding.navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }

    private void openBookDetail(int index) {
        Intent intent = new Intent(DashboardActivity.this, BookDetailActivity.class);
        if (index < recommendedBooks.size()) {
            Book book = recommendedBooks.get(index);
            intent.putExtra("book_id", book.getBookID());
            intent.putExtra("book_title", book.getTitle());
            intent.putExtra("book_author", book.getAuthor());
            intent.putExtra("book_category", book.getCategoryName());
            intent.putExtra("book_description", book.getDescription());
        }
        startActivity(intent);
    }

    private void fetchRecommendedBooks() {
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) return;

        BooksApi booksApi = ApiClient.getClient().create(BooksApi.class);
        booksApi.getBooks("Bearer " + token).enqueue(new Callback<BooksResponse>() {
            @Override
            public void onResponse(Call<BooksResponse> call, Response<BooksResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Book> books = response.body().getData();
                    if (books != null && !books.isEmpty()) {
                        recommendedBooks.clear();
                        recommendedBooks.addAll(books);

                        if (books.size() >= 1) {
                            Book b1 = books.get(0);
                            binding.tvRec1Title.setText(b1.getTitle());
                            binding.tvRec1Cover.setText(b1.getTitle());
                            binding.tvRec1Author.setText(b1.getAuthor() != null ? b1.getAuthor() : "Khuyết danh");
                        }
                        if (books.size() >= 2) {
                            Book b2 = books.get(1);
                            binding.tvRec2Title.setText(b2.getTitle());
                            binding.tvRec2Cover.setText(b2.getTitle());
                            binding.tvRec2Author.setText(b2.getAuthor() != null ? b2.getAuthor() : "Khuyết danh");
                        }
                        if (books.size() >= 3) {
                            Book b3 = books.get(2);
                            binding.tvRec3Title.setText(b3.getTitle());
                            binding.tvRec3Cover.setText(b3.getTitle());
                            binding.tvRec3Author.setText(b3.getAuthor() != null ? b3.getAuthor() : "Khuyết danh");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<BooksResponse> call, Throwable t) {
                // Keep default recommendation UI
            }
        });
    }
}
