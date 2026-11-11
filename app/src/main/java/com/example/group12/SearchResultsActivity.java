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
import com.example.group12.databinding.ActivitySearchResultsBinding;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsActivity extends AppCompatActivity {

    private ActivitySearchResultsBinding binding;
    private SearchResultAdapter bookAdapter;
    private AuthorSearchAdapter authorAdapter;
    
    private List<SearchResultItem> allBooks;
    private List<AuthorSearchItem> allAuthors;
    
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup RecyclerView & Adapters
        bookAdapter = new SearchResultAdapter();
        authorAdapter = new AuthorSearchAdapter();
        
        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo dữ liệu mẫu
        initSampleData();

        // Lấy filter_type từ Intent nếu có
        String incomingFilter = getIntent().getStringExtra("filter_type");
        if (incomingFilter != null) {
            currentFilter = incomingFilter;
        }

        String query = getIntent().getStringExtra("search_query");
        if (query != null && !query.isEmpty()) {
            binding.edtSearchQuery.setText(query);
        } else {
            query = "";
        }

        // Thiết lập sự kiện và trạng thái cho các Chip Filter
        setupChipFilters();
        applyFilterAndSearch(query, currentFilter);

        // Nút Back
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút xóa search query
        binding.btnClearSearch.setOnClickListener(v -> {
            binding.edtSearchQuery.setText("");
            applyFilterAndSearch("", currentFilter);
        });

        // Xử lý tìm kiếm khi nhấn Enter trên bàn phím
        final String activeFilter = currentFilter;
        binding.edtSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                String newQuery = binding.edtSearchQuery.getText().toString().trim();
                applyFilterAndSearch(newQuery, activeFilter);
                return true;
            }
            return false;
        });

        // Click item sách -> Mở BookDetailActivity
        bookAdapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(SearchResultsActivity.this, BookDetailActivity.class);
            intent.putExtra("book_title", item.getTitle());
            intent.putExtra("book_author", item.getAuthor());
            startActivity(intent);
        });

        // Click tác giả -> Thông báo hoặc xem sách của tác giả
        authorAdapter.setOnAuthorClickListener(author -> {
            Toast.makeText(this, "Xem tác phẩm của: " + author.getAuthorName(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SearchResultsActivity.this, SearchResultsActivity.class);
            intent.putExtra("filter_type", "all");
            intent.putExtra("search_query", author.getAuthorName());
            startActivity(intent);
        });

        // Nút Sắp xếp
        binding.btnSort.setOnClickListener(v ->
            Toast.makeText(this, "Đã sắp xếp theo: Mới nhất", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupChipFilters() {
        binding.chipAll.setOnClickListener(v -> switchFilter("all"));
        binding.chipAudio.setOnClickListener(v -> switchFilter("audio"));
        binding.chipEbook.setOnClickListener(v -> switchFilter("ebook"));
        binding.chipAuthor.setOnClickListener(v -> switchFilter("author"));
    }

    private void switchFilter(String filterType) {
        Intent intent = new Intent(SearchResultsActivity.this, SearchResultsActivity.class);
        intent.putExtra("filter_type", filterType);
        intent.putExtra("search_query", binding.edtSearchQuery.getText().toString());
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void updateChipStyles(String activeType) {
        int inactiveBg = Color.parseColor("#F1F5F9");
        int inactiveText = Color.parseColor("#4B5563");

        // Reset all chips to Inactive
        setChipInactive(binding.chipAll, inactiveBg, inactiveText);
        setChipInactive(binding.chipAudio, inactiveBg, inactiveText);
        setChipInactive(binding.chipEbook, inactiveBg, inactiveText);
        setChipInactive(binding.chipAuthor, inactiveBg, inactiveText);

        // Highlight Active chip
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
        allBooks = new ArrayList<>();
        allBooks.add(new SearchResultItem("Thư viện nửa đêm", "Matt Haig", "E-Book", "4.8", "(120 đánh giá)", "TV"));
        allBooks.add(new SearchResultItem("Thư viện kỳ diệu của Mr. Lemoncello", "Chris Grabenstein", "Sách nói", "4.9", "(95 đánh giá)", "ML"));
        allBooks.add(new SearchResultItem("Thư viện ánh sáng", "Nguyễn Nhật Ánh", "E-Book", "4.7", "(210 đánh giá)", "TA"));
        allBooks.add(new SearchResultItem("Bí mật ở thư viện thành phố", "Agatha Christie", "Sách nói", "4.6", "(84 đánh giá)", "BM"));
        allBooks.add(new SearchResultItem("Thư viện thời gian", "Marc Levy", "E-Book", "4.5", "(67 đánh giá)", "TT"));
        allBooks.add(new SearchResultItem("Hành trình trong thư viện cổ", "Carlos Ruiz Zafón", "E-Book", "4.9", "(310 đánh giá)", "HT"));
        allBooks.add(new SearchResultItem("Nhà giả kim", "Paulo Coelho", "E-Book", "4.9", "(500 đánh giá)", "NK"));
        allBooks.add(new SearchResultItem("Đắc nhân tâm", "Dale Carnegie", "Sách nói", "4.9", "(890 đánh giá)", "ĐN"));

        allAuthors = new ArrayList<>();
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
            // Hiển thị danh sách Tác giả
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
            // Hiển thị danh sách Sách (Tất cả, Sách nói, Ebook)
            binding.rvSearchResults.setAdapter(bookAdapter);
            List<SearchResultItem> filteredBooks = new ArrayList<>();
            for (SearchResultItem book : allBooks) {
                boolean matchesQuery = lowerQuery.isEmpty() || book.getTitle().toLowerCase().contains(lowerQuery) || book.getAuthor().toLowerCase().contains(lowerQuery);
                boolean matchesType = true;

                if (filterType.equals("audio")) {
                    matchesType = book.getTag().equalsIgnoreCase("Sách nói");
                } else if (filterType.equals("ebook")) {
                    matchesType = book.getTag().equalsIgnoreCase("E-Book");
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
