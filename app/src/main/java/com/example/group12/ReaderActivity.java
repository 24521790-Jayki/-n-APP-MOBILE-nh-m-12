package com.example.group12;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.Voice;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.group12.api.ApiClient;
import com.example.group12.databinding.ActivityReaderBinding;
import com.example.group12.model.BookChaptersResponse;
import com.example.group12.model.BookContent;
import com.example.group12.model.Chapter;
import com.example.group12.model.ChapterContentResponse;
import com.example.group12.model.Paragraph;
import com.example.group12.model.Sentence;
import com.example.group12.service.TtsMediaSessionService;
import com.example.group12.ui.reader.ReaderParagraphAdapter;
import com.example.group12.ui.reader.ReaderViewModel;
import com.example.group12.ui.tts.TtsSettingsBottomSheet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReaderActivity extends AppCompatActivity
        implements TtsSettingsBottomSheet.TtsSettingsListener {

    private ActivityReaderBinding binding;
    private ReaderViewModel viewModel;
    private ReaderParagraphAdapter paragraphAdapter;

    private final String selectedText = "Có những bình yên chỉ tìm thấy khi ta thôi vội vã.";

    // Thong tin chuong va sach dang doc
    private int currentBookId = -1;
    private int currentChapterIndex = 0;
    private int totalChapters = 0;
    private BookContent currentBookContent;

    // Trang thai hien thi Header & Footer va SeekBar
    private boolean isHeaderFooterVisible = true;
    private boolean isUserScrollingSeekBar = false;
    private String currentThemeKey = "sepia";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReaderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khoi tao ViewModel
        viewModel = new ViewModelProvider(this).get(ReaderViewModel.class);

        // Khoi tao RecyclerView va Adapter cho doan van EPUB
        setupRecyclerView();

        // Nhan bookId duoc truyen sang
        int bookId = getIntent().getIntExtra("bookId", getIntent().getIntExtra("book_id", -1));
        int targetChapter = getIntent().getIntExtra("chapter_number", 1);

        if (savedInstanceState != null) {
            currentBookId = savedInstanceState.getInt("saved_book_id", -1);
            currentChapterIndex = savedInstanceState.getInt("saved_chapter_idx", 0);
            totalChapters = savedInstanceState.getInt("saved_total_chapters", 0);
            currentThemeKey = savedInstanceState.getString("saved_theme_key", "sepia");
        } else if (bookId != -1) {
            currentBookId = bookId;
            currentChapterIndex = Math.max(0, targetChapter - 1);
        }

        if (currentBookId != -1) {
            // Tai va phan tich file EPUB tu local hoac download tu server
            viewModel.loadOrDownloadBook(currentBookId);
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin sách!", Toast.LENGTH_SHORT).show();
        }

        // Quan sat LiveData tu ViewModel
        setupViewModelObservers();

        // Quan sat LiveData vi tri doc va trang thai phat tu TTS Service
        setupTtsObservers();

        // Khoi tao giao diện doc mac dinh
        applyTheme(currentThemeKey);

        // An cac cua so noi mac dinh
        binding.cardFloatingToolbar.setVisibility(View.GONE);
        binding.cardThemeSettings.setVisibility(View.GONE);

        // Lang nghe cac nut bam, hanh vi cuon (scroll) va thanh tien trinh
        setupClickListeners();
    }

    private void setupRecyclerView() {
        paragraphAdapter = new ReaderParagraphAdapter();
        binding.recyclerViewParagraphs.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewParagraphs.setAdapter(paragraphAdapter);

        // Khi nguoi dung cham vao mot doan van tren Reader UI -> Phat TTS tu doan van do
        paragraphAdapter.setOnParagraphClickListener(paragraphIndex -> {
            TtsMediaSessionService ttsService = TtsMediaSessionService.getInstance();
            if (ttsService != null) {
                ttsService.seekTo(currentChapterIndex, paragraphIndex, 0);
                ttsService.startReading();
                Toast.makeText(this, "Đang phát TTS từ đoạn " + (paragraphIndex + 1), Toast.LENGTH_SHORT).show();
            } else {
                startService(new Intent(this, TtsMediaSessionService.class));
                Toast.makeText(this, "Đang khởi tạo TTS Service...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupViewModelObservers() {
        // Quan sat noi dung sach EPUB parse duoc
        viewModel.getBookContentLive().observe(this, content -> {
            if (content != null && content.getChapters() != null && !content.getChapters().isEmpty()) {
                this.currentBookContent = content;
                this.totalChapters = content.getTotalChapters();

                binding.txtBookName.setText(content.getTitle());
                binding.txtBookTitle.setText(content.getTitle());

                // An webview HTML, hien thi RecyclerView doan van
                binding.webViewChapter.setVisibility(View.GONE);
                binding.recyclerViewParagraphs.setVisibility(View.VISIBLE);

                // Hien thi chuong hien tai
                displayChapter(currentChapterIndex);

                // Truyen noi dung sach sang TTS Service
                TtsMediaSessionService ttsService = TtsMediaSessionService.getInstance();
                if (ttsService != null) {
                    ttsService.setBookContent(content);
                }
            }
        });

        // Fallback neu tai hoac parse EPUB that bai -> dung API chapter html cu
        viewModel.getErrorMessage().observe(this, error -> {
            if (currentBookContent == null && currentBookId != -1) {
                loadBookChaptersFromApi(currentBookId);
            }
        });
    }

    private void setupTtsObservers() {
        // Dong bo vi tri TTS -> UI Highlight va auto scroll + cap nhat thanh tien trinh
        TtsMediaSessionService.getCurrentPositionLive().observe(this, position -> {
            if (position != null && position.chapterIndex == currentChapterIndex) {
                paragraphAdapter.setHighlight(position.paragraphIndex, position.sentenceIndex);

                if (currentBookContent != null && currentChapterIndex < currentBookContent.getChapters().size()) {
                    List<Paragraph> paras = currentBookContent.getChapters().get(currentChapterIndex).getParagraphs();
                    int totalParas = (paras != null && !paras.isEmpty()) ? paras.size() : 1;

                    if (totalParas > 1) {
                        int percent = Math.min(100, Math.max(0, Math.round(((float) position.paragraphIndex / (totalParas - 1)) * 100f)));
                        binding.seekBarProgress.setProgress(percent);
                    }
                    updateProgressText(position.paragraphIndex, totalParas);

                    if (!isUserScrollingSeekBar) {
                        scrollToParagraphIndex(position.paragraphIndex);
                    }
                }

                // Luu tien do doc
                viewModel.saveProgress(currentBookId, currentChapterIndex, position.paragraphIndex, position.sentenceIndex, 1.0f);
            }
        });

        // Cap nhat trang thai nut Play Audio
        TtsMediaSessionService.getPlayingStateLive().observe(this, isPlaying -> {
            // UI phan hoi trang thai phat giong noi
        });
    }

    private void displayChapter(int chapterIdx) {
        if (currentBookContent == null || currentBookContent.getChapters() == null) return;
        List<Chapter> chapters = currentBookContent.getChapters();

        if (chapterIdx < 0 || chapterIdx >= chapters.size()) return;

        currentChapterIndex = chapterIdx;
        Chapter chapter = chapters.get(chapterIdx);

        binding.txtChapterNumber.setText("Chương " + (chapterIdx + 1) + ": " + chapter.getTitle());
        paragraphAdapter.setParagraphs(chapter.getParagraphs());

        int totalParas = chapter.getParagraphs() != null ? chapter.getParagraphs().size() : 1;
        binding.seekBarProgress.setMax(100);
        binding.seekBarProgress.setProgress(0);
        updateProgressText(0, totalParas);

        applyTheme(currentThemeKey);

        // Cuon len dau trang khi sang chuong moi
        binding.nestedScrollView.post(() -> binding.nestedScrollView.scrollTo(0, 0));
    }

    private void updateProgressText(int currentParaIdx, int totalParas) {
        int parasCount = Math.max(1, totalParas);
        int percent = (int) (((currentParaIdx + 1) * 100.0f) / parasCount);
        percent = Math.min(100, Math.max(0, percent));
        binding.txtPageProgress.setText("Đoạn " + (currentParaIdx + 1) + "/" + parasCount + " - " + percent + "%");
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        // Click vao vung man hinh hoac doan van -> Bat/Tat Header va Progress bar
        binding.readerRootContainer.setOnClickListener(v -> toggleHeaderAndFooter());

        // Bat su kien cuon (scroll) tren NestedScrollView de tu dong an/hien va cap nhat tien do
        binding.nestedScrollView.setOnScrollChangeListener((View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) -> {
            int dy = scrollY - oldScrollY;

            // Vuot xuong -> An Header & Footer; Vuot len -> Hien Header & Footer
            if (dy > 12) {
                hideHeaderAndFooter();
            } else if (dy < -12) {
                showHeaderAndFooter();
            }

            // Cap nhat tien do doc tu dong theo vi tri vuot scrollY
            if (!isUserScrollingSeekBar) {
                updateProgressFromScroll();
            }
        });

        // Nut bat/tat doc TTS va mo cai dat TTS
        binding.btnAudio.setOnClickListener(v -> {
            TtsMediaSessionService ttsService = TtsMediaSessionService.getInstance();
            if (ttsService != null) {
                if (ttsService.isCurrentlyPlaying()) {
                    ttsService.pauseReading();
                    Toast.makeText(this, "Đã tạm dừng TTS", Toast.LENGTH_SHORT).show();
                } else {
                    if (currentBookContent != null) {
                        ttsService.setBookContent(currentBookContent);
                        ttsService.seekTo(currentChapterIndex, 0, 0);
                        ttsService.startReading();
                    }
                }
            } else {
                Intent intent = new Intent(this, TtsMediaSessionService.class);
                startService(intent);
                Toast.makeText(this, "Đang khởi tạo dịch vụ TTS...", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnAudio.setOnLongClickListener(v -> {
            openTtsSettingsBottomSheet();
            return true;
        });

        // Thanh trượt SeekBar tien trinh doc liên tục 0% -> 100%
        binding.seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    View child = binding.nestedScrollView.getChildAt(0);
                    if (child != null) {
                        int childHeight = child.getHeight();
                        int scrollViewHeight = binding.nestedScrollView.getHeight();
                        int maxScroll = Math.max(0, childHeight - scrollViewHeight);
                        int targetY = Math.round((progress / 100.0f) * maxScroll);

                        binding.nestedScrollView.scrollTo(0, targetY);

                        int totalParas = 1;
                        if (currentBookContent != null && currentChapterIndex < currentBookContent.getChapters().size()) {
                            List<Paragraph> paras = currentBookContent.getChapters().get(currentChapterIndex).getParagraphs();
                            if (paras != null && !paras.isEmpty()) {
                                totalParas = paras.size();
                            }
                        }
                        int currentParaIdx = Math.min(totalParas - 1, Math.round((progress / 100.0f) * (totalParas - 1)));
                        updateProgressText(currentParaIdx, totalParas);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserScrollingSeekBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserScrollingSeekBar = false;
                View child = binding.nestedScrollView.getChildAt(0);
                if (child != null) {
                    int childHeight = child.getHeight();
                    int scrollViewHeight = binding.nestedScrollView.getHeight();
                    int maxScroll = Math.max(0, childHeight - scrollViewHeight);
                    int targetY = Math.round((seekBar.getProgress() / 100.0f) * maxScroll);

                    binding.nestedScrollView.smoothScrollTo(0, targetY);

                    int totalParas = 1;
                    if (currentBookContent != null && currentChapterIndex < currentBookContent.getChapters().size()) {
                        List<Paragraph> paras = currentBookContent.getChapters().get(currentChapterIndex).getParagraphs();
                        if (paras != null && !paras.isEmpty()) {
                            totalParas = paras.size();
                        }
                    }
                    int currentParaIdx = Math.min(totalParas - 1, Math.round((seekBar.getProgress() / 100.0f) * (totalParas - 1)));

                    TtsMediaSessionService ttsService = TtsMediaSessionService.getInstance();
                    if (ttsService != null && ttsService.isCurrentlyPlaying()) {
                        ttsService.seekTo(currentChapterIndex, currentParaIdx, 0);
                        ttsService.startReading();
                    }
                }
            }
        });

        // Nut Aa - Mo bang cai dat giao dien
        binding.btnAa.setOnClickListener(v -> {
            showHeaderAndFooter();
            if (binding.cardThemeSettings.getVisibility() == View.VISIBLE) {
                binding.cardThemeSettings.setVisibility(View.GONE);
            } else {
                binding.cardThemeSettings.setVisibility(View.VISIBLE);
                binding.cardThemeSettings.bringToFront();
            }
        });

        binding.cardThemeSettings.setOnClickListener(v -> {});
        binding.cardFloatingToolbar.setOnClickListener(v -> {});

        binding.btnCloseSettings.setOnClickListener(v -> binding.cardThemeSettings.setVisibility(View.GONE));

        View.OnClickListener lightListener = v -> {
            applyTheme("light");
            binding.cardThemeSettings.setVisibility(View.GONE);
            Toast.makeText(this, "Chuyển sang giao diện Sáng", Toast.LENGTH_SHORT).show();
        };
        binding.themeLight.setOnClickListener(lightListener);
        binding.cardLight.setOnClickListener(lightListener);

        View.OnClickListener darkListener = v -> {
            applyTheme("dark");
            binding.cardThemeSettings.setVisibility(View.GONE);
            Toast.makeText(this, "Chuyển sang giao diện Tối (Nền đen)", Toast.LENGTH_SHORT).show();
        };
        binding.themeDark.setOnClickListener(darkListener);
        binding.cardDark.setOnClickListener(darkListener);

        View.OnClickListener sepiaListener = v -> {
            applyTheme("sepia");
            binding.cardThemeSettings.setVisibility(View.GONE);
            Toast.makeText(this, "Chuyển sang giao diện Vàng Dịu (Bảo vệ mắt)", Toast.LENGTH_SHORT).show();
        };
        binding.themeSepia.setOnClickListener(sepiaListener);
        binding.cardSepia.setOnClickListener(sepiaListener);

        View.OnClickListener systemListener = v -> {
            applyTheme("system");
            binding.cardThemeSettings.setVisibility(View.GONE);
            Toast.makeText(this, "Chuyển sang giao diện Mặc định Hệ thống", Toast.LENGTH_SHORT).show();
        };
        binding.themeSystem.setOnClickListener(systemListener);
        binding.cardSystem.setOnClickListener(systemListener);

        binding.btnTranslate.setOnClickListener(v -> {
            Intent intent = new Intent(this, AiDictionaryActivity.class);
            intent.putExtra("selected_text", selectedText);
            startActivity(intent);
        });

        binding.btnQuote.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuoteGeneratorActivity.class);
            intent.putExtra("selected_text", selectedText);
            startActivity(intent);
        });

        binding.btnAiChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, AiChatActivity.class);
            intent.putExtra("selected_text", selectedText);
            startActivity(intent);
        });

        binding.btnBookmark.setOnClickListener(v ->
                Toast.makeText(this, "Đã đánh dấu đoạn văn!", Toast.LENGTH_SHORT).show()
        );

        binding.btnPrevPage.setOnClickListener(v -> changeChapter(-1));
        binding.btnNextPage.setOnClickListener(v -> changeChapter(1));
    }

    private void updateProgressFromScroll() {
        View child = binding.nestedScrollView.getChildAt(0);
        if (child == null) return;

        int childHeight = child.getHeight();
        int scrollViewHeight = binding.nestedScrollView.getHeight();
        int maxScroll = childHeight - scrollViewHeight;

        if (maxScroll <= 0) return;

        int scrollY = binding.nestedScrollView.getScrollY();
        float ratio = Math.max(0f, Math.min(1.0f, (float) scrollY / maxScroll));
        int percent = Math.min(100, Math.max(0, Math.round(ratio * 100f)));

        binding.seekBarProgress.setMax(100);
        binding.seekBarProgress.setProgress(percent);

        int totalParas = 1;
        if (currentBookContent != null && currentChapterIndex < currentBookContent.getChapters().size()) {
            List<Paragraph> paras = currentBookContent.getChapters().get(currentChapterIndex).getParagraphs();
            if (paras != null && !paras.isEmpty()) {
                totalParas = paras.size();
            }
        }

        int currentParaIdx = Math.min(totalParas - 1, Math.round(ratio * (totalParas - 1)));
        updateProgressText(currentParaIdx, totalParas);
    }

    private void scrollToParagraphIndex(int targetParaIdx) {
        if (currentBookContent == null || currentBookContent.getChapters() == null) return;
        List<Chapter> chapters = currentBookContent.getChapters();
        if (currentChapterIndex < 0 || currentChapterIndex >= chapters.size()) return;

        Chapter chapter = chapters.get(currentChapterIndex);
        List<Paragraph> paras = chapter.getParagraphs();
        if (paras == null || paras.isEmpty()) return;

        int totalParas = paras.size();
        int safeIdx = Math.max(0, Math.min(totalParas - 1, targetParaIdx));

        View child = binding.nestedScrollView.getChildAt(0);
        if (child == null) return;

        int childHeight = child.getHeight();
        int scrollViewHeight = binding.nestedScrollView.getHeight();
        int maxScroll = Math.max(0, childHeight - scrollViewHeight);

        int targetY = 0;
        if (totalParas > 1) {
            targetY = Math.round(((float) safeIdx / (totalParas - 1)) * maxScroll);
        }

        binding.nestedScrollView.smoothScrollTo(0, targetY);
    }

    private void hideHeaderAndFooter() {
        if (!isHeaderFooterVisible) return;
        isHeaderFooterVisible = false;

        binding.topBar.animate()
                .translationY(-binding.topBar.getHeight() - 20)
                .alpha(0f)
                .setDuration(250)
                .withEndAction(() -> binding.topBar.setVisibility(View.GONE))
                .start();

        binding.bottomBar.animate()
                .translationY(binding.bottomBar.getHeight() + 20)
                .alpha(0f)
                .setDuration(250)
                .withEndAction(() -> binding.bottomBar.setVisibility(View.GONE))
                .start();

        binding.cardFloatingToolbar.setVisibility(View.GONE);
        binding.cardThemeSettings.setVisibility(View.GONE);
    }

    private void showHeaderAndFooter() {
        if (isHeaderFooterVisible) return;
        isHeaderFooterVisible = true;

        binding.topBar.setVisibility(View.VISIBLE);
        binding.topBar.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(250)
                .start();

        binding.bottomBar.setVisibility(View.VISIBLE);
        binding.bottomBar.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(250)
                .start();
    }

    private void toggleHeaderAndFooter() {
        if (isHeaderFooterVisible) {
            hideHeaderAndFooter();
        } else {
            showHeaderAndFooter();
        }
    }

    private void openTtsSettingsBottomSheet() {
        TtsMediaSessionService ttsService = TtsMediaSessionService.getInstance();
        TtsSettingsBottomSheet bottomSheet = new TtsSettingsBottomSheet();
        if (ttsService != null) {
            bottomSheet.setTts(ttsService.getTtsInstance());
            bottomSheet.setCurrentSpeed(ttsService.getCurrentSpeed());
        }
        bottomSheet.setListener(this);
        bottomSheet.show(getSupportFragmentManager(), "TtsSettings");
    }

    @Override
    public void onSpeedChanged(float speed) {
        TtsMediaSessionService ttsService = TtsMediaSessionService.getInstance();
        if (ttsService != null) {
            ttsService.setSpeed(speed);
        }
    }

    @Override
    public void onVoiceChanged(Voice voice) {
        TtsMediaSessionService ttsService = TtsMediaSessionService.getInstance();
        if (ttsService != null) {
            ttsService.setVoice(voice);
        }
    }

    @Override
    public void onEngineChanged(String enginePackage) {
        TtsMediaSessionService ttsService = TtsMediaSessionService.getInstance();
        if (ttsService != null) {
            ttsService.setEngine(enginePackage);
        }
        Toast.makeText(this, "Đã chuyển Engine TTS: " + enginePackage, Toast.LENGTH_SHORT).show();
    }

    private void changeChapter(int direction) {
        int nextChapter = currentChapterIndex + direction;

        if (nextChapter < 0) {
            Toast.makeText(this, "Đây là chương đầu tiên!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (totalChapters > 0 && nextChapter >= totalChapters) {
            Toast.makeText(this, "Đây là chương cuối cùng!", Toast.LENGTH_SHORT).show();
            return;
        }

        currentChapterIndex = nextChapter;
        if (currentBookContent != null && currentBookContent.getChapters() != null && currentChapterIndex < currentBookContent.getChapters().size()) {
            displayChapter(currentChapterIndex);
        } else {
            loadChapterContentFromApi(currentBookId, currentChapterIndex + 1);
        }
    }

    private void applyTheme(String themeKey) {
        if (binding == null) return;
        this.currentThemeKey = themeKey;

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

        int bgColor;
        int topBottomBarColor;
        int primaryTextColor;
        int subtitleTextColor;
        int paragraphTextColor;
        int highlightBgColor;

        switch (themeKey) {
            case "light":
                bgColor = Color.parseColor("#FFFFFF");
                topBottomBarColor = Color.parseColor("#FFFFFF");
                primaryTextColor = Color.parseColor("#111827");
                subtitleTextColor = Color.parseColor("#6B7280");
                paragraphTextColor = Color.parseColor("#1F2937");
                highlightBgColor = Color.parseColor("#FDE68A");

                binding.cardLight.setStrokeColor(Color.parseColor("#2563EB"));
                binding.cardLight.setStrokeWidth(6);
                binding.checkLight.setVisibility(View.VISIBLE);
                break;
            case "dark":
                bgColor = Color.parseColor("#1F2937");
                topBottomBarColor = Color.parseColor("#111827");
                primaryTextColor = Color.parseColor("#F9FAFB");
                subtitleTextColor = Color.parseColor("#9CA3AF");
                paragraphTextColor = Color.parseColor("#E5E7EB");
                highlightBgColor = Color.parseColor("#B45309");

                binding.cardDark.setStrokeColor(Color.parseColor("#2563EB"));
                binding.cardDark.setStrokeWidth(6);
                binding.checkDark.setVisibility(View.VISIBLE);
                break;
            case "sepia":
                bgColor = Color.parseColor("#FDF6E2");
                topBottomBarColor = Color.parseColor("#F7EED2");
                primaryTextColor = Color.parseColor("#78350F");
                subtitleTextColor = Color.parseColor("#92400E");
                paragraphTextColor = Color.parseColor("#291D09");
                highlightBgColor = Color.parseColor("#FDE68A");

                binding.cardSepia.setStrokeColor(Color.parseColor("#2563EB"));
                binding.cardSepia.setStrokeWidth(6);
                binding.checkSepia.setVisibility(View.VISIBLE);
                break;
            case "system":
            default:
                bgColor = Color.parseColor("#F8FAFC");
                topBottomBarColor = Color.parseColor("#FFFFFF");
                primaryTextColor = Color.parseColor("#111827");
                subtitleTextColor = Color.parseColor("#6B7280");
                paragraphTextColor = Color.parseColor("#1F2937");
                highlightBgColor = Color.parseColor("#FDE68A");

                binding.cardSystem.setStrokeColor(Color.parseColor("#2563EB"));
                binding.cardSystem.setStrokeWidth(6);
                binding.checkSystem.setVisibility(View.VISIBLE);
                break;
        }

        binding.readerRootContainer.setBackgroundColor(bgColor);
        binding.topBar.setBackgroundColor(topBottomBarColor);
        binding.bottomBar.setBackgroundColor(topBottomBarColor);

        binding.txtBookName.setTextColor(primaryTextColor);
        binding.txtChapterNumber.setTextColor(primaryTextColor);
        binding.txtBookTitle.setTextColor(subtitleTextColor);
        binding.txtPageProgress.setTextColor(primaryTextColor);

        if (paragraphAdapter != null) {
            paragraphAdapter.setTheme(paragraphTextColor, highlightBgColor);
        }
    }

    // Fallback qua cu API neu sach khong co file EPUB
    private void loadBookChaptersFromApi(int bookId) {
        ApiClient.getApiService().getBookChapters(bookId).enqueue(new Callback<BookChaptersResponse>() {
            @Override
            public void onResponse(Call<BookChaptersResponse> call, Response<BookChaptersResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    BookChaptersResponse.Data data = response.body().getData();
                    if (data != null && data.getChapters() != null && !data.getChapters().isEmpty()) {
                        binding.txtBookName.setText(data.getBook().getTitle());
                        binding.txtBookTitle.setText(data.getBook().getTitle());
                        totalChapters = data.getTotalChapters();
                        loadChapterContentFromApi(bookId, currentChapterIndex + 1);
                    }
                }
            }

            @Override
            public void onFailure(Call<BookChaptersResponse> call, Throwable t) {
                Toast.makeText(ReaderActivity.this, "Lỗi kết nối API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadChapterContentFromApi(int bookId, int chapterNumber) {
        binding.webViewChapter.setVisibility(View.GONE);
        binding.recyclerViewParagraphs.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getChapterContent(bookId, chapterNumber).enqueue(new Callback<ChapterContentResponse>() {
            @Override
            public void onResponse(Call<ChapterContentResponse> call, Response<ChapterContentResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ChapterContentResponse.Data data = response.body().getData();
                    if (data != null) {
                        BookContent content = buildBookContentFromApiChapter(data);
                        currentBookContent = content;
                        displayChapter(0);

                        TtsMediaSessionService ttsService = TtsMediaSessionService.getInstance();
                        if (ttsService != null) {
                            ttsService.setBookContent(content);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ChapterContentResponse> call, Throwable t) {
                Toast.makeText(ReaderActivity.this, "Lỗi load chương: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private BookContent buildBookContentFromApiChapter(ChapterContentResponse.Data data) {
        BookContent content = new BookContent();
        content.setBookId(currentBookId);
        content.setTitle(binding.txtBookName.getText().toString());

        Chapter chap = new Chapter();
        chap.setChapterIndex(currentChapterIndex);
        chap.setTitle("Chương " + data.getChapterNumber());

        List<Paragraph> paragraphs = new ArrayList<>();
        if (data.getContent() != null && !data.getContent().isEmpty()) {
            Document doc = Jsoup.parse(data.getContent());
            String rawText = getWholeTextFromElementHelper(doc.body() != null ? doc.body() : doc);

            String[] lines = rawText.split("\\r?\\n+");
            int paraIdx = 0;
            for (String rawLine : lines) {
                String formattedLine = formatParagraphLineHelper(rawLine);
                if (formattedLine.isEmpty()) continue;

                List<Sentence> sentences = splitSentencesHelper(formattedLine);
                paragraphs.add(new Paragraph("chap_" + currentChapterIndex + "_para_" + paraIdx, paraIdx, formattedLine, sentences));
                paraIdx++;
            }
        }

        if (paragraphs.isEmpty()) {
            List<Sentence> sentences = new ArrayList<>();
            sentences.add(new Sentence(0, "Không có nội dung chương", 0, 20));
            paragraphs.add(new Paragraph("chap_0_para_0", 0, "Không có nội dung chương", sentences));
        }

        chap.setParagraphs(paragraphs);
        List<Chapter> chapters = new ArrayList<>();
        chapters.add(chap);
        content.setChapters(chapters);

        return content;
    }

    private String formatParagraphLineHelper(String rawLine) {
        if (rawLine == null) return "";
        String line = Jsoup.parse(rawLine).text();
        String body = line.replaceAll("[ \\t\\u00A0]+", " ").trim();
        if (body.isEmpty()) return "";
        return "    " + body;
    }

    private String getWholeTextFromElementHelper(org.jsoup.nodes.Node node) {
        StringBuilder sb = new StringBuilder();
        appendNodeTextHelper(node, sb);
        return sb.toString();
    }

    private void appendNodeTextHelper(org.jsoup.nodes.Node node, StringBuilder sb) {
        if (node instanceof org.jsoup.nodes.TextNode) {
            String text = ((org.jsoup.nodes.TextNode) node).getWholeText();
            sb.append(text);
        } else if (node instanceof Element) {
            Element element = (Element) node;
            String tagName = element.tagName().toLowerCase();

            if (tagName.equals("br")) {
                sb.append("\n");
                return;
            }

            boolean isBlock = element.isBlock() || tagName.equals("p") || tagName.equals("div")
                    || tagName.startsWith("h") || tagName.equals("li") || tagName.equals("blockquote");

            if (isBlock && sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n') {
                sb.append("\n");
            }

            for (org.jsoup.nodes.Node child : element.childNodes()) {
                appendNodeTextHelper(child, sb);
            }

            if (isBlock && sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n') {
                sb.append("\n");
            }
        }
    }

    private List<Sentence> splitSentencesHelper(String text) {
        List<Sentence> sentences = new ArrayList<>();
        String[] rawSentences = text.split("(?<=[.!?…])\\s+");
        int sentIdx = 0;
        int charStart = 0;
        for (String raw : rawSentences) {
            if (raw.trim().isEmpty()) continue;
            int charEnd = charStart + raw.length();
            sentences.add(new Sentence(sentIdx++, raw.trim(), charStart, charEnd));
            charStart = charEnd + 1;
        }
        return sentences;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("saved_book_id", currentBookId);
        outState.putInt("saved_chapter_idx", currentChapterIndex);
        outState.putInt("saved_total_chapters", totalChapters);
        outState.putString("saved_theme_key", currentThemeKey);
    }
}
