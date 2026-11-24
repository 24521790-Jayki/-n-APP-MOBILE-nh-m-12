package com.example.group12.model;

import java.io.Serializable;

// Mo hinh du lieu cho mot cau trong doan van
public class Sentence implements Serializable {
    private int sentenceIndex;
    private String text;
    private int charStart;
    private int charEnd;

    public Sentence() {}

    public Sentence(int sentenceIndex, String text, int charStart, int charEnd) {
        this.sentenceIndex = sentenceIndex;
        this.text = text;
        this.charStart = charStart;
        this.charEnd = charEnd;
    }

    public int getSentenceIndex() { return sentenceIndex; }
    public void setSentenceIndex(int sentenceIndex) { this.sentenceIndex = sentenceIndex; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getCharStart() { return charStart; }
    public void setCharStart(int charStart) { this.charStart = charStart; }

    public int getCharEnd() { return charEnd; }
    public void setCharEnd(int charEnd) { this.charEnd = charEnd; }
}
