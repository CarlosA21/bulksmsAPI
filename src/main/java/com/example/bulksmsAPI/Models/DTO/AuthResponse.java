package com.example.bulksmsAPI.Models.DTO;

public class AuthResponse {
    private String token;
    private String username;
    private String userId;

    // Default constructor (needed by Jackson for deserialization)
    public AuthResponse() {}

    public AuthResponse(String token, String username, String userId) {
        this.token = token;
        this.username = username;
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }
    public String setUsername(String username) {
        this.username = username;
        return username;
    }
}
