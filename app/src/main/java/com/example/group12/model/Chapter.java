package com.example.group12.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Mo hinh du lieu cho mot chuong sach
public class Chapter implements Serializable {
    private int chapterIndex;
    private String title;
    private List<Paragraph> paragraphs;

    public Chapter() {
        this.paragraphs = new ArrayList<>();
    }

    public Chapter(int chapterIndex, String title, List<Paragraph> paragraphs) {
        this.chapterIndex = chapterIndex;
        this.title = title;
        this.paragraphs = paragraphs != null ? paragraphs : new ArrayList<>();
    }

    public int getChapterIndex() { return chapterIndex; }
    public void setChapterIndex(int chapterIndex) { this.chapterIndex = chapterIndex; }

    public int getChapterNumber() { return chapterIndex + 1; }
    public void setChapterNumber(int chapterNumber) { this.chapterIndex = chapterNumber - 1; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<Paragraph> getParagraphs() { return paragraphs; }
    public void setParagraphs(List<Paragraph> paragraphs) { this.paragraphs = paragraphs; }
}
