package com.example.bulksmsAPI.Models.DTO;

import com.example.bulksmsAPI.Models.ValidationStatus;

public class AuthResponse {
    private String token;
    private String userId;
    private String Role;
    private ValidationStatus accountValidated;
    private String message;
    private String secretKey; // New field for the secret key
// Nuevo campo para el mensaje


    // Default constructor (needed by Jackson for deserialization)
    public AuthResponse() {}

    public AuthResponse(String token, String userId, String Role, ValidationStatus validationStatus, String secretKey) {
        this.token = token;
        this.userId = userId;
        this.accountValidated = validationStatus;
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

    public ValidationStatus getAccountValidated() {
        return accountValidated;
    }

    public void setAccountValidated(ValidationStatus accountValidated) {
        this.accountValidated = accountValidated;
    }
}
