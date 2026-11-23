package com.example.group12;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.group12.api.ApiClient;
import com.example.group12.model.BookChaptersResponse;
import com.example.group12.model.ChapterContentResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.databinding.ActivityReaderBinding;

public class ReaderActivity extends AppCompatActivity {

    private ActivityReaderBinding binding;
    private final String selectedText = "Có những bình yên chỉ tìm thấy khi ta thôi vội vã.";

    // Thông tin chương đang đọc
    private int currentBookId = -1;
    private int currentChapterNumber = 1;
    private int totalChapters = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReaderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nhận bookId được truyền từ LibraryActivity
        int bookId = getIntent().getIntExtra("bookId", -1);

        if (bookId != -1) {
            currentBookId = bookId;
            loadBookContent(bookId);
        } else {
            Toast.makeText(this,
                    "Không tìm thấy thông tin sách!",
                    Toast.LENGTH_SHORT).show();
        }

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

        // Chuyển chương trước
        binding.btnPrevPage.setOnClickListener(v -> {
            changeChapter(-1);
        });

// Chuyển chương tiếp theo
        binding.btnNextPage.setOnClickListener(v -> {
            changeChapter(1);
        });

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

    private void loadBookContent(int bookId) {

        ApiClient.getApiService().getBookChapters(bookId)
                .enqueue(new Callback<BookChaptersResponse>() {
                    @Override
                    public void onResponse(
                            Call<BookChaptersResponse> call,
                            Response<BookChaptersResponse> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            BookChaptersResponse.Data data =
                                    response.body().getData();

                            if (data == null || data.getChapters() == null
                                    || data.getChapters().isEmpty()) {
                                Toast.makeText(ReaderActivity.this,
                                        "Sách chưa có chương nào!",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String bookTitle = data.getBook().getTitle();

                            // Hiển thị tên sách
                            binding.txtBookName.setText(bookTitle);
                            binding.txtBookTitle.setText(bookTitle);

                            // Lưu tổng số chương
                            totalChapters = data.getTotalChapters();

                            // Lấy chương đầu tiên
                            currentChapterNumber = data.getChapters()
                                    .get(0).getChapterNumber();

                            loadChapterContent(bookId, currentChapterNumber);

                        } else {
                            Toast.makeText(ReaderActivity.this,
                                    "Không thể tải danh sách chương!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<BookChaptersResponse> call,
                            Throwable t) {

                        Toast.makeText(ReaderActivity.this,
                                "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadChapterContent(int bookId, int chapterNumber) {

        ApiClient.getApiService()
                .getChapterContent(bookId, chapterNumber)
                .enqueue(new Callback<ChapterContentResponse>() {

                    @Override
                    public void onResponse(
                            Call<ChapterContentResponse> call,
                            Response<ChapterContentResponse> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            ChapterContentResponse.Data data =
                                    response.body().getData();

                            if (data == null) return;

                            // Cập nhật chương hiện tại
                            currentChapterNumber = data.getChapterNumber();

                            binding.txtChapterNumber.setText(
                                    "Chương " + currentChapterNumber
                            );

                            String html =
                                    "<html><head><meta charset='UTF-8'>" +
                                            "<meta name='viewport' content='width=device-width, initial-scale=1'>" +
                                            "<style>" +
                                            "body { font-family: serif; font-size: 18px; " +
                                            "line-height: 1.8; color: #374151; " +
                                            "white-space: pre-wrap; margin: 0; }" +
                                            "</style></head><body>" +
                                            data.getContent() +
                                            "</body></html>";

                            binding.webViewChapter.setWebViewClient(
                                    new WebViewClient()
                            );

                            binding.webViewChapter.loadDataWithBaseURL(
                                    null,
                                    html,
                                    "text/html",
                                    "UTF-8",
                                    null
                            );

                        } else {
                            Toast.makeText(ReaderActivity.this,
                                    "Không thể tải nội dung chương!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ChapterContentResponse> call,
                            Throwable t) {

                        Toast.makeText(ReaderActivity.this,
                                "Lỗi tải nội dung: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void changeChapter(int direction) {

        int nextChapter = currentChapterNumber + direction;

        // Kiểm tra giới hạn chương
        if (nextChapter < 1) {
            Toast.makeText(this,
                    "Đây là chương đầu tiên!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (nextChapter > totalChapters) {
            Toast.makeText(this,
                    "Đây là chương cuối cùng!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Tải nội dung chương mới
        loadChapterContent(currentBookId, nextChapter);
    }
}
