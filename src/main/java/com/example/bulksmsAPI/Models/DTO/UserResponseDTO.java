package com.example.bulksmsAPI.Models.DTO;

import com.example.bulksmsAPI.Models.User;
import java.util.Date;

public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String roles;
    private String legalIdNumber;
    private Date dob;
    private String secretKey;

    // Constructor vacío
    public UserResponseDTO() {}

    // Constructor que convierte de User a DTO
    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.roles = user.getRoles();

        // Manejo seguro del legalIdNumber
        try {
            this.legalIdNumber = user.getLegalIdNumber();
        } catch (Exception e) {
            this.legalIdNumber = "ENCRYPTED_DATA_ERROR";
        }

        this.secretKey = user.getSecretKey();
    }

    public UserResponseDTO(Long id, String username, String email, String roles,
                           String legalIdNumber, Date dob, String secretKey) {
        this.id = id;
        this.username = username;
        this.legalIdNumber = legalIdNumber;
        this.email = email;
        this.roles = roles;
        this.dob = dob;
        this.secretKey = secretKey;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getDriverLicense() {
        return legalIdNumber;
    }

    public void setDriverLicense(String legalIdNumber) {
        this.legalIdNumber = legalIdNumber;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
