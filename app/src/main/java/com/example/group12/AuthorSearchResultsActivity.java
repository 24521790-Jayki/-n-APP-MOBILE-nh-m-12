package com.example.group12;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.group12.adapter.SearchResultAdapter;
import com.example.group12.adapter.SearchResultItem;
import com.example.group12.databinding.ActivityAuthorSearchResultsBinding;

import java.util.ArrayList;
import java.util.List;

public class AuthorSearchResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAuthorSearchResultsBinding binding = ActivityAuthorSearchResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        binding.rvAuthorResults.setLayoutManager(new LinearLayoutManager(this));
        SearchResultAdapter adapter = new SearchResultAdapter();
        binding.rvAuthorResults.setAdapter(adapter);

        List<SearchResultItem> authorsBooks = new ArrayList<>();
        authorsBooks.add(new SearchResultItem("Matt Haig (Tác giả)", "Tác giả bestseller thế giới", "Tác giả", "4.9", "(1,240 đánh giá)", "MH"));
        authorsBooks.add(new SearchResultItem("Nguyễn Nhật Ánh", "Nhà văn Việt Nam nổi tiếng", "Tác giả", "5.0", "(5,600 đánh giá)", "NA"));
        authorsBooks.add(new SearchResultItem("Dale Carnegie", "Chuyên gia đào tạo & Tác giả", "Tác giả", "4.9", "(3,400 đánh giá)", "DC"));

        adapter.setItems(authorsBooks);

        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(AuthorSearchResultsActivity.this, BookDetailActivity.class);
            intent.putExtra("book_title", item.getTitle());
            intent.putExtra("book_author", item.getAuthor());
            startActivity(intent);
        });
    }
}
