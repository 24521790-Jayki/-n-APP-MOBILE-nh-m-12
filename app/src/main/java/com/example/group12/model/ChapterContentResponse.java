
package com.example.group12.model;

public class ChapterContentResponse {

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

        private BookChaptersResponse.Book book;
        private int chapterNumber;
        private String title;
        private String content;
        private int contentLength;

        public BookChaptersResponse.Book getBook() {
            return book;
        }

        public int getChapterNumber() {
            return chapterNumber;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public int getContentLength() {
            return contentLength;
        }
    }
}
