package com.example.bulksmsAPI.Services;

import com.example.bulksmsAPI.Models.CreditAccount;
import com.example.bulksmsAPI.Models.DTO.PurchaseRequest;
import com.example.bulksmsAPI.Models.DTO.TransactionDTO;
import com.example.bulksmsAPI.Models.Transaction;
import com.example.bulksmsAPI.Repositories.CreditAccountRepository;
import com.example.bulksmsAPI.Repositories.TransactionRepository;
import com.example.bulksmsAPI.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreditService {

    @Autowired
    private CreditAccountRepository creditAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;


    public int getBalance(Long userId) {
        CreditAccount account = creditAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cuenta de crédito no encontrada"));
        return account.getBalance();
    }
    @Transactional
    public void processPayment(Long userId, PurchaseRequest request) {
        CreditAccount account = creditAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cuenta de crédito no encontrada"));

        int creditsPurchased = request.getAmount();
        account.setBalance(account.getBalance() + creditsPurchased);
        creditAccountRepository.save(account);

        // Registrar la transacción
        Transaction transaction = new Transaction();
        transaction.setCreditAccount(account);
        transaction.setType("PURCHASE");
        transaction.setCredits(creditsPurchased);
        transaction.setDate(LocalDateTime.now());

        transactionRepository.save(transaction);
    }
    @Transactional
    public void useCredits(Long userId, int creditsToUse) {
        CreditAccount account = creditAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cuenta de crédito no encontrada"));

        if (account.getBalance() < creditsToUse) {
            throw new RuntimeException("Saldo insuficiente");
        }

        account.setBalance(account.getBalance() - creditsToUse);
        creditAccountRepository.save(account);

        // Registrar la transacción
        Transaction transaction = new Transaction();
        transaction.setCreditAccount(account);
        transaction.setType("USAGE");
        transaction.setCredits(creditsToUse);
        transaction.setDate(LocalDateTime.now());

        transactionRepository.save(transaction);
    }
    public List<TransactionDTO> getTransactionHistory(Long userId) {
        CreditAccount account = creditAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cuenta de crédito no encontrada"));

        List<Transaction> transactions = transactionRepository.findByCreditAccount(account);

        return transactions.stream()
                .map(t -> new TransactionDTO(t.getType(), t.getCredits(), t.getDate()))
                .collect(Collectors.toList());
    }

}
