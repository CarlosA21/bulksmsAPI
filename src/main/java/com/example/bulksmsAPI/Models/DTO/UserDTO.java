package com.example.bulksmsAPI.Models.DTO;


import com.example.bulksmsAPI.Models.Contacts;

import java.util.List;

public class UserDTO {

    private String email;
    private String password;
    private List<Contacts> contacts;

    private int Credits;
    private String roles;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Contacts> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contacts> contacts) {
        this.contacts = contacts;
    }

    public int getCredits() {
        return Credits;
    }

    public void setCredits(int credits) {
        Credits = credits;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }
}
