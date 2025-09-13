package com.example.bulksmsAPI.Models.DTO;

public class AuthResponse {
    private String token;
    private String username;
    private String userId;
    private String Role;
    private String message;
    private String secretKey; // New field for the secret key
// Nuevo campo para el mensaje


    // Default constructor (needed by Jackson for deserialization)
    public AuthResponse() {}

    public AuthResponse(String token, String username, String userId, String Role, String secretKey) {
        this.token = token;
        this.username = username;
        this.userId = userId;

        this.Role = Role;
        this.secretKey = secretKey; // Initialize the secret key

    }
    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
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
    public String getRole() {
        return Role;
    }
    public void setRole(String role) {
        Role = role;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
