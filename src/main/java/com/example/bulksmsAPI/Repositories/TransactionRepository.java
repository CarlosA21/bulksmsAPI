package com.example.bulksmsAPI.Repositories;

import com.example.bulksmsAPI.Models.CreditAccount;
import com.example.bulksmsAPI.Models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByCreditAccount(CreditAccount creditAccount);
}
