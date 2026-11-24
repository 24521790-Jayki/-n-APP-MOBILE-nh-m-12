package com.example.group12.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Entity luu tien do doc sach - dung chung cho ca doc mat va nghe TTS
@Entity(tableName = "reading_progress")
public class ReadingProgress {

    @PrimaryKey
    private int bookId;

    private int chapterIndex;
    private int paragraphIndex;
    private int sentenceIndex;
    private float ttsSpeed;
    private float scrollOffset;
    private long updatedAt;

    public ReadingProgress() {
        this.ttsSpeed = 1.0f;
        this.scrollOffset = 0f;
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters
    public int getBookId() { return bookId; }
    public int getChapterIndex() { return chapterIndex; }
    public int getParagraphIndex() { return paragraphIndex; }
    public int getSentenceIndex() { return sentenceIndex; }
    public float getTtsSpeed() { return ttsSpeed; }
    public float getScrollOffset() { return scrollOffset; }
    public long getUpdatedAt() { return updatedAt; }

    // Setters
    public void setBookId(int bookId) { this.bookId = bookId; }
    public void setChapterIndex(int chapterIndex) { this.chapterIndex = chapterIndex; }
    public void setParagraphIndex(int paragraphIndex) { this.paragraphIndex = paragraphIndex; }
    public void setSentenceIndex(int sentenceIndex) { this.sentenceIndex = sentenceIndex; }
    public void setTtsSpeed(float ttsSpeed) { this.ttsSpeed = ttsSpeed; }
    public void setScrollOffset(float scrollOffset) { this.scrollOffset = scrollOffset; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
