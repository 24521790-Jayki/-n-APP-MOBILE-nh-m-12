package com.example.group12;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.api.ApiClient;
import com.example.group12.api.CategoriesApi;
import com.example.group12.api.CategoriesResponse;
import com.example.group12.api.Category;
import com.example.group12.api.SessionManager;
import com.example.group12.databinding.ActivityAdvancedSearchBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdvancedSearchActivity extends AppCompatActivity {

    private ActivityAdvancedSearchBinding binding;
    private final List<String> categoryNames = new ArrayList<>();

    private String selectedSort = "Liên quan nhất";
    private String selectedStatus = "Tất cả";
    private String selectedRating = "Mọi đánh giá";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdvancedSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Tải danh mục từ server
        loadCategories();

        // Thiết lập sự kiện cho dropdown thể loại
        binding.dropdownCategory.setOnClickListener(v -> binding.dropdownCategory.showDropDown());
        binding.dropdownCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.dropdownCategory.showDropDown();
            }
        });

        // Thiết lập nhóm Sắp xếp
        setupSortChips();

        // Thiết lập nhóm Trạng thái
        setupStatusChips();

        // Thiết lập nhóm Đánh giá
        setupRatingButtons();

        // Nút Đặt lại lọc
        binding.btnReset.setOnClickListener(v -> resetFilters());

        // Nút Áp dụng bộ lọc
        binding.btnApplyFilter.setOnClickListener(v -> applyFilters());
    }

    private void setupSortChips() {
        binding.chipSortRelevant.setOnClickListener(v -> selectSort("Liên quan nhất"));
        binding.chipSortUpdated.setOnClickListener(v -> selectSort("Mới cập nhật"));
        binding.chipSortRating.setOnClickListener(v -> selectSort("Đánh giá cao"));
        binding.chipSortViews.setOnClickListener(v -> selectSort("Lượt xem nhiều"));
        updateSortChipStyles();
    }

    private void selectSort(String sort) {
        selectedSort = sort;
        updateSortChipStyles();
    }

    private void updateSortChipStyles() {
        setChipStyle(binding.chipSortRelevant, "Liên quan nhất".equals(selectedSort));
        setChipStyle(binding.chipSortUpdated, "Mới cập nhật".equals(selectedSort));
        setChipStyle(binding.chipSortRating, "Đánh giá cao".equals(selectedSort));
        setChipStyle(binding.chipSortViews, "Lượt xem nhiều".equals(selectedSort));
    }

    private void setupStatusChips() {
        binding.chipStatusAll.setOnClickListener(v -> selectStatus("Tất cả"));
        binding.chipStatusCompleted.setOnClickListener(v -> selectStatus("Đã hoàn thành"));
        binding.chipStatusOngoing.setOnClickListener(v -> selectStatus("Đang ra"));
        updateStatusChipStyles();
    }

    private void selectStatus(String status) {
        selectedStatus = status;
        updateStatusChipStyles();
    }

    private void updateStatusChipStyles() {
        setChipStyle(binding.chipStatusAll, "Tất cả".equals(selectedStatus));
        setChipStyle(binding.chipStatusCompleted, "Đã hoàn thành".equals(selectedStatus));
        setChipStyle(binding.chipStatusOngoing, "Đang ra".equals(selectedStatus));
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

    private void setupRatingButtons() {
        binding.btnRating4.setOnClickListener(v -> selectRating("⭐ 4+"));
        binding.btnRating3.setOnClickListener(v -> selectRating("⭐ 3+"));
        binding.btnRatingAll.setOnClickListener(v -> selectRating("Mọi đánh giá"));
        updateRatingButtonStyles();
    }

    private void selectRating(String rating) {
        selectedRating = rating;
        updateRatingButtonStyles();
    }

    private void updateRatingButtonStyles() {
        setRatingButtonStyle(binding.btnRating4, "⭐ 4+".equals(selectedRating));
        setRatingButtonStyle(binding.btnRating3, "⭐ 3+".equals(selectedRating));
        setRatingButtonStyle(binding.btnRatingAll, "Mọi đánh giá".equals(selectedRating));
    }

    private void setRatingButtonStyle(MaterialButton button, boolean isActive) {
        if (button == null) return;
        if (isActive) {
            button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DBEAFE")));
            button.setTextColor(Color.parseColor("#2563EB"));
            button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#2563EB")));
            button.setStrokeWidth(1);
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            button.setTextColor(Color.parseColor("#4B5563"));
            button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E5E7EB")));
            button.setStrokeWidth(1);
        }
    }

    private void resetFilters() {
        if (binding.edtKeyword != null) {
            binding.edtKeyword.setText("");
        }
        if (binding.dropdownCategory != null) {
            binding.dropdownCategory.setText("");
        }
        selectedSort = "Liên quan nhất";
        selectedStatus = "Tất cả";
        selectedRating = "Mọi đánh giá";

        updateSortChipStyles();
        updateStatusChipStyles();
        updateRatingButtonStyles();

        Toast.makeText(this, "Đã đặt lại bộ lọc", Toast.LENGTH_SHORT).show();
    }

    private void applyFilters() {
        String query = binding.edtKeyword != null && binding.edtKeyword.getText() != null
                ? binding.edtKeyword.getText().toString().trim() : "";
        String category = binding.dropdownCategory != null && binding.dropdownCategory.getText() != null
                ? binding.dropdownCategory.getText().toString().trim() : "";

        Intent intent = new Intent(AdvancedSearchActivity.this, AdvancedSearchResultsActivity.class);
        intent.putExtra("query", query);
        intent.putExtra("category", category);
        intent.putExtra("sort_by", selectedSort);
        intent.putExtra("status", selectedStatus);
        intent.putExtra("rating", selectedRating);
        startActivity(intent);

        Toast.makeText(this, "Đang áp dụng bộ lọc nâng cao...", Toast.LENGTH_SHORT).show();
    }

    private void loadCategories() {
        categoryNames.clear();
        categoryNames.add("Tất cả thể loại");

        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();

        if (token != null && !token.isEmpty()) {
            CategoriesApi api = ApiClient.getClient().create(CategoriesApi.class);
            api.getCategories("Bearer " + token).enqueue(new Callback<CategoriesResponse>() {
                @Override
                public void onResponse(Call<CategoriesResponse> call, Response<CategoriesResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<Category> list = response.body().getData();
                        if (list != null) {
                            for (Category cat : list) {
                                categoryNames.add(cat.getCategoryName());
                            }
                        }
                    }
                    setupCategoryDropdown();
                }

                @Override
                public void onFailure(Call<CategoriesResponse> call, Throwable t) {
                    setupCategoryDropdown();
                }
            });
        } else {
            setupCategoryDropdown();
        }
    }

    private void setupCategoryDropdown() {
        if (binding.dropdownCategory != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    categoryNames
            );
            binding.dropdownCategory.setAdapter(adapter);
        }
    }
}
