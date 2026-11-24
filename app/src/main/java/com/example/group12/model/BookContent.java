package com.example.group12.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Mo hinh du lieu tong hop cho noi dung sach epub
// Day la Single Source of Truth cho ca Reader UI va TTS Engine
public class BookContent implements Serializable {
    private int bookId;
    private String title;
    private String author;
    private List<Chapter> chapters;

    public BookContent() {
        this.chapters = new ArrayList<>();
    }

    public BookContent(int bookId, String title, String author, List<Chapter> chapters) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.chapters = chapters != null ? chapters : new ArrayList<>();
    }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public List<Chapter> getChapters() { return chapters; }
    public void setChapters(List<Chapter> chapters) { this.chapters = chapters; }

    // Lay tong so chuong
    public int getTotalChapters() {
        return chapters != null ? chapters.size() : 0;
    }
}
