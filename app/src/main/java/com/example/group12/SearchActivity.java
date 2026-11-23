package com.example.group12;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivitySearchBinding;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySearchBinding binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nhấn tìm kiếm từ bàn phím trên ô nhập
        binding.edtSearchKey.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                String query = binding.edtSearchKey.getText().toString().trim();
                Intent intent = new Intent(SearchActivity.this, SearchResultsActivity.class);
                intent.putExtra("search_query", query);
                startActivity(intent);
                return true;
            }
            return false;
        });

        // Nút Mở Tìm kiếm Nâng cao
        binding.btnAdvancedSearch.setOnClickListener(v -> {
            Intent intent = new Intent(SearchActivity.this, AdvancedSearchActivity.class);
            startActivity(intent);
        });
    }
}
