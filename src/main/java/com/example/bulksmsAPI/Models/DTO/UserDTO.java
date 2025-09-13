package com.example.bulksmsAPI.Models.DTO;


import com.example.bulksmsAPI.Models.BillingAddress;
import com.example.bulksmsAPI.Models.Contacts;

import java.util.Date;
import java.util.List;

public class UserDTO {
    private String username;
    private String email;
    private String password;

    private String driverLicense;
    private List<Contacts> contacts;

    private int Credits;
    private String roles;
    private BillingAddress billingAddress;
    private Integer twoFactorCode; // Add this field
    private Date dob;




    public Integer getTwoFactorCode() {
        return twoFactorCode;
    }

    public void setTwoFactorCode(Integer twoFactorCode) {
        this.twoFactorCode = twoFactorCode;
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

    public String getDriverLicense() {
        return driverLicense;
    }

    public void setDriverLicense(String driverLicense) {
        this.driverLicense = driverLicense;
    }

    public BillingAddress getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(BillingAddress billingAddress) {
        this.billingAddress = billingAddress;
    }
}

