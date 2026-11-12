package com.example.group12;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.group12.adapter.MyUploadedBookAdapter;
import com.example.group12.adapter.MyUploadedBookItem;
import com.example.group12.databinding.ActivityMyUploadedBooksBinding;

import java.util.ArrayList;
import java.util.List;

public class MyUploadedBooksActivity extends AppCompatActivity {

    private ActivityMyUploadedBooksBinding binding;
    private MyUploadedBookAdapter adapter;
    private List<MyUploadedBookItem> allBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyUploadedBooksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnAddBook.setOnClickListener(v -> {
            Intent intent = new Intent(MyUploadedBooksActivity.this, UploadBookActivity.class);
            startActivity(intent);
        });

        adapter = new MyUploadedBookAdapter();
        binding.rvMyUploadedBooks.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMyUploadedBooks.setAdapter(adapter);

        initSampleData();
        filterBooks("all");

        binding.chipAll.setOnClickListener(v -> {
            updateChipStyles("all");
            filterBooks("all");
        });

        binding.chipApproved.setOnClickListener(v -> {
            updateChipStyles("approved");
            filterBooks("Đã duyệt");
        });

        binding.chipPending.setOnClickListener(v -> {
            updateChipStyles("pending");
            filterBooks("Đang chờ duyệt");
        });

        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(MyUploadedBooksActivity.this, BookDetailActivity.class);
            intent.putExtra("book_title", item.getTitle());
            intent.putExtra("book_author", item.getAuthor());
            startActivity(intent);
        });
    }

    private void initSampleData() {
        allBooks = new ArrayList<>();
        // Đã duyệt (8)
        allBooks.add(new MyUploadedBookItem("Lịch sử kinh tế Việt Nam thế kỷ 20", "Trần Văn A", "10/11/2024", "Đã duyệt", "LS"));
        allBooks.add(new MyUploadedBookItem("Cẩm nang lập trình Kotlin nâng cao", "Nguyễn Văn B", "05/11/2024", "Đã duyệt", "KT"));
        allBooks.add(new MyUploadedBookItem("Tuyển tập thơ tình xuân thu", "Lê Thị C", "01/11/2024", "Đã duyệt", "TH"));
        allBooks.add(new MyUploadedBookItem("Phương pháp học tập thông minh", "Phạm Văn D", "28/10/2024", "Đã duyệt", "PP"));
        allBooks.add(new MyUploadedBookItem("Nhập môn Trí tuệ nhân tạo", "Hoàng Văn E", "20/10/2024", "Đã duyệt", "AI"));
        allBooks.add(new MyUploadedBookItem("Kỹ năng thuyết trình trước đám đông", "Vũ Thị F", "15/10/2024", "Đã duyệt", "KN"));
        allBooks.add(new MyUploadedBookItem("Nghệ thuật quản lý thời gian", "Đỗ Văn G", "10/10/2024", "Đã duyệt", "QT"));
        allBooks.add(new MyUploadedBookItem("Hành trình khám phá vũ trụ", "Bùi Thị H", "05/10/2024", "Đã duyệt", "VT"));
        // Đang chờ duyệt (2)
        allBooks.add(new MyUploadedBookItem("Tư duy phản biện trong thời đại số", "Nguyễn Văn A", "12/11/2024", "Đang chờ duyệt", "TD"));
        allBooks.add(new MyUploadedBookItem("Kinh nghiệm thực chiến DevOps cho Startup", "Trần Văn B", "11/11/2024", "Đang chờ duyệt", "DV"));
    }

    private void updateChipStyles(String type) {
        int inactiveBg = Color.parseColor("#F1F5F9");
        int inactiveText = Color.parseColor("#4B5563");

        binding.chipAll.setBackgroundTintList(ColorStateList.valueOf(inactiveBg));
        binding.chipAll.setTextColor(inactiveText);
        binding.chipAll.setTypeface(null, Typeface.NORMAL);

        binding.chipApproved.setBackgroundTintList(ColorStateList.valueOf(inactiveBg));
        binding.chipApproved.setTextColor(inactiveText);
        binding.chipApproved.setTypeface(null, Typeface.NORMAL);

        binding.chipPending.setBackgroundTintList(ColorStateList.valueOf(inactiveBg));
        binding.chipPending.setTextColor(inactiveText);
        binding.chipPending.setTypeface(null, Typeface.NORMAL);

        TextView activeChip = binding.chipAll;
        if (type.equals("approved")) activeChip = binding.chipApproved;
        else if (type.equals("pending")) activeChip = binding.chipPending;

        activeChip.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2563EB")));
        activeChip.setTextColor(Color.parseColor("#FFFFFF"));
        activeChip.setTypeface(null, Typeface.BOLD);
    }

    private void filterBooks(String status) {
        List<MyUploadedBookItem> filtered = new ArrayList<>();
        for (MyUploadedBookItem item : allBooks) {
            if (status.equals("all") || item.getStatus().equals(status)) {
                filtered.add(item);
            }
        }
        adapter.setItems(filtered);

        if (!filtered.isEmpty()) {
            binding.rvMyUploadedBooks.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
        } else {
            binding.rvMyUploadedBooks.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        }
    }
}
