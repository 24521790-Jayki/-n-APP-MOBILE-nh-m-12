package com.example.group12.adapter;

public class CommunityBookItem {
    private final String title;
    private final String author;
    private final String contributor;
    private final String initial;

    public CommunityBookItem(String title, String author, String contributor, String initial) {
        this.title = title;
        this.author = author;
        this.contributor = contributor;
        this.initial = initial;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getContributor() { return contributor; }
    public String getInitial() { return initial; }
}
