package com.example.group12;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.group12.adapter.SearchResultAdapter;
import com.example.group12.adapter.SearchResultItem;
import com.example.group12.databinding.ActivityAudioLibraryBinding;

import java.util.ArrayList;
import java.util.List;

public class AudioLibraryActivity extends AppCompatActivity {

    private ActivityAudioLibraryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAudioLibraryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        binding.rvAudioBooks.setLayoutManager(new LinearLayoutManager(this));
        SearchResultAdapter adapter = new SearchResultAdapter();
        binding.rvAudioBooks.setAdapter(adapter);

        List<SearchResultItem> audioBooks = new ArrayList<>();
        audioBooks.add(new SearchResultItem("Đắc nhân tâm (Sách nói)", "Dale Carnegie", "Sách nói", "4.9", "(350 đánh giá)", "ĐN"));
        audioBooks.add(new SearchResultItem("Nhà giả kim (Sách nói)", "Paulo Coelho", "Sách nói", "4.9", "(500 đánh giá)", "NK"));
        audioBooks.add(new SearchResultItem("Tuổi trẻ đáng giá bao nhiêu (Sách nói)", "Rosie Nguyễn", "Sách nói", "4.8", "(410 đánh giá)", "TT"));
        audioBooks.add(new SearchResultItem("Sức mạnh của tiềm thức (Sách nói)", "Joseph Murphy", "Sách nói", "4.9", "(620 đánh giá)", "SM"));
        audioBooks.add(new SearchResultItem("Quảng gánh lo đi và vui sống (Sách nói)", "Dale Carnegie", "Sách nói", "4.7", "(190 đánh giá)", "QG"));
        audioBooks.add(new SearchResultItem("Đời thay đổi khi chúng ta thay đổi (Sách nói)", "Andrew Matthews", "Sách nói", "4.6", "(150 đánh giá)", "ĐT"));
        audioBooks.add(new SearchResultItem("Tư duy nhanh và chậm (Sách nói)", "Daniel Kahneman", "Sách nói", "4.9", "(880 đánh giá)", "TD"));
        audioBooks.add(new SearchResultItem("Sáng tạo tốc độ cao (Sách nói)", "Ed Catmull", "Sách nói", "4.8", "(230 đánh giá)", "ST"));

        adapter.setItems(audioBooks);

        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(AudioLibraryActivity.this, AudioPlayerActivity.class);
            startActivity(intent);
        });
    }
}
