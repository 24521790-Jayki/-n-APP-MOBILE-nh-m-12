package com.example.group12.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Mo hinh du lieu cho mot doan van trong chuong sach
public class Paragraph implements Serializable {
    private String paragraphId; // VD: "chap_1_para_3"
    private int paragraphIndex;
    private String fullText;
    private List<Sentence> sentences;

    public Paragraph() {
        this.sentences = new ArrayList<>();
    }

    public Paragraph(String paragraphId, int paragraphIndex, String fullText, List<Sentence> sentences) {
        this.paragraphId = paragraphId;
        this.paragraphIndex = paragraphIndex;
        this.fullText = fullText;
        this.sentences = sentences != null ? sentences : new ArrayList<>();
    }

    public String getParagraphId() { return paragraphId; }
    public void setParagraphId(String paragraphId) { this.paragraphId = paragraphId; }

    public int getParagraphIndex() { return paragraphIndex; }
    public void setParagraphIndex(int paragraphIndex) { this.paragraphIndex = paragraphIndex; }

    public String getFullText() { return fullText; }
    public void setFullText(String fullText) { this.fullText = fullText; }

    public List<Sentence> getSentences() { return sentences; }
    public void setSentences(List<Sentence> sentences) { this.sentences = sentences; }
}
