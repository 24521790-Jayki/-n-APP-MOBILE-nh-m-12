package com.example.group12;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.group12.adapter.AuthorSearchAdapter;
import com.example.group12.adapter.AuthorSearchItem;
import com.example.group12.adapter.SearchResultAdapter;
import com.example.group12.adapter.SearchResultItem;
import com.example.group12.api.ApiClient;
import com.example.group12.api.Book;
import com.example.group12.api.BooksApi;
import com.example.group12.api.BooksResponse;
import com.example.group12.api.SessionManager;
import com.example.group12.databinding.ActivitySearchResultsBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchResultsActivity extends AppCompatActivity {

    private ActivitySearchResultsBinding binding;
    private SearchResultAdapter bookAdapter;
    private AuthorSearchAdapter authorAdapter;

    private final List<SearchResultItem> allBooks = new ArrayList<>();
    private final List<AuthorSearchItem> allAuthors = new ArrayList<>();

    private String currentFilter = "all";
    private boolean isSortedAsc = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookAdapter = new SearchResultAdapter();
        authorAdapter = new AuthorSearchAdapter();

        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo dữ liệu mẫu trước phòng trường hợp offline
        initSampleData();

        // Lấy filter_type từ Intent nếu có
        String incomingFilter = getIntent().getStringExtra("filter_type");
        if (incomingFilter != null && !incomingFilter.isEmpty()) {
            currentFilter = incomingFilter;
        }

        String query = getIntent().getStringExtra("search_query");
        if (query != null && !query.isEmpty()) {
            binding.edtSearchQuery.setText(query);
        } else {
            query = "";
        }

        setupChipFilters();

        // Tải dữ liệu thực tế từ backend
        fetchBackendBooksAndSearch(query);

        // Nút Back
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút xóa search query
        binding.btnClearSearch.setOnClickListener(v -> {
            binding.edtSearchQuery.setText("");
            applyFilterAndSearch("", currentFilter);
        });

        // Xử lý tìm kiếm khi nhấn Enter trên bàn phím
        binding.edtSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                String newQuery = binding.edtSearchQuery.getText().toString().trim();
                applyFilterAndSearch(newQuery, currentFilter);
                return true;
            }
            return false;
        });

        // Click item sách -> Mở BookDetailActivity
        bookAdapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(SearchResultsActivity.this, BookDetailActivity.class);
            intent.putExtra("book_id", item.getBookId());
            intent.putExtra("book_title", item.getTitle());
            intent.putExtra("book_author", item.getAuthor());
            intent.putExtra("book_category", item.getTag());
            startActivity(intent);
        });

        // Click tác giả -> Lọc sách của tác giả
        authorAdapter.setOnAuthorClickListener(author -> {
            binding.edtSearchQuery.setText(author.getAuthorName());
            switchFilter("all");
            Toast.makeText(this, "Sách của tác giả: " + author.getAuthorName(), Toast.LENGTH_SHORT).show();
        });

        // Nút Sắp xếp
        binding.btnSort.setOnClickListener(v -> {
            isSortedAsc = !isSortedAsc;
            Collections.sort(allBooks, (b1, b2) -> {
                int cmp = b1.getTitle().compareToIgnoreCase(b2.getTitle());
                return isSortedAsc ? cmp : -cmp;
            });
            String sortMsg = isSortedAsc ? "Sắp xếp: Tên A-Z" : "Sắp xếp: Tên Z-A";
            Toast.makeText(this, sortMsg, Toast.LENGTH_SHORT).show();
            applyFilterAndSearch(binding.edtSearchQuery.getText().toString().trim(), currentFilter);
        });
    }

    private void fetchBackendBooksAndSearch(String initialQuery) {
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();

        if (token != null && !token.isEmpty()) {
            BooksApi api = ApiClient.getClient().create(BooksApi.class);
            api.getBooks("Bearer " + token).enqueue(new Callback<BooksResponse>() {
                @Override
                public void onResponse(Call<BooksResponse> call, Response<BooksResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<Book> books = response.body().getData();
                        if (books != null && !books.isEmpty()) {
                            allBooks.clear();
                            allAuthors.clear();

                            Map<String, Integer> authorBookCount = new HashMap<>();

                            for (Book b : books) {
                                String tag = b.getCategoryName() != null ? b.getCategoryName() : "Sách điện tử";
                                String author = b.getAuthor() != null && !b.getAuthor().trim().isEmpty() ? b.getAuthor() : "Khuyết danh";
                                String initial = b.getTitle().length() > 0 ? b.getTitle().substring(0, Math.min(2, b.getTitle().length())).toUpperCase() : "BK";

                                allBooks.add(new SearchResultItem(
                                        b.getBookID(),
                                        b.getTitle(),
                                        author,
                                        tag,
                                        "4.9",
                                        "(250 đánh giá)",
                                        initial
                                ));

                                authorBookCount.put(author, authorBookCount.getOrDefault(author, 0) + 1);
                            }

                            for (Map.Entry<String, Integer> entry : authorBookCount.entrySet()) {
                                String authorName = entry.getKey();
                                String initial = authorName.length() > 0 ? authorName.substring(0, Math.min(2, authorName.length())).toUpperCase() : "TG";
                                allAuthors.add(new AuthorSearchItem(
                                        authorName,
                                        entry.getValue() + " tác phẩm trên Trạm đọc",
                                        initial
                                ));
                            }
                        }
                    }
                    applyFilterAndSearch(initialQuery, currentFilter);
                }

                @Override
                public void onFailure(Call<BooksResponse> call, Throwable t) {
                    applyFilterAndSearch(initialQuery, currentFilter);
                }
            });
        } else {
            applyFilterAndSearch(initialQuery, currentFilter);
        }
    }

    private void setupChipFilters() {
        binding.chipAll.setOnClickListener(v -> switchFilter("all"));
        binding.chipAudio.setOnClickListener(v -> switchFilter("audio"));
        binding.chipEbook.setOnClickListener(v -> switchFilter("ebook"));
        binding.chipAuthor.setOnClickListener(v -> switchFilter("author"));
    }

    private void switchFilter(String filterType) {
        this.currentFilter = filterType;
        applyFilterAndSearch(binding.edtSearchQuery.getText().toString().trim(), filterType);
    }

    private void updateChipStyles(String activeType) {
        int inactiveBg = Color.parseColor("#F1F5F9");
        int inactiveText = Color.parseColor("#4B5563");

        setChipInactive(binding.chipAll, inactiveBg, inactiveText);
        setChipInactive(binding.chipAudio, inactiveBg, inactiveText);
        setChipInactive(binding.chipEbook, inactiveBg, inactiveText);
        setChipInactive(binding.chipAuthor, inactiveBg, inactiveText);

        TextView activeChip = binding.chipAll;
        switch (activeType) {
            case "audio":
                activeChip = binding.chipAudio;
                break;
            case "ebook":
                activeChip = binding.chipEbook;
                break;
            case "author":
                activeChip = binding.chipAuthor;
                break;
        }

        activeChip.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2563EB")));
        activeChip.setTextColor(Color.parseColor("#FFFFFF"));
        activeChip.setTypeface(null, Typeface.BOLD);
    }

    private void setChipInactive(TextView chip, int bg, int text) {
        chip.setBackgroundTintList(ColorStateList.valueOf(bg));
        chip.setTextColor(text);
        chip.setTypeface(null, Typeface.NORMAL);
    }

    private void initSampleData() {
        allBooks.clear();
        allBooks.add(new SearchResultItem(1, "Thư viện nửa đêm", "Matt Haig", "E-Book", "4.8", "(120 đánh giá)", "TV"));
        allBooks.add(new SearchResultItem(2, "Thư viện kỳ diệu của Mr. Lemoncello", "Chris Grabenstein", "Sách nói", "4.9", "(95 đánh giá)", "ML"));
        allBooks.add(new SearchResultItem(3, "Thư viện ánh sáng", "Nguyễn Nhật Ánh", "E-Book", "4.7", "(210 đánh giá)", "TA"));
        allBooks.add(new SearchResultItem(4, "Bí mật ở thư viện thành phố", "Agatha Christie", "Sách nói", "4.6", "(84 đánh giá)", "BM"));
        allBooks.add(new SearchResultItem(5, "Thư viện thời gian", "Marc Levy", "E-Book", "4.5", "(67 đánh giá)", "TT"));
        allBooks.add(new SearchResultItem(6, "Hành trình trong thư viện cổ", "Carlos Ruiz Zafón", "E-Book", "4.9", "(310 đánh giá)", "HT"));
        allBooks.add(new SearchResultItem(7, "Nhà giả kim", "Paulo Coelho", "E-Book", "4.9", "(500 đánh giá)", "NK"));
        allBooks.add(new SearchResultItem(8, "Đắc nhân tâm", "Dale Carnegie", "Sách nói", "4.9", "(890 đánh giá)", "ĐN"));

        allAuthors.clear();
        allAuthors.add(new AuthorSearchItem("Nguyễn Nhật Ánh", "28 tác phẩm trên Trạm đọc", "NA"));
        allAuthors.add(new AuthorSearchItem("Matt Haig", "12 tác phẩm trên Trạm đọc", "MH"));
        allAuthors.add(new AuthorSearchItem("Dale Carnegie", "8 tác phẩm trên Trạm đọc", "DC"));
        allAuthors.add(new AuthorSearchItem("Agatha Christie", "45 tác phẩm trên Trạm đọc", "AC"));
        allAuthors.add(new AuthorSearchItem("Marc Levy", "19 tác phẩm trên Trạm đọc", "ML"));
        allAuthors.add(new AuthorSearchItem("Paulo Coelho", "15 tác phẩm trên Trạm đọc", "PC"));
    }

    private void applyFilterAndSearch(String query, String filterType) {
        updateChipStyles(filterType);
        String lowerQuery = query != null ? query.toLowerCase() : "";

        if (filterType.equals("author")) {
            binding.rvSearchResults.setAdapter(authorAdapter);
            List<AuthorSearchItem> filteredAuthors = new ArrayList<>();
            for (AuthorSearchItem author : allAuthors) {
                if (lowerQuery.isEmpty() || author.getAuthorName().toLowerCase().contains(lowerQuery)) {
                    filteredAuthors.add(author);
                }
            }
            authorAdapter.setItems(filteredAuthors);

            int count = filteredAuthors.size();
            if (count > 0) {
                binding.tvResultCount.setText("Tìm thấy " + count + " tác giả phù hợp");
                binding.rvSearchResults.setVisibility(View.VISIBLE);
                binding.layoutEmptyState.setVisibility(View.GONE);
            } else {
                binding.tvResultCount.setText("Không tìm thấy tác giả phù hợp");
                binding.rvSearchResults.setVisibility(View.GONE);
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
            }

        } else {
            binding.rvSearchResults.setAdapter(bookAdapter);
            List<SearchResultItem> filteredBooks = new ArrayList<>();
            for (SearchResultItem book : allBooks) {
                boolean matchesQuery = lowerQuery.isEmpty()
                        || book.getTitle().toLowerCase().contains(lowerQuery)
                        || book.getAuthor().toLowerCase().contains(lowerQuery)
                        || book.getTag().toLowerCase().contains(lowerQuery);

                boolean matchesType = true;
                if (filterType.equals("audio")) {
                    matchesType = book.getTag().toLowerCase().contains("audio") || book.getTag().toLowerCase().contains("sách nói");
                } else if (filterType.equals("ebook")) {
                    matchesType = !book.getTag().toLowerCase().contains("audio") && !book.getTag().toLowerCase().contains("sách nói");
                }

                if (matchesQuery && matchesType) {
                    filteredBooks.add(book);
                }
            }
            bookAdapter.setItems(filteredBooks);

            int count = filteredBooks.size();
            String typeName = "Tất cả";
            if (filterType.equals("audio")) typeName = "Sách nói";
            else if (filterType.equals("ebook")) typeName = "Ebook";

            if (count > 0) {
                binding.tvResultCount.setText("Tìm thấy " + count + " kết quả cho \"" + typeName + "\"" + (query.isEmpty() ? "" : " (" + query + ")"));
                binding.rvSearchResults.setVisibility(View.VISIBLE);
                binding.layoutEmptyState.setVisibility(View.GONE);
            } else {
                binding.tvResultCount.setText("Không tìm thấy kết quả phù hợp");
                binding.rvSearchResults.setVisibility(View.GONE);
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
            }
        }
    }
}
