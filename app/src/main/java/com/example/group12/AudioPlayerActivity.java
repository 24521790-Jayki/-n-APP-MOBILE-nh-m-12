package com.example.group12;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.speech.tts.TextToSpeech;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

import android.util.TypedValue;

import com.example.group12.api.ApiClient;
import com.example.group12.databinding.ActivityAudioPlayerBinding;
import com.example.group12.model.BookChaptersResponse;
import com.example.group12.model.Chapter;
import com.example.group12.model.ChapterContentResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AudioPlayerActivity extends AppCompatActivity {

    private static final int BOOK_ID = 23;

    private ActivityAudioPlayerBinding binding;

    // Bộ máy chuyển văn bản thành giọng nói
    private TextToSpeech textToSpeech;

    // Kiểm tra TTS đã khởi tạo thành công chưa
    private boolean isTtsReady = false;

    // Lưu chương đang được chọn
    private int selectedChapterNumber = 0;
    private String selectedChapterTitle = "";
    private String selectedChapterContent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAudioPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút Playlist
        binding.btnPlaylist.setOnClickListener(v -> showChapterDialog());

        // Khởi tạo TextToSpeech
        initializeTextToSpeech();
    }

    private void showChapterDialog() {

        BottomSheetDialog sheetDialog =
                new BottomSheetDialog(AudioPlayerActivity.this);

        View view = LayoutInflater.from(AudioPlayerActivity.this)
                .inflate(R.layout.dialog_chapters, null);

        sheetDialog.setContentView(view);
        sheetDialog.show();

        LinearLayout chapterContainer =
                view.findViewById(R.id.chapterContainer);

        TextView txtBookSubtitle =
                view.findViewById(R.id.txtBookSubtitle);

        // Hiển thị trạng thái đang tải
        txtBookSubtitle.setText("Đang tải danh sách chương...");

        // Gọi API lấy danh sách chương
        ApiClient.getApiService()
                .getBookChapters(BOOK_ID)
                .enqueue(new Callback<BookChaptersResponse>() {

                    @Override
                    public void onResponse(
                            Call<BookChaptersResponse> call,
                            Response<BookChaptersResponse> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            BookChaptersResponse result = response.body();

                            BookChaptersResponse.Data data = result.getData();

                            if (data == null || data.getChapters() == null) {
                                txtBookSubtitle.setText(
                                        "Không có dữ liệu chương"
                                );
                                return;
                            }

                            List<Chapter> chapters = data.getChapters();

                            String bookTitle = data.getBook() != null
                                    ? data.getBook().getTitle()
                                    : "Sách";

                            txtBookSubtitle.setText(
                                    bookTitle + " • " + data.getTotalChapters() + " chương"
                            );

                            chapterContainer.removeAllViews();

                            // Tạo từng dòng chương từ dữ liệu API
                            for (Chapter chapter : chapters) {
                                addChapterItem(
                                        chapterContainer,
                                        chapter,
                                        sheetDialog
                                );
                            }

                        } else {
                            txtBookSubtitle.setText(
                                    "Không thể tải danh sách chương"
                            );

                            Toast.makeText(
                                    AudioPlayerActivity.this,
                                    "Lỗi tải danh sách chương",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<BookChaptersResponse> call,
                            Throwable t) {

                        txtBookSubtitle.setText(
                                "Không thể kết nối đến máy chủ"
                        );

                        Toast.makeText(
                                AudioPlayerActivity.this,
                                "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void addChapterItem(
            LinearLayout container,
            Chapter chapter,
            BottomSheetDialog sheetDialog) {

        LinearLayout row = new LinearLayout(this);

        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(0, dpToPx(12), 0, dpToPx(12));

        TypedValue outValue = new TypedValue();

        getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground,
                outValue,
                true
        );

        if (outValue.resourceId != 0) {
            row.setBackgroundResource(outValue.resourceId);
        }

        TextView titleView = new TextView(this);

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        titleView.setLayoutParams(titleParams);
        titleView.setText(
                "Chương " + chapter.getChapterNumber()
                        + ": " + chapter.getTitle()
        );

        titleView.setTextColor(
                chapter.getChapterNumber() == selectedChapterNumber
                        ? 0xFF2563EB
                        : 0xFF1F2937
        );

        titleView.setTextSize(14);
        titleView.setMaxLines(3);

        row.addView(titleView);
        container.addView(row);

        // Đường phân cách giữa các chương
        View divider = new View(this);

        LinearLayout.LayoutParams dividerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(1)
                );

        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(0xFFF3F4F6);

        container.addView(divider);

        // Khi nhấn chương, gọi API lấy nội dung chương
        row.setOnClickListener(v -> {
            loadChapterContent(
                    chapter.getChapterNumber(),
                    sheetDialog
            );
        });
    }

    private void loadChapterContent(
            int chapterNumber,
            BottomSheetDialog sheetDialog) {

        Toast.makeText(
                this,
                "Đang tải nội dung chương " + chapterNumber,
                Toast.LENGTH_SHORT
        ).show();

        ApiClient.getApiService()
                .getChapterContent(BOOK_ID, chapterNumber)
                .enqueue(new Callback<ChapterContentResponse>() {

                    @Override
                    public void onResponse(
                            Call<ChapterContentResponse> call,
                            Response<ChapterContentResponse> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()
                                && response.body().getData() != null) {

                            ChapterContentResponse.Data data =
                                    response.body().getData();

                            selectedChapterNumber = data.getChapterNumber();
                            selectedChapterTitle = data.getTitle();
                            selectedChapterContent = data.getContent();

                            Toast.makeText(
                                    AudioPlayerActivity.this,
                                    "Đã tải chương " + selectedChapterNumber,
                                    Toast.LENGTH_SHORT
                            ).show();

                            sheetDialog.dismiss();

                        } else {
                            Toast.makeText(
                                    AudioPlayerActivity.this,
                                    "Không thể tải nội dung chương",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ChapterContentResponse> call,
                            Throwable t) {

                        Toast.makeText(
                                AudioPlayerActivity.this,
                                "Lỗi tải nội dung: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources()
                .getDisplayMetrics().density);
    }

    private void initializeTextToSpeech() {

        textToSpeech = new TextToSpeech(
                this,
                status -> {

                    if (status == TextToSpeech.SUCCESS) {

                        // Thiết lập ngôn ngữ tiếng Việt
                        int result = textToSpeech.setLanguage(
                                new Locale("vi", "VN")
                        );

                        if (result == TextToSpeech.LANG_MISSING_DATA
                                || result == TextToSpeech.LANG_NOT_SUPPORTED) {

                            isTtsReady = false;

                            Toast.makeText(
                                    AudioPlayerActivity.this,
                                    "Thiết bị chưa hỗ trợ giọng đọc tiếng Việt",
                                    Toast.LENGTH_LONG
                            ).show();

                        } else {

                            isTtsReady = true;

                            Toast.makeText(
                                    AudioPlayerActivity.this,
                                    "Khởi tạo giọng đọc thành công",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                    } else {

                        isTtsReady = false;

                        Toast.makeText(
                                AudioPlayerActivity.this,
                                "Không thể khởi tạo TextToSpeech",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }
}

