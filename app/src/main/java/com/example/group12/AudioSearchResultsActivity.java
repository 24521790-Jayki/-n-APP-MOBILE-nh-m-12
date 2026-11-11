package com.example.group12;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.group12.adapter.SearchResultAdapter;
import com.example.group12.adapter.SearchResultItem;
import com.example.group12.databinding.ActivityAudioSearchResultsBinding;

import java.util.ArrayList;
import java.util.List;

public class AudioSearchResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAudioSearchResultsBinding binding = ActivityAudioSearchResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        binding.rvAudioResults.setLayoutManager(new LinearLayoutManager(this));
        SearchResultAdapter adapter = new SearchResultAdapter();
        binding.rvAudioResults.setAdapter(adapter);

        List<SearchResultItem> audioBooks = new ArrayList<>();
        audioBooks.add(new SearchResultItem("Thư viện kỳ diệu của Mr. Lemoncello", "Chris Grabenstein", "Sách nói", "4.9", "(95 đánh giá)", "ML"));
        audioBooks.add(new SearchResultItem("Bí mật ở thư viện thành phố", "Agatha Christie", "Sách nói", "4.6", "(84 đánh giá)", "BM"));
        audioBooks.add(new SearchResultItem("Đắc nhân tâm (Audio Book)", "Dale Carnegie", "Sách nói", "4.9", "(350 đánh giá)", "ĐN"));

        adapter.setItems(audioBooks);

        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(AudioSearchResultsActivity.this, BookDetailActivity.class);
            intent.putExtra("book_title", item.getTitle());
            intent.putExtra("book_author", item.getAuthor());
            startActivity(intent);
        });
    }
}
