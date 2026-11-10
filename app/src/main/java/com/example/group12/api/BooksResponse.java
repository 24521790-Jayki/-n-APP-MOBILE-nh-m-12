
package com.example.group12.api;

import java.util.List;

public class BooksResponse {

    private boolean success;
    private List<Book> data;

    public boolean isSuccess() {
        return success;
    }

    public List<Book> getData() {
        return data;
    }
}
