package com.example.group12;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityReaderBinding;

public class ReaderActivity extends AppCompatActivity {

    private ActivityReaderBinding binding;
    private final String selectedText = "Có những bình yên chỉ tìm thấy khi ta thôi vội vã.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReaderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo giao diện đọc mặc định (Bảo vệ mắt - Sepia)
        applyTheme("sepia");

        // Ẩn mặc định các cửa sổ nổi
        binding.cardFloatingToolbar.setVisibility(View.GONE);
        binding.cardThemeSettings.setVisibility(View.GONE);

        // Nút Quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Mở Sách Nói
        binding.btnAudio.setOnClickListener(v -> {
            Intent intent = new Intent(ReaderActivity.this, AudioPlayerActivity.class);
            startActivity(intent);
        });

        // Nút Aa - Bật/tắt cài đặt giao diện đọc
        binding.btnAa.setOnClickListener(v -> {
            int visibility = binding.cardThemeSettings.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
            binding.cardThemeSettings.setVisibility(visibility);
        });

        binding.btnCloseSettings.setOnClickListener(v -> binding.cardThemeSettings.setVisibility(View.GONE));

        // Nhấp chọn văn bản trên màn hình ➔ Hiển thị Floating Toolbar
        binding.readerRootContainer.setOnClickListener(v -> {
            int vis = binding.cardFloatingToolbar.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
            binding.cardFloatingToolbar.setVisibility(vis);
        });

        // Floating Toolbar Actions:
        // 1. Tra từ / Dịch thuật -> Mở AiDictionaryActivity với câu từ đã chọn
        binding.btnTranslate.setOnClickListener(v -> {
            Intent intent = new Intent(ReaderActivity.this, AiDictionaryActivity.class);
            intent.putExtra("selected_text", selectedText);
            startActivity(intent);
        });

        // 2. Tạo ảnh Quote -> Mở QuoteGeneratorActivity với câu từ đã chọn
        binding.btnQuote.setOnClickListener(v -> {
            Intent intent = new Intent(ReaderActivity.this, QuoteGeneratorActivity.class);
            intent.putExtra("selected_text", selectedText);
            startActivity(intent);
        });

        // 3. Hỏi đáp AI -> Mở AiChatActivity với câu từ đã chọn
        binding.btnAiChat.setOnClickListener(v -> {
            Intent intent = new Intent(ReaderActivity.this, AiChatActivity.class);
            intent.putExtra("selected_text", selectedText);
            startActivity(intent);
        });

        // 4. Đánh dấu
        binding.btnBookmark.setOnClickListener(v -> 
            Toast.makeText(this, "Đã đánh dấu đoạn văn bản!", Toast.LENGTH_SHORT).show()
        );

        // Chuyển trang
        binding.btnPrevPage.setOnClickListener(v -> 
            Toast.makeText(this, "Trang trước", Toast.LENGTH_SHORT).show()
        );

        binding.btnNextPage.setOnClickListener(v -> 
            Toast.makeText(this, "Trang tiếp theo", Toast.LENGTH_SHORT).show()
        );

        // Chuyển chế độ đọc / theme
        binding.themeLight.setOnClickListener(v -> {
            applyTheme("light");
            Toast.makeText(this, "Đã chuyển sang giao diện Sáng (Light)", Toast.LENGTH_SHORT).show();
            binding.cardThemeSettings.setVisibility(View.GONE);
        });

        binding.themeDark.setOnClickListener(v -> {
            applyTheme("dark");
            Toast.makeText(this, "Đã chuyển sang giao diện Tối (Dark)", Toast.LENGTH_SHORT).show();
            binding.cardThemeSettings.setVisibility(View.GONE);
        });

        binding.themeSepia.setOnClickListener(v -> {
            applyTheme("sepia");
            Toast.makeText(this, "Đã chuyển sang giao diện Bảo vệ mắt (Sepia)", Toast.LENGTH_SHORT).show();
            binding.cardThemeSettings.setVisibility(View.GONE);
        });

        binding.themeSystem.setOnClickListener(v -> {
            applyTheme("system");
            Toast.makeText(this, "Đã chuyển theo giao diện Hệ thống", Toast.LENGTH_SHORT).show();
            binding.cardThemeSettings.setVisibility(View.GONE);
        });
    }

    private void applyTheme(String themeKey) {
        if (binding == null) return;

        // Bỏ viền xanh và ẩn tích trên tất cả các thẻ
        binding.cardLight.setStrokeColor(Color.parseColor("#E2E8F0"));
        binding.cardLight.setStrokeWidth(1);
        binding.checkLight.setVisibility(View.GONE);

        binding.cardDark.setStrokeColor(Color.parseColor("#E2E8F0"));
        binding.cardDark.setStrokeWidth(0);
        binding.checkDark.setVisibility(View.GONE);

        binding.cardSepia.setStrokeColor(Color.parseColor("#E2E8F0"));
        binding.cardSepia.setStrokeWidth(0);
        binding.checkSepia.setVisibility(View.GONE);

        binding.cardSystem.setStrokeColor(Color.parseColor("#E2E8F0"));
        binding.cardSystem.setStrokeWidth(1);
        binding.checkSystem.setVisibility(View.GONE);

        switch (themeKey) {
            case "light":
                binding.readerRootContainer.setBackgroundColor(Color.parseColor("#FFFFFF"));
                binding.cardLight.setStrokeColor(Color.parseColor("#2563EB"));
                binding.cardLight.setStrokeWidth(6);
                binding.checkLight.setVisibility(View.VISIBLE);
                break;
            case "dark":
                binding.readerRootContainer.setBackgroundColor(Color.parseColor("#1F2937"));
                binding.cardDark.setStrokeColor(Color.parseColor("#2563EB"));
                binding.cardDark.setStrokeWidth(6);
                binding.checkDark.setVisibility(View.VISIBLE);
                break;
            case "sepia":
                binding.readerRootContainer.setBackgroundColor(Color.parseColor("#FDF6E2"));
                binding.cardSepia.setStrokeColor(Color.parseColor("#2563EB"));
                binding.cardSepia.setStrokeWidth(6);
                binding.checkSepia.setVisibility(View.VISIBLE);
                break;
            case "system":
            default:
                binding.readerRootContainer.setBackgroundColor(Color.parseColor("#F8FAFC"));
                binding.cardSystem.setStrokeColor(Color.parseColor("#2563EB"));
                binding.cardSystem.setStrokeWidth(6);
                binding.checkSystem.setVisibility(View.VISIBLE);
                break;
        }
    }
}
