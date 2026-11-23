
package com.example.group12.api;

public class CreateBookRequest {

    private String Title;
    private String Author;
    private String Description;
    private Integer CategoryID;
    private String Language;

    public CreateBookRequest(
            String title,
            String author,
            String description,
            Integer categoryID,
            String language
    ) {
        this.Title = title;
        this.Author = author;
        this.Description = description;
        this.CategoryID = categoryID;
        this.Language = language;
    }
}
