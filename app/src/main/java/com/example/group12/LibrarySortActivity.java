package com.example.group12;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    public static final String PREF_NAME = "library_prefs";
    public static final String KEY_SORT_TYPE = "pref_sort_type";
    public static final String KEY_SORT_NAME = "pref_sort_name";

    public static final String EXTRA_SORT_TYPE = "extra_sort_type";
    public static final String EXTRA_SORT_NAME = "extra_sort_name";

    private final String[] sortNames = {
            "Mới nhất",
            "Cũ nhất",
            "Tên A-Z",
            "Tác giả A-Z",
            "Đọc gần đây"
    };

    private int selectedIndex = 0;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        ActivityLibrarySortBinding binding = ActivityLibrarySortBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Load saved sort index or from intent
        selectedIndex = getIntent().getIntExtra(EXTRA_SORT_TYPE, preferences.getInt(KEY_SORT_TYPE, 0));
        if (selectedIndex < 0 || selectedIndex >= sortNames.length) {
            selectedIndex = 0;
        }

        binding.btnCloseSort.setOnClickListener(v -> finish());

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

        // Set initial selected state
        selectSortOption(selectedIndex, rbs, layouts);

        binding.btnApplySort.setOnClickListener(v -> {
            String selectedName = sortNames[selectedIndex];

            preferences.edit()
                    .putInt(KEY_SORT_TYPE, selectedIndex)
                    .putString(KEY_SORT_NAME, selectedName)
                    .apply();

            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_SORT_TYPE, selectedIndex);
            resultIntent.putExtra(EXTRA_SORT_NAME, selectedName);
            setResult(RESULT_OK, resultIntent);

            Toast.makeText(this, "Đã áp dụng sắp xếp: " + selectedName, Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setupSortClick(View layout, RadioButton rb, int index, RadioButton[] rbs, View[] layouts) {
        View.OnClickListener listener = v -> selectSortOption(index, rbs, layouts);
        layout.setOnClickListener(listener);
        rb.setOnClickListener(listener);
    }

    private void selectSortOption(int newIndex, RadioButton[] rbs, View[] layouts) {
        this.selectedIndex = newIndex;
        for (int i = 0; i < rbs.length; i++) {
            updateItem(i, newIndex, rbs, layouts);
        }
    }

    private void updateItem(int itemIndex, int curSelectedIndex, RadioButton[] rbs, View[] layouts) {
        boolean selected = (itemIndex == curSelectedIndex);
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
