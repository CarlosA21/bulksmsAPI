package com.example.bulksmsAPI.Models;


import jakarta.persistence.*;

import java.util.List;

@Entity

public class CreditAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cada usuario tiene una única cuenta de créditos.
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private int balance = 0; // Saldo inicial

    @OneToMany(mappedBy = "creditAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void addCredits(int credits) {
        this.balance += credits;
    }

    public void subtractCredits(int credits) {
        if (this.balance < credits) {
            throw new RuntimeException("Saldo insuficiente");
        }
        this.balance -= credits;
    }
}
