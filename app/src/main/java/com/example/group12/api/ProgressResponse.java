package com.example.group12.api;

import com.example.group12.db.entity.ReadingProgress;

// Reponse chua tien do doc tu server
public class ProgressResponse {

    private boolean success;
    private String message;
    private ReadingProgress data;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public ReadingProgress getData() { return data; }
    public void setData(ReadingProgress data) { this.data = data; }
}
