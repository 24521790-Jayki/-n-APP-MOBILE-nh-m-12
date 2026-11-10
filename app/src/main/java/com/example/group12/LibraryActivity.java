package com.example.group12;

import com.example.group12.api.SessionManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.group12.adapter.BookAdapter;
import com.example.group12.api.ApiClient;
import com.example.group12.api.Book;
import com.example.group12.api.BooksApi;
import com.example.group12.api.BooksResponse;
import com.example.group12.databinding.ActivityLibraryBinding;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryActivity extends AppCompatActivity {

    private ActivityLibraryBinding binding;
    private BookAdapter bookAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLibraryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khoi tao danh sach sach
        setupRecyclerView();

        // Tai danh sach sach tu Backend
        fetchBooks();

        // Filter
        binding.btnFilter.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LibraryActivity.this,
                    LibrarySettingsActivity.class
            );
            startActivity(intent);
            overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );
        });

        // Sort
        binding.btnSort.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LibraryActivity.this,
                    LibrarySortActivity.class
            );
            startActivity(intent);
            overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );
        });

        // Upload
        binding.btnUploadBook.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LibraryActivity.this,
                    UploadBookActivity.class
            );
            startActivity(intent);
        });

        // Audio
        binding.btnChipAudio.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LibraryActivity.this,
                    AudioPlayerActivity.class
            );
            startActivity(intent);
        });

        // Offline
        binding.btnChipOffline.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LibraryActivity.this,
                    OfflineLibraryActivity.class
            );
            startActivity(intent);
        });

        // Community
        binding.btnChipCommunity.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LibraryActivity.this,
                    UploadBookActivity.class
            );
            startActivity(intent);
        });

        // Bottom navigation
        binding.navHome.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LibraryActivity.this,
                    DashboardActivity.class
            );
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        binding.navLibrary.setOnClickListener(v -> {
            // Dang o man hinh Library
        });

        binding.navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LibraryActivity.this,
                    ProfileActivity.class
            );
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }

    // Cau hinh RecyclerView hien thi 2 cot
    private void setupRecyclerView() {
        bookAdapter = new BookAdapter();

        binding.recyclerBooks.setLayoutManager(
                new GridLayoutManager(this, 2)
        );

        binding.recyclerBooks.setAdapter(bookAdapter);
    }

    // Goi API lay danh sach sach
    private void fetchBooks() {

        // Lay JWT token da luu sau khi dang nhap
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();

        // Kiem tra token
        if (token == null || token.isEmpty()) {
            Toast.makeText(
                    this,
                    "Vui long dang nhap lai",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        BooksApi booksApi = ApiClient.getClient()
                .create(BooksApi.class);

        // Gui JWT token len Backend
        booksApi.getBooks("Bearer " + token)
                .enqueue(new Callback<BooksResponse>() {

                    @Override
                    public void onResponse(
                            Call<BooksResponse> call,
                            Response<BooksResponse> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            List<Book> books = response.body().getData();

                            bookAdapter.setBooks(books);

                            int totalBooks = books == null ? 0 : books.size();

                            binding.txtBookCount.setText(
                                    totalBooks + " cuốn sách"
                            );

                        } else {
                            Toast.makeText(
                                    LibraryActivity.this,
                                    "Không thể tải sách - HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<BooksResponse> call,
                            Throwable t
                    ) {
                        Toast.makeText(
                                LibraryActivity.this,
                                "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }



        }


