package com.example.group12;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.group12.adapter.AdvancedSearchAdapter;
import com.example.group12.adapter.AdvancedSearchItem;
import com.example.group12.databinding.ActivityAdvancedSearchResultsBinding;

import java.util.ArrayList;
import java.util.List;

public class AdvancedSearchResultsActivity extends AppCompatActivity {

    private ActivityAdvancedSearchResultsBinding binding;
    private AdvancedSearchAdapter adapter;
    private List<AdvancedSearchItem> allItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdvancedSearchResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup RecyclerView
        adapter = new AdvancedSearchAdapter();
        binding.rvAdvancedSearchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdvancedSearchResults.setAdapter(adapter);

        // Khởi tạo dữ liệu mẫu
        initSampleData();

        // Nhận dữ liệu từ AdvancedSearchActivity
        String query = getIntent().getStringExtra("query");
        String category = getIntent().getStringExtra("category");
        String sortBy = getIntent().getStringExtra("sort_by");
        String status = getIntent().getStringExtra("status");

        if (query == null) query = "";
        if (category == null || category.isEmpty()) category = "Tất cả thể loại";
        if (sortBy == null) sortBy = "Liên quan nhất";
        if (status == null || status.isEmpty()) status = "Tất cả";

        // Hiển thị tóm tắt tiêu chí lọc
        String summary = "Từ khóa: \"" + (query.isEmpty() ? "Tất cả" : query) + "\"" +
                " • Thể loại: " + category +
                " • Trạng thái: " + status +
                " • Sắp xếp: " + sortBy;
        binding.tvFilterSummaryText.setText(summary);

        // Lọc danh sách theo tiêu chí nâng cao
        filterAdvancedResults(query, category, status);

        // Nút Back
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Edit Filter -> Quay lại màn hình AdvancedSearchActivity
        binding.btnEditFilter.setOnClickListener(v -> finish());

        // Click vào item -> Mở BookDetailActivity
        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(AdvancedSearchResultsActivity.this, BookDetailActivity.class);
            intent.putExtra("book_title", item.getTitle());
            intent.putExtra("book_author", item.getAuthor());
            startActivity(intent);
        });
    }

    private void initSampleData() {
        allItems = new ArrayList<>();
        allItems.add(new AdvancedSearchItem("Thư viện nửa đêm", "Matt Haig", "Văn học", "Đã hoàn thành", "4.8", "Tiếng Việt", "TV"));
        allItems.add(new AdvancedSearchItem("Nhà giả kim", "Paulo Coelho", "Kinh tế", "Đã hoàn thành", "4.9", "Tiếng Việt", "NK"));
        allItems.add(new AdvancedSearchItem("Sức mạnh của thói quen", "Charles Duhigg", "Kỹ năng sống", "Đã hoàn thành", "4.7", "Tiếng Việt", "SM"));
        allItems.add(new AdvancedSearchItem("Đắc nhân tâm", "Dale Carnegie", "Kỹ năng sống", "Đã hoàn thành", "4.9", "Tiếng Việt", "ĐN"));
        allItems.add(new AdvancedSearchItem("Cây cam ngọt của tôi", "J. M. Vasconcelos", "Văn học", "Đã hoàn thành", "4.9", "Tiếng Việt", "CC"));
        allItems.add(new AdvancedSearchItem("Khởi nghiệp tinh gọn", "Eric Ries", "Kinh tế", "Đang ra", "4.6", "Tiếng Việt", "KN"));
    }

    private void filterAdvancedResults(String query, String category, String status) {
        List<AdvancedSearchItem> matched = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (AdvancedSearchItem item : allItems) {
            boolean matchesQuery = lowerQuery.isEmpty() || item.getTitle().toLowerCase().contains(lowerQuery) || item.getAuthor().toLowerCase().contains(lowerQuery);
            boolean matchesCategory = category.equals("Tất cả thể loại") || category.isEmpty() || item.getCategory().equalsIgnoreCase(category);
            boolean matchesStatus = status.equals("Tất cả") || item.getStatus().equalsIgnoreCase(status);

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
