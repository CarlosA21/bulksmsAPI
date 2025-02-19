package com.example.bulksmsAPI.Models.DTO;

import java.time.LocalDateTime;

public class TransactionDTO {
    private String type;
    private int credits;
    private LocalDateTime date;

    public TransactionDTO(String type, int credits, LocalDateTime date) {
        this.type = type;
        this.credits = credits;
        this.date = date;
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
}
