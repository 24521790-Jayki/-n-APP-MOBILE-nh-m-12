package com.example.group12;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityUploadBookBinding;

public class UploadBookActivity extends AppCompatActivity {

    private ActivityUploadBookBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnSubmitReview.setOnClickListener(v -> {
            Toast.makeText(this, "Gửi duyệt sách thành công!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
