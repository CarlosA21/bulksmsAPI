package com.example.bulksmsAPI.Models.DTO;

public class AuthResponse {
    private String token;

    public AuthResponse() {} // Important: No-args constructor for Jackson

    public AuthResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
