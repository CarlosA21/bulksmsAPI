package com.example.bulksmsAPI.Services;

import com.example.bulksmsAPI.Models.CreditAccount;
import com.example.bulksmsAPI.Models.DTO.TransactionDTO;
import com.example.bulksmsAPI.Models.Transaction;
import com.example.bulksmsAPI.Models.User;
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
    private UserRepository userRepository;

    @Autowired
    private CreditAccountRepository creditAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Registra el uso de créditos, disminuyendo el saldo y registrando la transacción.
     */
    @Transactional
    public void deductCredits(Long userId, int credits) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        CreditAccount account = user.getCreditAccount();
        if (account == null) {
            throw new RuntimeException("Cuenta de crédito no encontrada para el usuario");
        }
        account.subtractCredits(credits);
        creditAccountRepository.save(account);

        // Registrar la transacción de consumo
        Transaction transaction = new Transaction();
        transaction.setCreditAccount(account);
        transaction.setType("USAGE");
        transaction.setCredits(credits);
        transaction.setDate(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    /**
     * Devuelve el saldo actual de créditos del usuario.
     */
    public int getCreditBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        CreditAccount account = user.getCreditAccount();
        return account != null ? account.getBalance() : 0;
    }

    /**
     * Obtiene el historial de transacciones del usuario.
     */
    public List<TransactionDTO> getTransactionHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        CreditAccount account = user.getCreditAccount();
        if (account == null) {
            throw new RuntimeException("Cuenta de crédito no encontrada para el usuario");
        }
        List<Transaction> transactions = transactionRepository.findByCreditAccount(account);
        return transactions.stream()
                .map(t -> new TransactionDTO(t.getType(), t.getCredits(), t.getDate()))
                .collect(Collectors.toList());
    }
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }


}
