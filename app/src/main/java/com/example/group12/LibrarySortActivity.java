package com.example.group12;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityLibrarySortBinding;

public class LibrarySortActivity extends AppCompatActivity {

    private int selectedSortIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        ActivityLibrarySortBinding binding = ActivityLibrarySortBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        selectedSortIndex = getSharedPreferences("LibraryPrefs", MODE_PRIVATE).getInt("sort_index", 0);

        binding.btnCloseSort.setOnClickListener(v -> finish());
        binding.btnApplySort.setOnClickListener(v -> {
            String sortText = "";
            switch (selectedSortIndex) {
                case 0: sortText = "Mới nhất"; break;
                case 1: sortText = "Cũ nhất"; break;
                case 2: sortText = "Tên sách (A–Z)"; break;
                case 3: sortText = "Tên tác giả (A–Z)"; break;
                case 4: sortText = "Đọc gần đây"; break;
            }
            getSharedPreferences("LibraryPrefs", MODE_PRIVATE)
                    .edit()
                    .putInt("sort_index", selectedSortIndex)
                    .putString("sort_text", sortText)
                    .apply();
            Toast.makeText(this, "Đã áp dụng sắp xếp: " + sortText, Toast.LENGTH_SHORT).show();
            finish();
        });

        RadioButton[] rbs = {
                binding.rbNewest,
                binding.rbOldest,
                binding.rbTitleAZ,
                binding.rbAuthorAZ,
                binding.rbRecentRead
        };

        View[] layouts = {
                binding.layoutNewest,
                binding.layoutOldest,
                binding.layoutTitleAZ,
                binding.layoutAuthorAZ,
                binding.layoutRecentRead
        };

        setupSortClick(binding.layoutNewest, binding.rbNewest, 0, rbs, layouts);
        setupSortClick(binding.layoutOldest, binding.rbOldest, 1, rbs, layouts);
        setupSortClick(binding.layoutTitleAZ, binding.rbTitleAZ, 2, rbs, layouts);
        setupSortClick(binding.layoutAuthorAZ, binding.rbAuthorAZ, 3, rbs, layouts);
        setupSortClick(binding.layoutRecentRead, binding.rbRecentRead, 4, rbs, layouts);

        selectSortOption(selectedSortIndex, rbs, layouts);
    }

    private void setupSortClick(View layout, RadioButton rb, int index, RadioButton[] rbs, View[] layouts) {
        View.OnClickListener listener = v -> {
            selectedSortIndex = index;
            selectSortOption(index, rbs, layouts);
        };
        layout.setOnClickListener(listener);
        rb.setOnClickListener(listener);
    }

    private void selectSortOption(int selectedIndex, RadioButton[] rbs, View[] layouts) {
        updateItem(0, selectedIndex, rbs, layouts);
        updateItem(1, selectedIndex, rbs, layouts);
        updateItem(2, selectedIndex, rbs, layouts);
        updateItem(3, selectedIndex, rbs, layouts);
        updateItem(4, selectedIndex, rbs, layouts);
    }

    private void updateItem(int itemIndex, int selectedIndex, RadioButton[] rbs, View[] layouts) {
        boolean selected = (itemIndex == selectedIndex);
        rbs[itemIndex].setChecked(selected);
        if (selected) {
            rbs[itemIndex].setTextColor(Color.parseColor("#2563EB"));
            rbs[itemIndex].setTypeface(null, Typeface.BOLD);
            layouts[itemIndex].setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EEF2FF")));
        } else {
            rbs[itemIndex].setTextColor(Color.parseColor("#1F2937"));
            rbs[itemIndex].setTypeface(null, Typeface.NORMAL);
            layouts[itemIndex].setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        }
    }
}
