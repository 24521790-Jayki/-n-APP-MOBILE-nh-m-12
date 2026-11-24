package com.example.group12.adapter;

public class SearchResultItem {
    private int bookId = -1;
    private final String title;
    private final String author;
    private final String tag;
    private final String rating;
    private final String reviews;
    private final String initial;

    public SearchResultItem(String title, String author, String tag, String rating, String reviews, String initial) {
        this(-1, title, author, tag, rating, reviews, initial);
    }

    public SearchResultItem(int bookId, String title, String author, String tag, String rating, String reviews, String initial) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.tag = tag;
        this.rating = rating;
        this.reviews = reviews;
        this.initial = initial;
    }

    public int getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getTag() { return tag; }
    public String getRating() { return rating; }
    public String getReviews() { return reviews; }
    public String getInitial() { return initial; }
}
