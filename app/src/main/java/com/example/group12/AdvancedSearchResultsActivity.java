package com.example.group12;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.group12.adapter.AdvancedSearchAdapter;
import com.example.group12.adapter.AdvancedSearchItem;
import com.example.group12.api.ApiClient;
import com.example.group12.api.Book;
import com.example.group12.api.BooksApi;
import com.example.group12.api.BooksResponse;
import com.example.group12.api.SessionManager;
import com.example.group12.databinding.ActivityAdvancedSearchResultsBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdvancedSearchResultsActivity extends AppCompatActivity {

    private ActivityAdvancedSearchResultsBinding binding;
    private AdvancedSearchAdapter adapter;
    private final List<AdvancedSearchItem> allItems = new ArrayList<>();

    private String query = "";
    private String category = "Tất cả thể loại";
    private String sortBy = "Liên quan nhất";
    private String status = "Tất cả";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdvancedSearchResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new AdvancedSearchAdapter();
        binding.rvAdvancedSearchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdvancedSearchResults.setAdapter(adapter);

        initSampleData();

        String incomingQuery = getIntent().getStringExtra("query");
        String incomingCategory = getIntent().getStringExtra("category");
        String incomingSortBy = getIntent().getStringExtra("sort_by");
        String incomingStatus = getIntent().getStringExtra("status");

        if (incomingQuery != null) query = incomingQuery;
        if (incomingCategory != null && !incomingCategory.isEmpty()) category = incomingCategory;
        if (incomingSortBy != null) sortBy = incomingSortBy;
        if (incomingStatus != null && !incomingStatus.isEmpty()) status = incomingStatus;

        String summary = "Từ khóa: \"" + (query.isEmpty() ? "Tất cả" : query) + "\"" +
                " • Thể loại: " + category +
                " • Trạng thái: " + status +
                " • Sắp xếp: " + sortBy;
        binding.tvFilterSummaryText.setText(summary);

        fetchBooksAndFilter();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnEditFilter.setOnClickListener(v -> finish());

        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(AdvancedSearchResultsActivity.this, BookDetailActivity.class);
            intent.putExtra("book_id", item.getBookId());
            intent.putExtra("book_title", item.getTitle());
            intent.putExtra("book_author", item.getAuthor());
            intent.putExtra("book_category", item.getCategory());
            startActivity(intent);
        });
    }

    private void fetchBooksAndFilter() {
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
                            allItems.clear();
                            for (Book b : books) {
                                String author = b.getAuthor() != null && !b.getAuthor().trim().isEmpty() ? b.getAuthor() : "Khuyết danh";
                                String catName = b.getCategoryName() != null ? b.getCategoryName() : "Tổng hợp";
                                String initial = !b.getTitle().isEmpty() ? b.getTitle().substring(0, Math.min(2, b.getTitle().length())).toUpperCase() : "BK";

                                allItems.add(new AdvancedSearchItem(
                                        b.getBookID(),
                                        b.getTitle(),
                                        author,
                                        catName,
                                        "Đã hoàn thành",
                                        "4.8",
                                        "Tiếng Việt",
                                        initial
                                ));
                            }
                        }
                    }
                    filterAdvancedResults(query, category, status);
                }

                @Override
                public void onFailure(Call<BooksResponse> call, Throwable t) {
                    filterAdvancedResults(query, category, status);
                }
            });
        } else {
            filterAdvancedResults(query, category, status);
        }
    }

    private void initSampleData() {
        allItems.clear();
        allItems.add(new AdvancedSearchItem(1, "Thư viện nửa đêm", "Matt Haig", "Văn học", "Đã hoàn thành", "4.8", "Tiếng Việt", "TV"));
        allItems.add(new AdvancedSearchItem(2, "Nhà giả kim", "Paulo Coelho", "Kinh tế", "Đã hoàn thành", "4.9", "Tiếng Việt", "NK"));
        allItems.add(new AdvancedSearchItem(3, "Sức mạnh của thói quen", "Charles Duhigg", "Kỹ năng sống", "Đã hoàn thành", "4.7", "Tiếng Việt", "SM"));
        allItems.add(new AdvancedSearchItem(4, "Đắc nhân tâm", "Dale Carnegie", "Kỹ năng sống", "Đã hoàn thành", "4.9", "Tiếng Việt", "ĐN"));
        allItems.add(new AdvancedSearchItem(5, "Cây cam ngọt của tôi", "J. M. Vasconcelos", "Văn học", "Đã hoàn thành", "4.9", "Tiếng Việt", "CC"));
        allItems.add(new AdvancedSearchItem(6, "Khởi nghiệp tinh gọn", "Eric Ries", "Kinh tế", "Đang ra", "4.6", "Tiếng Việt", "KN"));
    }

    private void filterAdvancedResults(String q, String cat, String st) {
        List<AdvancedSearchItem> matched = new ArrayList<>();
        String lowerQuery = q.toLowerCase();

        for (AdvancedSearchItem item : allItems) {
            boolean matchesQuery = lowerQuery.isEmpty()
                    || item.getTitle().toLowerCase().contains(lowerQuery)
                    || item.getAuthor().toLowerCase().contains(lowerQuery);

            boolean matchesCategory = cat.equals("Tất cả thể loại") || cat.isEmpty()
                    || item.getCategory().equalsIgnoreCase(cat);

            boolean matchesStatus = st.equals("Tất cả")
                    || item.getStatus().equalsIgnoreCase(st);

            if (matchesQuery && matchesCategory && matchesStatus) {
                matched.add(item);
            }
        }

        adapter.setItems(matched);

        if (!matched.isEmpty()) {
            binding.rvAdvancedSearchResults.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
        } else {
            binding.rvAdvancedSearchResults.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        }
    }
}
