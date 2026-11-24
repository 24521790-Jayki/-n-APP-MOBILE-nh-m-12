package com.example.group12;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.api.ApiClient;
import com.example.group12.databinding.ActivityBookDetailBinding;
import com.example.group12.model.BookChaptersResponse;
import com.example.group12.model.Chapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookDetailActivity extends AppCompatActivity {

    private ActivityBookDetailBinding binding;
    private boolean isFavorite = false;
    private int bookId = -1;
    private String bookTitle = "";
    private String bookAuthor = "";
    private SharedPreferences favPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        favPrefs = getSharedPreferences("favorite_books_prefs", Context.MODE_PRIVATE);

        // Nhận dữ liệu Tên sách, Tác giả, Category, Description, ID từ Intent
        bookId = getIntent().getIntExtra("book_id", getIntent().getIntExtra("bookId", -1));
        bookTitle = getIntent().getStringExtra("book_title");
        bookAuthor = getIntent().getStringExtra("book_author");
        String category = getIntent().getStringExtra("book_category");
        String description = getIntent().getStringExtra("book_description");

        if (bookTitle != null && !bookTitle.isEmpty()) {
            binding.tvBookTitle.setText(bookTitle);
            binding.tvBookCoverTitle.setText(bookTitle);
        }
        if (bookAuthor != null && !bookAuthor.isEmpty()) {
            binding.tvBookAuthor.setText(bookAuthor);
        }
        if (category != null && !category.isEmpty()) {
            binding.tvBookCategory.setText(category);
        }
        if (description != null && !description.isEmpty()) {
            binding.tvBookDescription.setText(description);
        }

        // Tải trạng thái yêu thích đã lưu
        if (bookId > 0) {
            isFavorite = favPrefs.getBoolean("fav_" + bookId, false);
            updateFavoriteIcon();
        }

        // Tải danh sách chương từ server
        if (bookId > 0) {
            loadBookChapters(bookId);
        }

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Yêu thích (Tim)
        binding.btnFavorite.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            if (bookId > 0) {
                favPrefs.edit().putBoolean("fav_" + bookId, isFavorite).apply();
            }
            updateFavoriteIcon();
            Toast.makeText(
                    this,
                    isFavorite ? "Đã thêm vào danh sách yêu thích!" : "Đã bỏ khỏi danh sách yêu thích",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // Nút Đọc sách ngay -> Chuyển sang ReaderActivity
        binding.btnReadNow.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailActivity.this, ReaderActivity.class);
            if (bookId > 0) intent.putExtra("bookId", bookId);
            startActivity(intent);
        });

        // Click các chương sách -> Chuyển sang ReaderActivity
        binding.layoutChapter1.setOnClickListener(v -> openReaderAtChapter(1, binding.tvChapter1Title.getText().toString()));
        binding.layoutChapter2.setOnClickListener(v -> openReaderAtChapter(2, binding.tvChapter2Title.getText().toString()));
        binding.layoutChapter3.setOnClickListener(v -> openReaderAtChapter(3, binding.tvChapter3Title.getText().toString()));

        // Nút Nghe Audio -> Chuyển sang AudioPlayerActivity
        binding.btnListenAudio.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailActivity.this, AudioPlayerActivity.class);
            if (bookId > 0) intent.putExtra("bookId", bookId);
            intent.putExtra("book_title", bookTitle);
            intent.putExtra("book_author", bookAuthor);
            startActivity(intent);
        });
    }

    private void updateFavoriteIcon() {
        binding.btnFavorite.setImageResource(
                isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border
        );
    }

    private void openReaderAtChapter(int chapterNumber, String chapterTitle) {
        Intent intent = new Intent(BookDetailActivity.this, ReaderActivity.class);
        if (bookId > 0) intent.putExtra("bookId", bookId);
        intent.putExtra("chapter_number", chapterNumber);
        intent.putExtra("chapter_title", chapterTitle);
        startActivity(intent);
    }

    private void loadBookChapters(int id) {
        ApiClient.getApiService().getBookChapters(id).enqueue(new Callback<BookChaptersResponse>() {
            @Override
            public void onResponse(Call<BookChaptersResponse> call, Response<BookChaptersResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    BookChaptersResponse.Data data = response.body().getData();
                    if (data != null && data.getChapters() != null) {
                        List<Chapter> chapters = data.getChapters();
                        int size = chapters.size();

                        if (size >= 1) {
                            binding.tvChapter1Title.setText("Chương 1: " + chapters.get(0).getTitle());
                            binding.layoutChapter1.setVisibility(View.VISIBLE);
                        }
                        if (size >= 2) {
                            binding.tvChapter2Title.setText("Chương 2: " + chapters.get(1).getTitle());
                            binding.layoutChapter2.setVisibility(View.VISIBLE);
                        } else {
                            binding.layoutChapter2.setVisibility(View.GONE);
                        }
                        if (size >= 3) {
                            binding.tvChapter3Title.setText("Chương 3: " + chapters.get(2).getTitle());
                            binding.layoutChapter3.setVisibility(View.VISIBLE);
                        } else {
                            binding.layoutChapter3.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<BookChaptersResponse> call, Throwable t) {
                // Keep default chapter UI
            }
        });
    }
}
