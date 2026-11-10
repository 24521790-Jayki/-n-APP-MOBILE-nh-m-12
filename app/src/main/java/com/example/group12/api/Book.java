
package com.example.group12.api;

public class Book {

    private int BookID;
    private String Title;
    private String Author;
    private String Description;
    private int CategoryID;
    private String CategoryName;
    private String Language;
    private String EpubStoragePath;
    private String EpubOriginalName;
    private Long EpubSizeBytes;
    private String CoverImagePath;
    private String CreatedAt;
    private String UpdatedAt;

    public int getBookID() {
        return BookID;
    }

    public String getTitle() {
        return Title;
    }

    public String getAuthor() {
        return Author;
    }

    public String getDescription() {
        return Description;
    }

    public int getCategoryID() {
        return CategoryID;
    }

    public String getCategoryName() {
        return CategoryName;
    }

    public String getLanguage() {
        return Language;
    }

    public String getEpubStoragePath() {
        return EpubStoragePath;
    }

    public String getEpubOriginalName() {
        return EpubOriginalName;
    }

    public Long getEpubSizeBytes() {
        return EpubSizeBytes;
    }

    public String getCoverImagePath() {
        return CoverImagePath;
    }

    public String getCreatedAt() {
        return CreatedAt;
    }

    public String getUpdatedAt() {
        return UpdatedAt;
    }
}
