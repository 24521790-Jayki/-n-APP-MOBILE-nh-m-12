package com.example.group12.api;

public class Category {

    private int CategoryID;
    private String CategoryName;
    private String Description;
    private String CreatedAt;

    public int getCategoryID() {
        return CategoryID;
    }

    public String getCategoryName() {
        return CategoryName;
    }

    public String getDescription() {
        return Description;
    }

    public String getCreatedAt() {
        return CreatedAt;
    }

    @Override
    public String toString() {
        return CategoryName;
    }
}
