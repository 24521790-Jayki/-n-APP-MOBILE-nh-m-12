package com.example.group12.adapter;

public class MyUploadedBookItem {
    private final String title;
    private final String author;
    private final String uploadDate;
    private final String status; // "Đã duyệt" hoặc "Đang chờ duyệt"
    private final String initial;

    public MyUploadedBookItem(String title, String author, String uploadDate, String status, String initial) {
        this.title = title;
        this.author = author;
        this.uploadDate = uploadDate;
        this.status = status;
        this.initial = initial;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getUploadDate() { return uploadDate; }
    public String getStatus() { return status; }
    public String getInitial() { return initial; }
}
