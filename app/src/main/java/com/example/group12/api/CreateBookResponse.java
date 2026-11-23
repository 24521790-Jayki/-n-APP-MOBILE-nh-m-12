
package com.example.group12.api;

public class CreateBookResponse {

    private boolean success;
    private String message;
    private Book data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Book getData() {
        return data;
    }
}
