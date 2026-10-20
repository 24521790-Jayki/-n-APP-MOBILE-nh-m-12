package com.example.group12;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivitySignUpErrorBinding;

public class SignUpErrorActivity extends AppCompatActivity {

    private ActivitySignUpErrorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpErrorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvNavLogin.setOnClickListener(v -> finish());

        binding.btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
            finishAffinity();
        });
    }
}
