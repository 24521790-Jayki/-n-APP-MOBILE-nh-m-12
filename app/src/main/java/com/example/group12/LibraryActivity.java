package com.example.group12;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group12.adapter.BookAdapter;
import com.example.group12.api.ApiClient;
import com.example.group12.api.Book;
import com.example.group12.api.BooksApi;
import com.example.group12.api.BooksResponse;
import com.example.group12.api.SessionManager;
import com.example.group12.databinding.ActivityLibraryBinding;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryActivity extends AppCompatActivity {

    private ActivityLibraryBinding binding;
    private BookAdapter bookAdapter;
    private SharedPreferences libraryPrefs;

    private final ActivityResultLauncher<Intent> settingsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    applyLibraryPreferences();
                }
            }
    );

    private final ActivityResultLauncher<Intent> sortLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    int sortType = result.getData().getIntExtra(
                            LibrarySortActivity.EXTRA_SORT_TYPE,
                            BookAdapter.SORT_NEWEST
                    );
                    String sortName = result.getData().getStringExtra(
                            LibrarySortActivity.EXTRA_SORT_NAME
                    );
                    if (sortName != null) {
                        binding.txtCurrentSort.setText(sortName);
                    }
                    bookAdapter.sortBooks(sortType);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityLibraryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        libraryPrefs = getSharedPreferences(LibrarySettingsActivity.PREF_NAME, Context.MODE_PRIVATE);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khoi tao danh sach sach
        setupRecyclerView();

        // Khoi tao chuc nang tim kiem
        setupSearch();

        // Ap dung cai dat hien thi & sap xep da luu
        applyLibraryPreferences();

        // Tai danh sach sach tu Backend
        fetchBooks();

        // Nut Cai dat hien thi (Filter/Settings)
        binding.btnFilter.setOnClickListener(v -> {
            Intent intent = new Intent(LibraryActivity.this, LibrarySettingsActivity.class);
            settingsLauncher.launch(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Nut Sap xep (Sort)
        binding.btnSort.setOnClickListener(v -> {
            Intent intent = new Intent(LibraryActivity.this, LibrarySortActivity.class);
            intent.putExtra(LibrarySortActivity.EXTRA_SORT_TYPE, bookAdapter.getCurrentSortType());
            sortLauncher.launch(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Upload sach
        binding.btnUploadBook.setOnClickListener(v -> {
            Intent intent = new Intent(LibraryActivity.this, UploadBookActivity.class);
            startActivity(intent);
        });

        // Filter chips navigation / filter
        binding.btnChipAll.setOnClickListener(v -> {
            bookAdapter.filterBooks("");
            updateBookCount();
        });

        binding.btnChipAudio.setOnClickListener(v -> {
            Intent intent = new Intent(LibraryActivity.this, AudioLibraryActivity.class);
            startActivity(intent);
        });

        binding.btnChipOffline.setOnClickListener(v -> {
            Intent intent = new Intent(LibraryActivity.this, OfflineLibraryActivity.class);
            startActivity(intent);
        });

        binding.btnChipCommunity.setOnClickListener(v -> {
            Intent intent = new Intent(LibraryActivity.this, CommunityLibraryActivity.class);
            startActivity(intent);
        });

        binding.btnScrollTop.setOnClickListener(v -> binding.recyclerBooks.smoothScrollToPosition(0));

        // Lang nghe su kien cuon de an/hien nut Upload va nut ScrollTop
        binding.recyclerBooks.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 8) {
                    binding.btnUploadBook.setVisibility(View.GONE);
                    binding.btnScrollTop.setVisibility(View.GONE);
                } else if (dy < -8) {
                    binding.btnUploadBook.setVisibility(View.VISIBLE);
                    binding.btnScrollTop.setVisibility(View.VISIBLE);
                }
            }
        });

        // Thanh dieu huong duoi cung (Bottom navigation)
        binding.navHome.setOnClickListener(v -> {
            Intent intent = new Intent(LibraryActivity.this, DashboardActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        binding.navLibrary.setOnClickListener(v -> {
            // Dang o man hinh Library
        });

        binding.navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(LibraryActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyLibraryPreferences();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyLayoutManager();
    }

    private void applyLibraryPreferences() {
        String displayMode = libraryPrefs.getString(LibrarySettingsActivity.KEY_DISPLAY_MODE, "grid");
        boolean isList = "list".equalsIgnoreCase(displayMode);
        bookAdapter.setDisplayMode(isList ? BookAdapter.VIEW_TYPE_LIST : BookAdapter.VIEW_TYPE_GRID);

        boolean showAudio = libraryPrefs.getBoolean(LibrarySettingsActivity.KEY_SHOW_AUDIO, true);
        bookAdapter.setShowAudioBadge(showAudio);

        int savedSortType = libraryPrefs.getInt(LibrarySortActivity.KEY_SORT_TYPE, BookAdapter.SORT_NEWEST);
        String savedSortName = libraryPrefs.getString(LibrarySortActivity.KEY_SORT_NAME, "Mới nhất");
        binding.txtCurrentSort.setText(savedSortName);
        bookAdapter.sortBooks(savedSortType);

        applyLayoutManager();
    }

    private void applyLayoutManager() {
        String displayMode = libraryPrefs.getString(LibrarySettingsActivity.KEY_DISPLAY_MODE, "grid");
        boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        if ("list".equalsIgnoreCase(displayMode)) {
            binding.recyclerBooks.setLayoutManager(new LinearLayoutManager(this));
        } else {
            int spanCount = isLandscape ? 3 : 2;
            binding.recyclerBooks.setLayoutManager(new GridLayoutManager(this, spanCount));
        }

        if (binding.topHeader != null) {
            float density = getResources().getDisplayMetrics().density;
            int topPad = isLandscape ? (int) (6 * density) : (int) (20 * density);
            int bottomPad = isLandscape ? (int) (4 * density) : (int) (8 * density);
            binding.topHeader.setPadding(
                    (int) (16 * density),
                    topPad,
                    (int) (16 * density),
                    bottomPad
            );
        }
    }

    // Cau hinh chuc nang tim kiem sach
    private void setupSearch() {
        binding.etSearchBook.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                bookAdapter.filterBooks(s.toString());
                updateBookCount();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateBookCount() {
        int totalBooks = bookAdapter.getItemCount();
        binding.txtBookCount.setText(totalBooks + " cuốn sách");
    }

    private void setupRecyclerView() {
        bookAdapter = new BookAdapter();
        applyLayoutManager();
        binding.recyclerBooks.setAdapter(bookAdapter);

        // Tat ca nguoi dung (ca Admin va Doc gia) deu co the mo xem chi tiet va doc sach
        bookAdapter.setOnBookClickListener(book -> {
            Intent intent = new Intent(LibraryActivity.this, BookDetailActivity.class);
            intent.putExtra("book_id", book.getBookID());
            intent.putExtra("book_title", book.getTitle());
            intent.putExtra("book_author", book.getAuthor());
            intent.putExtra("book_category", book.getCategoryName());
            intent.putExtra("book_description", book.getDescription());
            startActivity(intent);
        });
    }

    // Goi API lay danh sach sach
    private void fetchBooks() {
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_LONG).show();
            return;
        }

        BooksApi booksApi = ApiClient.getClient().create(BooksApi.class);

        booksApi.getBooks("Bearer " + token).enqueue(new Callback<BooksResponse>() {
            @Override
            public void onResponse(Call<BooksResponse> call, Response<BooksResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Book> books = response.body().getData();
                    bookAdapter.setBooks(books);
                    updateBookCount();
                } else {
                    Toast.makeText(
                            LibraryActivity.this,
                            "Không thể tải sách - HTTP " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<BooksResponse> call, Throwable t) {
                Toast.makeText(
                        LibraryActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
