package com.example.group12.adapter;

public class AuthorSearchItem {
    private final String authorName;
    private final String bookCount;
    private final String initial;

    public AuthorSearchItem(String authorName, String bookCount, String initial) {
        this.authorName = authorName;
        this.bookCount = bookCount;
        this.initial = initial;
    }

    public String getAuthorName() { return authorName; }
    public String getBookCount() { return bookCount; }
    public String getInitial() { return initial; }
}
