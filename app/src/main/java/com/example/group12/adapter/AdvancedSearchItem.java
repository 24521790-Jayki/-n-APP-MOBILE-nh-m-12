package com.example.group12.adapter;

public class AdvancedSearchItem {
    private final String title;
    private final String author;
    private final String category;
    private final String status;
    private final String rating;
    private final String language;
    private final String initial;

    public AdvancedSearchItem(String title, String author, String category, String status, String rating, String language, String initial) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.status = status;
        this.rating = rating;
        this.language = language;
        this.initial = initial;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public String getRating() { return rating; }
    public String getLanguage() { return language; }
    public String getInitial() { return initial; }
}
