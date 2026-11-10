
package com.example.group12.api;

public class LoginRequest {

    private String Email;
    private String Password;

    public LoginRequest(String email, String password) {
        this.Email = email;
        this.Password = password;
    }

    public String getEmail() {
        return Email;
    }

    public String getPassword() {
        return Password;
    }
}

