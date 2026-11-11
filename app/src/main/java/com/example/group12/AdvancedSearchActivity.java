package com.example.group12;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityAdvancedSearchBinding;

public class AdvancedSearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAdvancedSearchBinding binding = ActivityAdvancedSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Đặt lại lọc
        binding.btnReset.setOnClickListener(v -> {
            if (binding.edtKeyword != null) {
                binding.edtKeyword.setText("");
            }
            if (binding.dropdownCategory != null) {
                binding.dropdownCategory.setText("");
            }
            Toast.makeText(this, "Đã đặt lại bộ lọc", Toast.LENGTH_SHORT).show();
        });

        // Nút Áp dụng bộ lọc -> Mở màn hình Kết quả tìm kiếm nâng cao riêng biệt
        binding.btnApplyFilter.setOnClickListener(v -> {
            String query = binding.edtKeyword != null && binding.edtKeyword.getText() != null
                    ? binding.edtKeyword.getText().toString().trim() : "";
            String category = binding.dropdownCategory != null && binding.dropdownCategory.getText() != null
                    ? binding.dropdownCategory.getText().toString().trim() : "";

            Intent intent = new Intent(AdvancedSearchActivity.this, AdvancedSearchResultsActivity.class);
            intent.putExtra("query", query);
            intent.putExtra("category", category);
            intent.putExtra("sort_by", "Liên quan nhất");
            intent.putExtra("status", "Tất cả");
            startActivity(intent);
            Toast.makeText(this, "Đang lọc nâng cao...", Toast.LENGTH_SHORT).show();
        });
    }
}
