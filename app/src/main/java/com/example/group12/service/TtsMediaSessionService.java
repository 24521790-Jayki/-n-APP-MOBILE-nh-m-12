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

// Media3 Service boc Android TextToSpeech
// Quan ly phat giong noi chay nen, dieu khien tu man hinh khoa/thong bao
public class TtsMediaSessionService extends MediaSessionService
        implements TextToSpeech.OnInitListener {

    private static final String TAG = "TtsMediaService";

    private static TtsMediaSessionService instance;

    private MediaSession mediaSession;
    private TextToSpeech tts;
    private boolean ttsReady = false;

    // Du lieu sach dang doc
    private BookContent currentBook;
    private int currentChapterIdx = 0;
    private int currentParaIdx = 0;
    private int currentSentIdx = 0;
    private float currentSpeed = 1.0f;
    private boolean isPlaying = false;

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
        tts = new TextToSpeech(this, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            setupTts();
        } else {
            Log.e(TAG, "Khoi tao TextToSpeech that bai voi status: " + status);
            ttsReady = false;
        }
    }

    private void setupTts() {
        // Dat ngon ngu tieng Viet voi fallback an toan
        TtsEngineHelper.setVietnameseLanguage(tts);

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
                moveToNextSentence();
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

        // Neu da co lenh phat va book content -> Bat dau doc ngay
        if (isPlaying && currentBook != null) {
            speakCurrentSentence();
        }
    }

    // Dat sach can doc
    public void setBookContent(BookContent book) {
        if (book == null) return;
        this.currentBook = book;
        this.currentChapterIdx = 0;
        this.currentParaIdx = 0;
        this.currentSentIdx = 0;
    }

    // Bat dau doc tu vi tri hien tai
    public void startReading() {
        if (tts == null || !ttsReady) {
            Log.w(TAG, "TTSEngine chua san sang, dang khoi tao lai...");
            isPlaying = true;
            if (tts == null) {
                initTtsEngine();
            }
            return;
        }

        if (currentBook == null) {
            Log.w(TAG, "Chua co du lieu bookContent de doc");
            return;
        }

        isPlaying = true;
        playingState.postValue(true);
        speakCurrentSentence();
    }

    // Tam dung
    public void pauseReading() {
        if (tts != null) {
            tts.stop();
        }
        isPlaying = false;
        playingState.postValue(false);
    }

    // Tiep tuc doc
    public void resumeReading() {
        startReading();
    }

    // Nhay den vi tri cu the (khi nguoi dung cham vao doan van tren Reader UI)
    public void seekTo(int chapterIdx, int paraIdx, int sentIdx) {
        this.currentChapterIdx = chapterIdx;
        this.currentParaIdx = paraIdx;
        this.currentSentIdx = sentIdx;

        if (tts != null) {
            tts.stop();
        }

        if (isPlaying) {
            speakCurrentSentence();
        }
    }

    // Dat toc do doc
    public void setSpeed(float speed) {
        this.currentSpeed = speed;
        if (tts != null) {
            tts.setSpeechRate(speed);
        }
    }

    // Dat giong doc
    public void setVoice(Voice voice) {
        if (tts != null && voice != null) {
            tts.setVoice(voice);
        }
    }

    // Thay doi Engine TTS
    public void setEngine(String enginePackage) {
        if (enginePackage == null || enginePackage.isEmpty()) return;

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        ttsReady = false;
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                setupTts();
            } else {
                Log.e(TAG, "Loi khoi tao engine moi: " + enginePackage);
            }
        }, enginePackage);
    }

    // Lay instance TextToSpeech
    public TextToSpeech getTtsInstance() {
        return tts;
    }

    // Doc cau hien tai
    private void speakCurrentSentence() {
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
            speakCurrentSentence();
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
            speakCurrentSentence();
            return;
        }

        Sentence sentence = sentences.get(currentSentIdx);
        String utteranceId = "chap_" + currentChapterIdx
                + "_para_" + currentParaIdx
                + "_sent_" + currentSentIdx;

        tts.setSpeechRate(currentSpeed);

        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);

        int result = tts.speak(sentence.getText(), TextToSpeech.QUEUE_FLUSH, params, utteranceId);
        if (result == TextToSpeech.ERROR) {
            Log.e(TAG, "Loi tts.speak voi cau: " + sentence.getText());
        }
    }

    // Chuyen sang cau tiep theo
    private void moveToNextSentence() {
        if (!isPlaying || currentBook == null) return;

        currentSentIdx++;
        speakCurrentSentence();
    }

    // Phan tich utteranceId de cap nhat vi tri
    private void parseAndUpdatePosition(String utteranceId) {
        try {
            String[] parts = utteranceId.split("_");
            if (parts.length >= 6) {
                int chapIdx = Integer.parseInt(parts[1]);
                int paraIdx = Integer.parseInt(parts[3]);
                int sentIdx = Integer.parseInt(parts[5]);
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
    public boolean isCurrentlyPlaying() { return isPlaying; }

    @Nullable
    @Override
    public MediaSession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (mediaSession != null) {
            mediaSession.release();
        }
        instance = null;
        super.onDestroy();
    }
}
