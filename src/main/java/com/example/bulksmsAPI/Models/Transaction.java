package com.example.bulksmsAPI.Models;


import jakarta.persistence.*;

import java.time.LocalDateTime;


public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private CreditAccount creditAccount;

    private String type; // "PURCHASE" o "USAGE"
    private int credits;
    private LocalDateTime date;

    public CreditAccount getCreditAccount() {
        return creditAccount;
    }

    public void setCreditAccount(CreditAccount creditAccount) {
        this.creditAccount = creditAccount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
