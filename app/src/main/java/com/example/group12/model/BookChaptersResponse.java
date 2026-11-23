
package com.example.group12.model;

import java.util.List;

public class BookChaptersResponse {

    private boolean success;
    private String message;
    private Data data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Data getData() {
        return data;
    }

    public static class Data {

        private Book book;
        private int totalChapters;
        private List<Chapter> chapters;

        public Book getBook() {
            return book;
        }

        public int getTotalChapters() {
            return totalChapters;
        }

        public List<Chapter> getChapters() {
            return chapters;
        }
    }

    public static class Book {

        private int bookId;
        private String title;

        public int getBookId() {
            return bookId;
        }

        public String getTitle() {
            return title;
        }
    }
}
