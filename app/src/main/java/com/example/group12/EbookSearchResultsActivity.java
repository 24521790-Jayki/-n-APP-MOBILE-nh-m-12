package com.example.group12;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.group12.adapter.SearchResultAdapter;
import com.example.group12.adapter.SearchResultItem;
import com.example.group12.databinding.ActivityEbookSearchResultsBinding;

import java.util.ArrayList;
import java.util.List;

public class EbookSearchResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityEbookSearchResultsBinding binding = ActivityEbookSearchResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        binding.rvEbookResults.setLayoutManager(new LinearLayoutManager(this));
        SearchResultAdapter adapter = new SearchResultAdapter();
        binding.rvEbookResults.setAdapter(adapter);

        List<SearchResultItem> ebooks = new ArrayList<>();
        ebooks.add(new SearchResultItem("Thư viện nửa đêm", "Matt Haig", "E-Book", "4.8", "(120 đánh giá)", "TV"));
        ebooks.add(new SearchResultItem("Thư viện ánh sáng", "Nguyễn Nhật Ánh", "E-Book", "4.7", "(210 đánh giá)", "TA"));
        ebooks.add(new SearchResultItem("Thư viện thời gian", "Marc Levy", "E-Book", "4.5", "(67 đánh giá)", "TT"));
        ebooks.add(new SearchResultItem("Hành trình trong thư viện cổ", "Carlos Ruiz Zafón", "E-Book", "4.9", "(310 đánh giá)", "HT"));

        adapter.setItems(ebooks);

        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(EbookSearchResultsActivity.this, BookDetailActivity.class);
            intent.putExtra("book_title", item.getTitle());
            intent.putExtra("book_author", item.getAuthor());
            startActivity(intent);
        });
    }
}
