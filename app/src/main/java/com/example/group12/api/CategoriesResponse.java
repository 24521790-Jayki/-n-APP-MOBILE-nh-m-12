package com.example.group12.api;

import java.util.List;

public class CategoriesResponse {

    private boolean success;
    private List<Category> data;

    public boolean isSuccess() {
        return success;
    }

    public List<Category> getData() {
        return data;
    }
}
