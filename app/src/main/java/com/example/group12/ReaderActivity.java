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

    private final String selectedText = "Co nhung binh yen chi tim thay khi ta thoi voi va.";

    // Thong tin chuong va sach dang doc
    private int currentBookId = -1;
    private int currentChapterIndex = 0;
    private int totalChapters = 0;
    private BookContent currentBookContent;

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
        } else if (bookId != -1) {
            currentBookId = bookId;
            currentChapterIndex = Math.max(0, targetChapter - 1);
        }

        if (currentBookId != -1) {
            // Tai va phan tich file EPUB tu local hoac download tu server
            viewModel.loadOrDownloadBook(currentBookId);
        } else {
            Toast.makeText(this, "Khong tim thay thong tin sach!", Toast.LENGTH_SHORT).show();
        }

        // Quan sat LiveData tu ViewModel
        setupViewModelObservers();

        // Quan sat LiveData vi tri doc va trang thai phat tu TTS Service
        setupTtsObservers();

        // Khoi tao giao diện doc mac dinh (Sepia)
        applyTheme("sepia");

        // An cac cua so noi mac dinh
        binding.cardFloatingToolbar.setVisibility(View.GONE);
        binding.cardThemeSettings.setVisibility(View.GONE);

        // Lang nghe cac nut bam va thanh trượt tiến độ
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
                Toast.makeText(this, "Dang phat TTS tu doan " + (paragraphIndex + 1), Toast.LENGTH_SHORT).show();
            } else {
                startService(new Intent(this, TtsMediaSessionService.class));
                Toast.makeText(this, "Dang khoi tao TTS Service...", Toast.LENGTH_SHORT).show();
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
                binding.recyclerViewParagraphs.smoothScrollToPosition(position.paragraphIndex);

                if (currentBookContent != null && currentChapterIndex < currentBookContent.getChapters().size()) {
                    int totalParas = currentBookContent.getChapters().get(currentChapterIndex).getParagraphs().size();
                    binding.seekBarProgress.setProgress(position.paragraphIndex);
                    updateProgressText(position.paragraphIndex, totalParas);
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

        binding.txtChapterNumber.setText("Chuong " + (chapterIdx + 1) + ": " + chapter.getTitle());
        paragraphAdapter.setParagraphs(chapter.getParagraphs());

        int totalParas = chapter.getParagraphs() != null ? chapter.getParagraphs().size() : 1;
        binding.seekBarProgress.setMax(Math.max(1, totalParas - 1));
        binding.seekBarProgress.setProgress(0);
        updateProgressText(0, totalParas);
    }

    private void updateProgressText(int currentParaIdx, int totalParas) {
        int parasCount = Math.max(1, totalParas);
        int percent = (int) (((currentParaIdx + 1) * 100.0f) / parasCount);
        percent = Math.min(100, Math.max(0, percent));
        binding.txtPageProgress.setText("Doan " + (currentParaIdx + 1) + "/" + parasCount + " - " + percent + "%");
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        // Nut bat/tat doc TTS va mo cai dat TTS
        binding.btnAudio.setOnClickListener(v -> {
            TtsMediaSessionService ttsService = TtsMediaSessionService.getInstance();
            if (ttsService != null) {
                if (ttsService.isCurrentlyPlaying()) {
                    ttsService.pauseReading();
                    Toast.makeText(this, "Da tam dung TTS", Toast.LENGTH_SHORT).show();
                } else {
                    if (currentBookContent != null) {
                        ttsService.setBookContent(currentBookContent);
                        ttsService.seekTo(currentChapterIndex, binding.seekBarProgress.getProgress(), 0);
                        ttsService.startReading();
                    }
                }
            } else {
                Intent intent = new Intent(this, TtsMediaSessionService.class);
                startService(intent);
                Toast.makeText(this, "Dang khoi tao dich vu TTS...", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnAudio.setOnLongClickListener(v -> {
            openTtsSettingsBottomSheet();
            return true;
        });

        // Thanh trượt SeekBar tien trinh doc
        binding.seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && currentBookContent != null && currentChapterIndex < currentBookContent.getChapters().size()) {
                    int totalParas = currentBookContent.getChapters().get(currentChapterIndex).getParagraphs().size();
                    updateProgressText(progress, totalParas);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int targetPara = seekBar.getProgress();
                binding.recyclerViewParagraphs.scrollToPosition(targetPara);

                TtsMediaSessionService ttsService = TtsMediaSessionService.getInstance();
                if (ttsService != null && ttsService.isCurrentlyPlaying()) {
                    ttsService.seekTo(currentChapterIndex, targetPara, 0);
                    ttsService.startReading();
                }
            }
        });

        // Nut Aa - Mo bang cai dat giao dien
        binding.btnAa.setOnClickListener(v -> {
            int vis = binding.cardThemeSettings.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
            binding.cardThemeSettings.setVisibility(vis);
        });

        binding.btnCloseSettings.setOnClickListener(v -> binding.cardThemeSettings.setVisibility(View.GONE));

        // Click root container mo floating toolbar
        binding.readerRootContainer.setOnClickListener(v -> {
            int vis = binding.cardFloatingToolbar.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
            binding.cardFloatingToolbar.setVisibility(vis);
        });

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
                Toast.makeText(this, "Da danh dau doan van!", Toast.LENGTH_SHORT).show()
        );

        binding.btnPrevPage.setOnClickListener(v -> changeChapter(-1));
        binding.btnNextPage.setOnClickListener(v -> changeChapter(1));

        binding.themeLight.setOnClickListener(v -> {
            applyTheme("light");
            binding.cardThemeSettings.setVisibility(View.GONE);
        });

        binding.themeDark.setOnClickListener(v -> {
            applyTheme("dark");
            binding.cardThemeSettings.setVisibility(View.GONE);
        });

        binding.themeSepia.setOnClickListener(v -> {
            applyTheme("sepia");
            binding.cardThemeSettings.setVisibility(View.GONE);
        });

        binding.themeSystem.setOnClickListener(v -> {
            applyTheme("system");
            binding.cardThemeSettings.setVisibility(View.GONE);
        });
    }

    private void openTtsSettingsBottomSheet() {
        TtsMediaSessionService ttsService = TtsMediaSessionService.getInstance();
        TtsSettingsBottomSheet bottomSheet = new TtsSettingsBottomSheet();
        if (ttsService != null) {
            bottomSheet.setTts(ttsService.getTtsInstance());
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
        Toast.makeText(this, "Da chuyen Engine TTS: " + enginePackage, Toast.LENGTH_SHORT).show();
    }

    private void changeChapter(int direction) {
        int nextChapter = currentChapterIndex + direction;

        if (nextChapter < 0) {
            Toast.makeText(this, "Day la chuong dau tien!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (totalChapters > 0 && nextChapter >= totalChapters) {
            Toast.makeText(this, "Day la chuong cuoi cung!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ReaderActivity.this, "Loi ket noi API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ReaderActivity.this, "Loi load chuong: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private BookContent buildBookContentFromApiChapter(ChapterContentResponse.Data data) {
        BookContent content = new BookContent();
        content.setBookId(currentBookId);
        content.setTitle(binding.txtBookName.getText().toString());

        Chapter chap = new Chapter();
        chap.setChapterIndex(currentChapterIndex);
        chap.setTitle("Chuong " + data.getChapterNumber());

        List<Paragraph> paragraphs = new ArrayList<>();
        if (data.getContent() != null && !data.getContent().isEmpty()) {
            Document doc = Jsoup.parse(data.getContent());
            doc.select("br").after("\n");
            Elements pElements = doc.select("p, h1, h2, h3, h4, h5, h6");

            if (pElements.isEmpty()) {
                String plainText = doc.text();
                String[] lines = plainText.split("\\r?\\n+");
                int paraIdx = 0;
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) continue;

                    List<Sentence> sentences = splitSentencesHelper(trimmed);
                    paragraphs.add(new Paragraph("chap_" + currentChapterIndex + "_para_" + paraIdx, paraIdx, trimmed, sentences));
                    paraIdx++;
                }
            } else {
                int paraIdx = 0;
                for (Element pEl : pElements) {
                    if (!pEl.select("p").isEmpty()) continue;
                    String text = pEl.text().trim();
                    if (text.isEmpty()) continue;

                    List<Sentence> sentences = splitSentencesHelper(text);
                    paragraphs.add(new Paragraph("chap_" + currentChapterIndex + "_para_" + paraIdx, paraIdx, text, sentences));
                    paraIdx++;
                }
            }
        }

        if (paragraphs.isEmpty()) {
            List<Sentence> sentences = new ArrayList<>();
            sentences.add(new Sentence(0, "Khong co noi dung chuong", 0, 20));
            paragraphs.add(new Paragraph("chap_0_para_0", 0, "Khong co noi dung chuong", sentences));
        }

        chap.setParagraphs(paragraphs);
        List<Chapter> chapters = new ArrayList<>();
        chapters.add(chap);
        content.setChapters(chapters);

        return content;
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
    }
}
