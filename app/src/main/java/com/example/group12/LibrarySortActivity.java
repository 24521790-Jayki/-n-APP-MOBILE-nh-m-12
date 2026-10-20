package com.example.group12;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Window;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityLibrarySortBinding;

public class LibrarySortActivity extends AppCompatActivity {

    private ActivityLibrarySortBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = ActivityLibrarySortBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnCloseSort.setOnClickListener(v -> finish());
        binding.btnApplySort.setOnClickListener(v -> finish());

        RadioButton[] rbs = {
                binding.rbNewest,
                binding.rbOldest,
                binding.rbTitleAZ,
                binding.rbAuthorAZ,
                binding.rbRecentRead
        };

        for (RadioButton rb : rbs) {
            rb.setOnClickListener(v -> {
                for (RadioButton other : rbs) {
                    boolean selected = (other == rb);
                    other.setChecked(selected);
                    if (selected) {
                        other.setTextColor(Color.parseColor("#2563EB"));
                        other.setTypeface(null, Typeface.BOLD);
                    } else {
                        other.setTextColor(Color.parseColor("#1F2937"));
                        other.setTypeface(null, Typeface.NORMAL);
                    }
                }
            });
        }
    }
}
