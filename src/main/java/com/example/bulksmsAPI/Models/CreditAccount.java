package com.example.bulksmsAPI.Models;


import jakarta.persistence.*;

import java.util.List;

@Entity

public class CreditAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private User user;

    private int balance; // Créditos disponibles

    @OneToMany(mappedBy = "creditAccount", cascade = CascadeType.ALL)
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
}
