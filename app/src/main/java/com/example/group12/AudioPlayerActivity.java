package com.example.group12;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.Voice;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group12.api.ApiClient;
import com.example.group12.databinding.ActivityAudioPlayerBinding;
import com.example.group12.model.BookChaptersResponse;
import com.example.group12.model.BookContent;
import com.example.group12.model.Chapter;
import com.example.group12.model.ChapterContentResponse;
import com.example.group12.model.Paragraph;
import com.example.group12.model.Sentence;
import com.example.group12.repository.BookRepository;
import com.example.group12.service.TtsMediaSessionService;
import com.example.group12.ui.tts.TtsSettingsBottomSheet;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AudioPlayerActivity extends AppCompatActivity
        implements TtsSettingsBottomSheet.TtsSettingsListener {

    private static final String TAG = "AudioPlayerActivity";

    private ActivityAudioPlayerBinding binding;
    private BookRepository repository;

    private int currentBookId = 1;
    private int currentChapterIndex = 0;
    private int totalChapters = 0;
    private BookContent bookContent;
    private boolean isFavorite = false;

    // Sleep Timer
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAudioPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new BookRepository(this);

        // Khoi tao dich vu TtsMediaSessionService chay nen
        Intent serviceIntent = new Intent(this, TtsMediaSessionService.class);
        startService(serviceIntent);

        // Nhan du lieu tu Intent
        currentBookId = getIntent().getIntExtra("bookId", getIntent().getIntExtra("book_id", 1));
        int targetChapter = getIntent().getIntExtra("chapter_number", 1);
        currentChapterIndex = Math.max(0, targetChapter - 1);

        String incomingTitle = getIntent().getStringExtra("book_title");
        String incomingAuthor = getIntent().getStringExtra("book_author");

        if (incomingTitle != null && !incomingTitle.isEmpty()) {
            binding.tvAudioBookTitle.setText(incomingTitle);
        }
        if (incomingAuthor != null && !incomingAuthor.isEmpty()) {
            binding.tvAudioBookAuthor.setText(incomingAuthor);
        }

        // Tai du lieu sach tu Local/EPUB hoac Server API
        loadBookData();

        // Lang nghe su kien nut bam
        setupListeners();

        // Quan sat LiveData tu TTS Service
        setupTtsObservers();
    }

    private void loadBookData() {
        if (repository.isEpubDownloaded(currentBookId)) {
            bookContent = repository.parseEpub(currentBookId);
            if (bookContent != null && bookContent.getChapters() != null && !bookContent.getChapters().isEmpty()) {
                totalChapters = bookContent.getTotalChapters();
                binding.tvAudioBookTitle.setText(bookContent.getTitle());
                binding.tvAudioBookAuthor.setText(bookContent.getAuthor());

                TtsMediaSessionService ttsService = TtsMediaSessionService.getInstance();
                if (ttsService != null) {
                    ttsService.setBookContent(bookContent);
                }

                updateChapterUi(currentChapterIndex);
            } else {
                fetchChaptersFromApi();
            }
        } else {
            fetchChaptersFromApi();
        }
    }

    private void fetchChaptersFromApi() {
        ApiClient.getApiService().getBookChapters(currentBookId).enqueue(new Callback<BookChaptersResponse>() {
            @Override
            public void onResponse(Call<BookChaptersResponse> call, Response<BookChaptersResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    BookChaptersResponse.Data data = response.body().getData();
                    if (data != null && data.getChapters() != null) {
                        totalChapters = data.getTotalChapters();

                        if (data.getBook() != null) {
                            binding.tvAudioBookTitle.setText(data.getBook().getTitle());
                        }

                        // Khoi tao bookContent bao gom tat ca cac chuong tu API
                        initBookContentFromApiList(data);

                        updateChapterUi(currentChapterIndex);
                        loadAndPrepareAudioFromApi(currentChapterIndex + 1);
                    }
                }
            }

            @Override
            public void onFailure(Call<BookChaptersResponse> call, Throwable t) {
                Toast.makeText(AudioPlayerActivity.this, "Loi tai danh sach chuong: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initBookContentFromApiList(BookChaptersResponse.Data data) {
        if (bookContent == null) {
            bookContent = new BookContent();
            bookContent.setBookId(currentBookId);
            if (data.getBook() != null) {
                bookContent.setTitle(data.getBook().getTitle());
            }
        }

        List<Chapter> chapters = new ArrayList<>();
        if (data.getChapters() != null) {
            for (int i = 0; i < data.getChapters().size(); i++) {
                Chapter chap = new Chapter();
                chap.setChapterIndex(i);
                chap.setTitle("Chuong " + (i + 1));
                chapters.add(chap);
            }
        }
        bookContent.setChapters(chapters);
    }

    private void loadAndPrepareAudioFromApi(int chapterNumber) {
        ApiClient.getApiService().getChapterContent(currentBookId, chapterNumber).enqueue(new Callback<ChapterContentResponse>() {
            @Override
            public void onResponse(Call<ChapterContentResponse> call, Response<ChapterContentResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    ChapterContentResponse.Data data = response.body().getData();
                    updateBookContentWithChapterData(data);

                    TtsMediaSessionService ttsService = TtsMediaSessionService.getInstance();
                    if (ttsService != null && bookContent != null) {
                        ttsService.setBookContent(bookContent);
                        ttsService.seekTo(currentChapterIndex, 0, 0);
                        ttsService.startReading();
                    }
                }
            }

            @Override
            public void onFailure(Call<ChapterContentResponse> call, Throwable t) {
                Log.e(TAG, "Loi tai noi dung audio chuong tu API: " + t.getMessage());
            }
        });
    }

    private void updateBookContentWithChapterData(ChapterContentResponse.Data data) {
        if (bookContent == null) {
            bookContent = new BookContent();
            bookContent.setBookId(currentBookId);
            bookContent.setTitle(binding.tvAudioBookTitle.getText().toString());
        }

        List<Chapter> chapters = bookContent.getChapters();
        if (chapters == null || chapters.isEmpty()) {
            chapters = new ArrayList<>();
            bookContent.setChapters(chapters);
        }

        int targetIdx = Math.max(0, data.getChapterNumber() - 1);

        while (chapters.size() <= targetIdx) {
            Chapter newChap = new Chapter();
            newChap.setChapterIndex(chapters.size());
            newChap.setTitle("Chuong " + (chapters.size() + 1));
            chapters.add(newChap);
        }

        Chapter targetChap = chapters.get(targetIdx);
        targetChap.setTitle(data.getTitle() != null && !data.getTitle().isEmpty() ? data.getTitle() : "Chuong " + data.getChapterNumber());

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

                    List<Sentence> sentences = new ArrayList<>();
                    String[] rawSentences = trimmed.split("(?<=[.!?…])\\s+");
                    int sentIdx = 0;
                    int charStart = 0;
                    for (String raw : rawSentences) {
                        if (raw.trim().isEmpty()) continue;
                        int charEnd = charStart + raw.length();
                        sentences.add(new Sentence(sentIdx++, raw.trim(), charStart, charEnd));
                        charStart = charEnd + 1;
                    }

                    paragraphs.add(new Paragraph("chap_" + targetIdx + "_para_" + paraIdx, paraIdx, trimmed, sentences));
                    paraIdx++;
                }
            } else {
                int paraIdx = 0;
                for (Element pEl : pElements) {
                    if (!pEl.select("p").isEmpty()) continue;
                    String text = pEl.text().trim();
                    if (text.isEmpty()) continue;

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

                    Paragraph para = new Paragraph("chap_" + targetIdx + "_para_" + paraIdx, paraIdx, text, sentences);
                    paragraphs.add(para);
                    paraIdx++;
                }
            }
        }

        if (paragraphs.isEmpty()) {
            List<Sentence> sentences = new ArrayList<>();
            sentences.add(new Sentence(0, "Khong co noi dung chuong", 0, 20));
            paragraphs.add(new Paragraph("chap_" + targetIdx + "_para_0", 0, "Khong co noi dung chuong", sentences));
        }

        targetChap.setParagraphs(paragraphs);
    }

    private void setupListeners() {
        // 1. Nut Quay lai
        binding.btnBack.setOnClickListener(v -> finish());

        // 2. Nut Yeu thich
        binding.btnFavorite.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            if (isFavorite) {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                binding.btnFavorite.setImageTintList(ColorStateList.valueOf(Color.parseColor("#EF4444")));
                Toast.makeText(this, "Da them vao danh sach yeu thich!", Toast.LENGTH_SHORT).show();
            } else {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                binding.btnFavorite.setImageTintList(ColorStateList.valueOf(Color.parseColor("#7C3AED")));
                Toast.makeText(this, "Da xoa khoi danh sach yeu thich!", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. Nut Doi giong doc / Engine TTS
        binding.btnVoiceSettings.setOnClickListener(v -> openTtsSettingsBottomSheet());

        // 4. Nut Play/Pause
        binding.btnPlayPause.setOnClickListener(v -> togglePlayPause());

        // 5. Nut Rewind 15s / Para truoc
        binding.btnRewind15.setOnClickListener(v -> seekRelative(-1));

        // 6. Nut Forward 15s / Para sau
        binding.btnForward15.setOnClickListener(v -> seekRelative(1));

        // 7. Nut Chuong truoc (Previous Chapter)
        binding.btnPrevious.setOnClickListener(v -> changeChapter(-1));

        // 8. Nut Chuong tiep theo (Next Chapter)
        binding.btnNext.setOnClickListener(v -> changeChapter(1));

        // 9. Toc do 1.0x & 1.25x
        binding.btnSpeed10.setOnClickListener(v -> setSpeed(1.0f));
        binding.btnSpeed125.setOnClickListener(v -> setSpeed(1.25f));

        // 10. Nut Hen gio tat (Timer)
        binding.btnTimer.setOnClickListener(v -> showTimerMenu());

        // 11. Nut Danh sach chuong (Playlist)
        binding.btnPlaylist.setOnClickListener(v -> showChapterDialog());

        // 12. Nut Chuyen sang doc sach chu
        binding.btnSwitchToText.setOnClickListener(v -> {
            Intent intent = new Intent(AudioPlayerActivity.this, ReaderActivity.class);
            intent.putExtra("bookId", currentBookId);
            intent.putExtra("chapter_number", currentChapterIndex + 1);
            startActivity(intent);
            finish();
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

    private void setupTtsObservers() {
        TtsMediaSessionService.getCurrentPositionLive().observe(this, pos -> {
            if (pos != null && pos.chapterIndex == currentChapterIndex) {
                int totalParas = 1;
                if (bookContent != null && bookContent.getChapters() != null && currentChapterIndex < bookContent.getChapters().size()) {
                    List<Paragraph> paras = bookContent.getChapters().get(currentChapterIndex).getParagraphs();
                    if (paras != null && !paras.isEmpty()) {
                        totalParas = paras.size();
                    }
                }
                binding.tvAudioStatus.setText("🔊 Dang phat Doan " + (pos.paragraphIndex + 1) + " / " + totalParas);
            }
        });

        TtsMediaSessionService.getPlayingStateLive().observe(this, isPlaying -> {
            if (isPlaying) {
                binding.btnPlayPause.setImageResource(R.drawable.ic_pause);
            } else {
                binding.btnPlayPause.setImageResource(R.drawable.ic_play);
                binding.tvAudioStatus.setText("⏸ Tam dung phat giong doc AI");
            }
        });
    }

    private void togglePlayPause() {
        TtsMediaSessionService ttsService = TtsMediaSessionService.getInstance();
        if (ttsService == null) {
            Intent intent = new Intent(this, TtsMediaSessionService.class);
            startService(intent);
            ttsService = TtsMediaSessionService.getInstance();
        }

        if (ttsService != null) {
            if (ttsService.isCurrentlyPlaying()) {
                ttsService.pauseReading();
                binding.btnPlayPause.setImageResource(R.drawable.ic_play);
                binding.tvAudioStatus.setText("⏸ Tam dung phat giong doc AI");
                Toast.makeText(this, "Da tam dung phat audio", Toast.LENGTH_SHORT).show();
            } else {
                if (bookContent != null) {
                    ttsService.setBookContent(bookContent);
                    ttsService.seekTo(currentChapterIndex, 0, 0);
                    ttsService.startReading();
                    binding.btnPlayPause.setImageResource(R.drawable.ic_pause);
                } else {
                    Toast.makeText(this, "Dang tai noi dung chuong audio...", Toast.LENGTH_SHORT).show();
                    loadAndPrepareAudioFromApi(currentChapterIndex + 1);
                }
            }
        } else {
            Toast.makeText(this, "Dang khoi tao Media Service...", Toast.LENGTH_SHORT).show();
        }
    }

    private void seekRelative(int deltaPara) {
        TtsMediaSessionService ttsService = TtsMediaSessionService.getInstance();
        if (ttsService == null) {
            Intent intent = new Intent(this, TtsMediaSessionService.class);
            startService(intent);
            ttsService = TtsMediaSessionService.getInstance();
        }

        if (ttsService != null && bookContent != null && bookContent.getChapters() != null && currentChapterIndex < bookContent.getChapters().size()) {
            List<Paragraph> paras = bookContent.getChapters().get(currentChapterIndex).getParagraphs();
            int totalParas = (paras != null && !paras.isEmpty()) ? paras.size() : 1;
            int currentPara = ttsService.getCurrentParaIdx();
            int targetPara = Math.max(0, Math.min(totalParas - 1, currentPara + deltaPara));

            ttsService.seekTo(currentChapterIndex, targetPara, 0);
            ttsService.startReading();
            binding.tvAudioStatus.setText("🔊 Dang phat Doan " + (targetPara + 1) + " / " + totalParas);
        } else {
            Toast.makeText(this, "Dang tai du lieu audio...", Toast.LENGTH_SHORT).show();
            loadAndPrepareAudioFromApi(currentChapterIndex + 1);
        }
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
        updateChapterUi(currentChapterIndex);

        loadAndPrepareAudioFromApi(currentChapterIndex + 1);
    }

    private void setSpeed(float speed) {
        TtsMediaSessionService ttsService = TtsMediaSessionService.getInstance();
        if (ttsService != null) {
            ttsService.setSpeed(speed);
        }

        if (speed == 1.0f) {
            binding.btnSpeed10.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#7C3AED")));
            binding.btnSpeed10.setTextColor(Color.WHITE);
            binding.btnSpeed125.setBackgroundTintList(null);
            binding.btnSpeed125.setTextColor(Color.parseColor("#7C3AED"));
        } else {
            binding.btnSpeed125.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#7C3AED")));
            binding.btnSpeed125.setTextColor(Color.WHITE);
            binding.btnSpeed10.setBackgroundTintList(null);
            binding.btnSpeed10.setTextColor(Color.parseColor("#7C3AED"));
        }

        Toast.makeText(this, "Da dat toc do " + speed + "x", Toast.LENGTH_SHORT).show();
    }

    private void showTimerMenu() {
        String[] options = {"Tat hen gio", "15 phut", "30 phut", "45 phut", "60 phut"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hen gio tat")
                .setItems(options, (dialog, which) -> {
                    if (timerRunnable != null) {
                        timerHandler.removeCallbacks(timerRunnable);
                    }

                    switch (which) {
                        case 0:
                            Toast.makeText(this, "Da tat hen gio", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            setSleepTimer(15);
                            break;
                        case 2:
                            setSleepTimer(30);
                            break;
                        case 3:
                            setSleepTimer(45);
                            break;
                        case 4:
                            setSleepTimer(60);
                            break;
                    }
                }).show();
    }

    private void setSleepTimer(int minutes) {
        long millis = minutes * 60 * 1000L;

        timerRunnable = () -> {
            TtsMediaSessionService ttsService = TtsMediaSessionService.getInstance();
            if (ttsService != null) {
                ttsService.pauseReading();
            }
            Toast.makeText(AudioPlayerActivity.this, "Da den gio tat audio tu dong", Toast.LENGTH_LONG).show();
        };

        timerHandler.postDelayed(timerRunnable, millis);
        Toast.makeText(this, "Da hen gio tat sau " + minutes + " phut", Toast.LENGTH_SHORT).show();
    }

    private void updateChapterUi(int chapterIdx) {
        String chapterTitleStr;

        if (bookContent != null && bookContent.getChapters() != null && chapterIdx < bookContent.getChapters().size()) {
            Chapter chap = bookContent.getChapters().get(chapterIdx);
            chapterTitleStr = "Chuong " + (chapterIdx + 1) + " - " + chap.getTitle();
        } else {
            chapterTitleStr = "Chuong " + (chapterIdx + 1);
        }

        binding.tvChapterTitle.setText(chapterTitleStr);
        binding.tvAudioStatus.setText("🔊 Dang phat " + chapterTitleStr);
    }

    private void showChapterDialog() {
        BottomSheetDialog sheetDialog = new BottomSheetDialog(AudioPlayerActivity.this);
        View view = LayoutInflater.from(AudioPlayerActivity.this).inflate(R.layout.dialog_chapters, null);
        sheetDialog.setContentView(view);
        sheetDialog.show();

        LinearLayout chapterContainer = view.findViewById(R.id.chapterContainer);
        TextView txtBookSubtitle = view.findViewById(R.id.txtBookSubtitle);

        if (bookContent != null && bookContent.getChapters() != null && !bookContent.getChapters().isEmpty()) {
            txtBookSubtitle.setText(bookContent.getTitle() + " • " + bookContent.getChapters().size() + " chuong");
            chapterContainer.removeAllViews();

            for (Chapter chapter : bookContent.getChapters()) {
                addChapterItem(chapterContainer, chapter, sheetDialog);
            }
        } else {
            txtBookSubtitle.setText("Dang tai danh sach chuong...");
            ApiClient.getApiService().getBookChapters(currentBookId).enqueue(new Callback<BookChaptersResponse>() {
                @Override
                public void onResponse(Call<BookChaptersResponse> call, Response<BookChaptersResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        BookChaptersResponse.Data data = response.body().getData();
                        if (data != null && data.getChapters() != null) {
                            txtBookSubtitle.setText(data.getBook() != null ? data.getBook().getTitle() + " • " + data.getTotalChapters() + " chuong" : "Chuong");
                            chapterContainer.removeAllViews();

                            initBookContentFromApiList(data);

                            for (Chapter chapter : bookContent.getChapters()) {
                                addChapterItem(chapterContainer, chapter, sheetDialog);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<BookChaptersResponse> call, Throwable t) {
                    txtBookSubtitle.setText("Loi tai danh sach chuong");
                }
            });
        }
    }

    private void addChapterItem(LinearLayout container, Chapter chapter, BottomSheetDialog sheetDialog) {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(0, dpToPx(12), 0, dpToPx(12));

        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        if (outValue.resourceId != 0) {
            row.setBackgroundResource(outValue.resourceId);
        }

        TextView titleView = new TextView(this);
        titleView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        titleView.setText("Chuong " + chapter.getChapterNumber() + ": " + chapter.getTitle());
        titleView.setTextColor((chapter.getChapterNumber() - 1) == currentChapterIndex ? 0xFF7C3AED : 0xFF1F2937);
        titleView.setTextSize(14);
        titleView.setMaxLines(3);

        row.addView(titleView);
        container.addView(row);

        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)));
        divider.setBackgroundColor(0xFFF3F4F6);
        container.addView(divider);

        row.setOnClickListener(v -> {
            currentChapterIndex = chapter.getChapterNumber() - 1;
            updateChapterUi(currentChapterIndex);

            loadAndPrepareAudioFromApi(currentChapterIndex + 1);

            sheetDialog.dismiss();
        });
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
}
