package com.example.group12;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.group12.adapter.CommunityBookAdapter;
import com.example.group12.adapter.CommunityBookItem;
import com.example.group12.databinding.ActivityCommunityLibraryBinding;

import java.util.ArrayList;
import java.util.List;

public class CommunityLibraryActivity extends AppCompatActivity {

    private ActivityCommunityLibraryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommunityLibraryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(CommunityLibraryActivity.this, UploadBookActivity.class);
            startActivity(intent);
        });

        binding.rvCommunityBooks.setLayoutManager(new LinearLayoutManager(this));
        CommunityBookAdapter adapter = new CommunityBookAdapter();
        binding.rvCommunityBooks.setAdapter(adapter);

        List<CommunityBookItem> communityBooks = new ArrayList<>();
        communityBooks.add(new CommunityBookItem("Kinh tế học vĩ mô thực chiến", "PGS. TS Trần Văn Minh", "Hoàng Nam", "KT"));
        communityBooks.add(new CommunityBookItem("Lập trình Java căn bản từ A-Z", "ThS. Lê Minh Quân", "Minh Thư", "JV"));
        communityBooks.add(new CommunityBookItem("Tâm lý học hành vi con người", "Dr. Robert Cialdini", "Tuấn Kiệt", "TL"));
        communityBooks.add(new CommunityBookItem("Nghệ thuật giao tiếp ứng xử", "Barbara Pachter", "Phương Linh", "GT"));
        communityBooks.add(new CommunityBookItem("Khởi nghiệp kinh doanh số", "Eric Ries", "Gia Huy", "KN"));
        communityBooks.add(new CommunityBookItem("Thiết kế UI/UX hiện đại", "Steve Krug", "Thu Hà", "UI"));
        communityBooks.add(new CommunityBookItem("Trí tuệ nhân tạo tương lai", "Max Tegmark", "Đức Anh", "AI"));
        communityBooks.add(new CommunityBookItem("Quản trị tài chính cá nhân", "Dave Ramsey", "Mai Anh", "TC"));

        adapter.setItems(communityBooks);

        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(CommunityLibraryActivity.this, BookDetailActivity.class);
            intent.putExtra("book_title", item.getTitle());
            intent.putExtra("book_author", item.getAuthor());
            startActivity(intent);
        });
    }
}
