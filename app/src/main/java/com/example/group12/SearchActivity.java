package com.example.group12;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivitySearchBinding;
import com.google.android.material.chip.Chip;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private String searchType = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Hiển thị / ẩn nút xóa từ khóa khi gõ
        binding.edtSearchKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Nút Xóa từ khóa
        binding.btnClearSearch.setOnClickListener(v -> {
            binding.edtSearchKey.setText("");
            binding.edtSearchKey.requestFocus();
        });

        // Nút Tìm (Button "Tìm" + Click Icon kính lúp)
        binding.btnSubmitSearch.setOnClickListener(v ->
                performSearch(binding.edtSearchKey.getText().toString().trim())
        );

        binding.btnSearchIcon.setOnClickListener(v ->
                performSearch(binding.edtSearchKey.getText().toString().trim())
        );

        // Nhấn tìm kiếm từ bàn phím
        binding.edtSearchKey.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch(binding.edtSearchKey.getText().toString().trim());
                return true;
            }
            return false;
        });

        // Chọn loại tìm kiếm (Tất cả / Tên sách / Tác giả / Thể loại)
        binding.chipAll.setOnClickListener(v -> selectSearchType("all"));
        binding.chipTitle.setOnClickListener(v -> selectSearchType("title"));
        binding.chipAuthor.setOnClickListener(v -> selectSearchType("author"));
        binding.chipCategory.setOnClickListener(v -> selectSearchType("category"));
        updateSearchChipStyles("all");

        // Lịch sử tìm kiếm
        binding.itemHistory1.setOnClickListener(v -> {
            String q = binding.tvHistory1.getText().toString().trim();
            binding.edtSearchKey.setText(q);
            performSearch(q);
        });

        binding.itemHistory2.setOnClickListener(v -> {
            String q = binding.tvHistory2.getText().toString().trim();
            binding.edtSearchKey.setText(q);
            performSearch(q);
        });

        binding.btnClearHistory.setOnClickListener(v -> {
            binding.layoutHistorySection.setVisibility(View.GONE);
            Toast.makeText(this, "Đã xóa lịch sử tìm kiếm", Toast.LENGTH_SHORT).show();
        });

        // Tìm kiếm phổ biến (Trending)
        setupTrendingChip(binding.chipTrending1);
        setupTrendingChip(binding.chipTrending2);
        setupTrendingChip(binding.chipTrending3);
        setupTrendingChip(binding.chipTrending4);
        setupTrendingChip(binding.chipTrending5);

        // Nút Mở Tìm kiếm Nâng cao
        binding.btnAdvancedSearch.setOnClickListener(v -> {
            Intent intent = new Intent(SearchActivity.this, AdvancedSearchActivity.class);
            startActivity(intent);
        });
    }

    private void selectSearchType(String type) {
        this.searchType = type;
        updateSearchChipStyles(type);
    }

    private void updateSearchChipStyles(String activeType) {
        setChipStyle(binding.chipAll, "all".equals(activeType));
        setChipStyle(binding.chipTitle, "title".equals(activeType));
        setChipStyle(binding.chipAuthor, "author".equals(activeType));
        setChipStyle(binding.chipCategory, "category".equals(activeType));
    }

    private void setChipStyle(Chip chip, boolean isActive) {
        if (chip == null) return;
        if (isActive) {
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#DBEAFE")));
            chip.setTextColor(Color.parseColor("#2563EB"));
            chip.setTypeface(null, Typeface.BOLD);
            chip.setChipStrokeWidth(0);
        } else {
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            chip.setTextColor(Color.parseColor("#4B5563"));
            chip.setTypeface(null, Typeface.NORMAL);
            chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#E5E7EB")));
            chip.setChipStrokeWidth(1);
        }
    }

    private void setupTrendingChip(Chip chip) {
        if (chip != null) {
            chip.setOnClickListener(v -> {
                String query = chip.getText().toString();
                binding.edtSearchKey.setText(query);
                performSearch(query);
            });
        }
    }

    private void performSearch(String query) {
        Intent intent = new Intent(SearchActivity.this, SearchResultsActivity.class);
        intent.putExtra("search_query", query);
        intent.putExtra("filter_type", searchType);
        startActivity(intent);
    }
}
