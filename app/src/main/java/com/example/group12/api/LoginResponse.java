
package com.example.group12.api;

public class LoginResponse {

    private boolean success;
    private String message;
    private String token;
    private User user;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public static class User {

        private int UserID;
        private String FullName;
        private String Email;
        private String Role;

        public int getUserID() {
            return UserID;
        }

        public String getFullName() {
            return FullName;
        }

        public String getEmail() {
            return Email;
        }

        public String getRole() {
            return Role;
        }
    }
}

