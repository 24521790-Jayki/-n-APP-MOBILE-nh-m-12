package com.example.group12.adapter;

public class SearchResultItem {
    private final String title;
    private final String author;
    private final String tag;
    private final String rating;
    private final String reviews;
    private final String initial;

    public SearchResultItem(String title, String author, String tag, String rating, String reviews, String initial) {
        this.title = title;
        this.author = author;
        this.tag = tag;
        this.rating = rating;
        this.reviews = reviews;
        this.initial = initial;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getTag() { return tag; }
    public String getRating() { return rating; }
    public String getReviews() { return reviews; }
    public String getInitial() { return initial; }
}
