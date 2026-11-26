package com.example.group12.service;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import com.example.group12.model.BookContent;
import com.example.group12.model.Chapter;
import com.example.group12.model.Paragraph;
import com.example.group12.model.Sentence;
import com.example.group12.util.TtsEngineHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Media3 Service boc Android TextToSpeech
// Quan ly phat giong noi chay nen voi Da luong (Multi-threading), dieu khien tu man hinh khoa/thong bao
public class TtsMediaSessionService extends MediaSessionService
        implements TextToSpeech.OnInitListener {

    private static final String TAG = "TtsMediaService";

    private static TtsMediaSessionService instance;

    private MediaSession mediaSession;
    private TextToSpeech tts;
    private volatile boolean ttsReady = false;

    // Running background operations for TTS in a single-thread executor
    private final ExecutorService ttsExecutor = Executors.newSingleThreadExecutor();

    // Du lieu sach dang doc
    private BookContent currentBook;
    private int currentChapterIdx = 0;
    private int currentParaIdx = 0;
    private int currentSentIdx = 0;
    private float currentSpeed = 1.0f;
    private volatile boolean isPlaying = false;

    // LiveData de thong bao UI vi tri hien tai dang doc
    private static final MutableLiveData<TtsPosition> currentPosition = new MutableLiveData<>();
    private static final MutableLiveData<Boolean> playingState = new MutableLiveData<>(false);

    // Lop luu tru vi tri hien tai cua TTS
    public static class TtsPosition {
        public final int chapterIndex;
        public final int paragraphIndex;
        public final int sentenceIndex;
        public final String utteranceId;

        public TtsPosition(int chapterIndex, int paragraphIndex,
                           int sentenceIndex, String utteranceId) {
            this.chapterIndex = chapterIndex;
            this.paragraphIndex = paragraphIndex;
            this.sentenceIndex = sentenceIndex;
            this.utteranceId = utteranceId;
        }
    }

    public static TtsMediaSessionService getInstance() {
        return instance;
    }

    // Cac phuong thuc static de UI lay du lieu
    public static LiveData<TtsPosition> getCurrentPositionLive() {
        return currentPosition;
    }

    public static LiveData<Boolean> getPlayingStateLive() {
        return playingState;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initTtsEngine();
    }

    private void initTtsEngine() {
        ttsExecutor.execute(() -> tts = new TextToSpeech(TtsMediaSessionService.this, TtsMediaSessionService.this));
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            ttsExecutor.execute(this::setupTtsInternal);
        } else {
            Log.e(TAG, "Khoi tao TextToSpeech that bai voi status: " + status);
            ttsReady = false;
        }
    }

    private void setupTtsInternal() {
        if (tts == null) return;

        // Dat ngon ngu tieng Viet voi fallback an toan
        TtsEngineHelper.setVietnameseLanguage(tts);
        tts.setSpeechRate(currentSpeed);

        // Lang nghe tien trinh doc
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                Log.d(TAG, "Bat dau doc: " + utteranceId);
                isPlaying = true;
                playingState.postValue(true);
                parseAndUpdatePosition(utteranceId);
            }

            @Override
            public void onDone(String utteranceId) {
                Log.d(TAG, "Doc xong: " + utteranceId);
                if (isPlaying) {
                    ttsExecutor.execute(() -> moveToNextSentenceInternal());
                }
            }

            @Override
            public void onError(String utteranceId) {
                Log.e(TAG, "Loi khi doc: " + utteranceId);
                isPlaying = false;
                playingState.postValue(false);
            }
        });

        ttsReady = true;
        Log.d(TAG, "TTS Engine da san sang va thiet lap hoan tat");

        // Neu da co lenh phat va book content -> Bat dau doc ngay tu vi tri hien tai
        if (isPlaying && currentBook != null) {
            speakCurrentSentenceInternal();
        }
    }

    // Dat sach can doc (Chi reset chi so neu la sach MOI)
    public void setBookContent(BookContent book) {
        if (book == null) return;
        if (this.currentBook == null || this.currentBook.getBookId() != book.getBookId()) {
            this.currentBook = book;
            this.currentChapterIdx = 0;
            this.currentParaIdx = 0;
            this.currentSentIdx = 0;
        } else {
            this.currentBook = book;
        }
    }

    // Bat dau doc tu vi tri hien tai
    public void startReading() {
        isPlaying = true;
        playingState.postValue(true);

        ttsExecutor.execute(() -> {
            if (tts == null || !ttsReady) {
                Log.w(TAG, "TTSEngine chua san sang, dang khoi tao lai...");
                initTtsEngine();
                return;
            }

            if (currentBook == null) {
                Log.w(TAG, "Chua co du lieu bookContent de doc");
                return;
            }

            speakCurrentSentenceInternal();
        });
    }

    // Tam dung
    public void pauseReading() {
        isPlaying = false;
        playingState.postValue(false);
        ttsExecutor.execute(() -> {
            if (tts != null) {
                tts.stop();
            }
        });
    }

    // Tiep tuc doc
    public void resumeReading() {
        startReading();
    }

    // Nhay den vi tri cu the
    public void seekTo(int chapterIdx, int paraIdx, int sentIdx) {
        this.currentChapterIdx = chapterIdx;
        this.currentParaIdx = paraIdx;
        this.currentSentIdx = sentIdx;

        ttsExecutor.execute(() -> {
            if (tts != null) {
                tts.stop();
            }
            if (isPlaying) {
                speakCurrentSentenceInternal();
            }
        });
    }

    // Dat toc do doc (Multi-thread safe)
    public void setSpeed(float speed) {
        this.currentSpeed = speed;
        ttsExecutor.execute(() -> {
            if (tts != null && ttsReady) {
                tts.setSpeechRate(currentSpeed);
                if (isPlaying) {
                    tts.stop();
                    speakCurrentSentenceInternal();
                }
            }
        });
    }

    // Dat giong doc (Fix loi reset tien trinh doc)
    public void setVoice(Voice voice) {
        if (voice == null) return;
        ttsExecutor.execute(() -> {
            if (tts != null && ttsReady) {
                int res = tts.setVoice(voice);
                Log.d(TAG, "Cap nhat giong doc TTS: " + voice.getName() + " (code " + res + ")");
                if (isPlaying) {
                    tts.stop();
                    speakCurrentSentenceInternal();
                }
            }
        });
    }

    // Thay doi Engine TTS
    public void setEngine(String enginePackage) {
        if (enginePackage == null || enginePackage.isEmpty()) return;

        final int savedChap = currentChapterIdx;
        final int savedPara = currentParaIdx;
        final int savedSent = currentSentIdx;
        final boolean savedPlaying = isPlaying;

        ttsExecutor.execute(() -> {
            if (tts != null) {
                tts.stop();
                tts.shutdown();
            }

            ttsReady = false;
            currentChapterIdx = savedChap;
            currentParaIdx = savedPara;
            currentSentIdx = savedSent;
            isPlaying = savedPlaying;

            tts = new TextToSpeech(TtsMediaSessionService.this, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    ttsExecutor.execute(() -> {
                        setupTtsInternal();
                        if (savedPlaying) {
                            speakCurrentSentenceInternal();
                        }
                    });
                } else {
                    Log.e(TAG, "Loi khoi tao engine moi: " + enginePackage);
                }
            }, enginePackage);
        });
    }

    // Lay instance TextToSpeech
    public TextToSpeech getTtsInstance() {
        return tts;
    }

    // Doc cau hien tai (Chay tren ttsExecutor thread)
    private void speakCurrentSentenceInternal() {
        if (currentBook == null || tts == null || !ttsReady) return;

        List<Chapter> chapters = currentBook.getChapters();
        if (chapters == null || chapters.isEmpty() || currentChapterIdx >= chapters.size()) {
            isPlaying = false;
            playingState.postValue(false);
            return;
        }

        Chapter chapter = chapters.get(currentChapterIdx);
        List<Paragraph> paragraphs = chapter.getParagraphs();
        if (paragraphs == null || paragraphs.isEmpty() || currentParaIdx >= paragraphs.size()) {
            currentChapterIdx++;
            currentParaIdx = 0;
            currentSentIdx = 0;
            speakCurrentSentenceInternal();
            return;
        }

        Paragraph para = paragraphs.get(currentParaIdx);
        List<Sentence> sentences = para.getSentences();

        if (sentences == null || sentences.isEmpty()) {
            sentences = new ArrayList<>();
            sentences.add(new Sentence(0, para.getFullText(), 0, para.getFullText().length()));
            para.setSentences(sentences);
        }

        if (currentSentIdx >= sentences.size()) {
            currentParaIdx++;
            currentSentIdx = 0;
            speakCurrentSentenceInternal();
            return;
        }

        Sentence sentence = sentences.get(currentSentIdx);
        String utteranceId = "chap_" + currentChapterIdx
                + "_para_" + currentParaIdx
                + "_sent_" + currentSentIdx;

        tts.setSpeechRate(currentSpeed);

        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);

        // Phat cau hien tai voi QUEUE_FLUSH
        int result = tts.speak(sentence.getText(), TextToSpeech.QUEUE_FLUSH, params, utteranceId);
        if (result == TextToSpeech.ERROR) {
            Log.e(TAG, "Loi tts.speak voi cau: " + sentence.getText());
        } else {
            // Pre-queue cau ke tiếp neu co
            preQueueNextSentence(sentences);
        }
    }

    // Hang doi truoc (Pre-queue) cau ke tiep de phat mượt va khong bi khuc khac
    private void preQueueNextSentence(List<Sentence> currentSentences) {
        if (tts == null || !ttsReady) return;

        int nextSent = currentSentIdx + 1;
        int nextPara = currentParaIdx;
        int nextChap = currentChapterIdx;

        List<Sentence> targetSentences = currentSentences;

        if (nextSent >= targetSentences.size()) {
            nextPara++;
            nextSent = 0;
            List<Chapter> chapters = currentBook.getChapters();
            if (chapters != null && nextChap < chapters.size()) {
                List<Paragraph> paras = chapters.get(nextChap).getParagraphs();
                if (paras != null && nextPara < paras.size()) {
                    targetSentences = paras.get(nextPara).getSentences();
                } else {
                    nextChap++;
                    nextPara = 0;
                    if (chapters != null && nextChap < chapters.size()) {
                        List<Paragraph> nextChapParas = chapters.get(nextChap).getParagraphs();
                        if (nextChapParas != null && !nextChapParas.isEmpty()) {
                            targetSentences = nextChapParas.get(0).getSentences();
                        }
                    }
                }
            }
        }

        if (targetSentences != null && nextSent < targetSentences.size()) {
            Sentence nextSentence = targetSentences.get(nextSent);
            String nextUtteranceId = "chap_" + nextChap + "_para_" + nextPara + "_sent_" + nextSent;
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, nextUtteranceId);

            tts.speak(nextSentence.getText(), TextToSpeech.QUEUE_ADD, params, nextUtteranceId);
        }
    }

    // Chuyen sang cau tiep theo khi cau hien tai doc xong
    private void moveToNextSentenceInternal() {
        if (!isPlaying || currentBook == null) return;

        currentSentIdx++;
        speakCurrentSentenceInternal();
    }

    // Phan tich utteranceId de cap nhat vi tri
    private void parseAndUpdatePosition(String utteranceId) {
        try {
            String[] parts = utteranceId.split("_");
            if (parts.length >= 6) {
                int chapIdx = Integer.parseInt(parts[1]);
                int paraIdx = Integer.parseInt(parts[3]);
                int sentIdx = Integer.parseInt(parts[5]);
                this.currentChapterIdx = chapIdx;
                this.currentParaIdx = paraIdx;
                this.currentSentIdx = sentIdx;
                currentPosition.postValue(
                        new TtsPosition(chapIdx, paraIdx, sentIdx, utteranceId));
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "Khong the parse utteranceId: " + utteranceId);
        }
    }

    // Lay vi tri hien tai
    public int getCurrentChapterIdx() { return currentChapterIdx; }
    public int getCurrentParaIdx() { return currentParaIdx; }
    public int getCurrentSentIdx() { return currentSentIdx; }
    public float getCurrentSpeed() { return currentSpeed; }
    public boolean isCurrentlyPlaying() { return isPlaying; }

    @Nullable
    @Override
    public MediaSession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    @Override
    public void onDestroy() {
        ttsExecutor.execute(() -> {
            if (tts != null) {
                tts.stop();
                tts.shutdown();
            }
        });
        ttsExecutor.shutdown();
        if (mediaSession != null) {
            mediaSession.release();
        }
        instance = null;
        super.onDestroy();
    }
}
