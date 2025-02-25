package com.example.bulksmsAPI.Controller;


import com.example.bulksmsAPI.Models.CreditAccount;
import com.example.bulksmsAPI.Models.DTO.TransactionDTO;
import com.example.bulksmsAPI.Models.Transaction;
import com.example.bulksmsAPI.Models.TransactionToken;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Repositories.CreditAccountRepository;
import com.example.bulksmsAPI.Repositories.TransactionRepository;
import com.example.bulksmsAPI.Repositories.TransactionTokenRepository;
import com.example.bulksmsAPI.Services.CreditService;
import com.example.bulksmsAPI.Services.PayPalService;
import com.example.bulksmsAPI.Services.UserService;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/paypal")
public class PayPalController {

    @Autowired
    private UserService userService;
    @Autowired
    private PayPalService payPalService;

    @Autowired
    private CreditService creditService;

    @Autowired
    private TransactionTokenRepository tokenRepository;

    @Autowired
    private CreditAccountRepository creditAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Crea un pago y devuelve la URL de aprobación de PayPal.
     */
    @PostMapping("/pay")
    public ResponseEntity<String> createPayment(@RequestParam int credits) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User userDetails) {
            String email = userDetails.getUsername();
            User user = userService.getUserByEmail(email);
            try {
                String paymentUrl = payPalService.createPayment(credits, user.getId());  // Pass credits and user.getId()
                return ResponseEntity.ok(paymentUrl);
            } catch (PayPalRESTException e)
            {
                return ResponseEntity.badRequest().body("Error creating payment");
            }
        } else {
            return ResponseEntity.status(401).body("Unauthorized"); // Or appropriate error response
        }

    }
    @GetMapping("/success")
    public ResponseEntity<String> paymentSuccess(
            @RequestParam String token,
            @RequestParam String paymentId,
            @RequestParam String PayerID) {

        // Extract the token (before the comma if present).
        String cleanToken = token;
        if (token.contains(",")) {
            cleanToken = token.substring(0, token.indexOf(","));
        }

        System.out.println("Cleaned Token: " + cleanToken); // Log the cleaned token

        Optional<TransactionToken> optionalToken = tokenRepository.findByTokenAndUsedFalse(cleanToken); // Use the cleaned token

        if (optionalToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid or expired token");
        }

        TransactionToken transactionToken = optionalToken.get();
        if (transactionToken.getExpiry().isBefore(Instant.now())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token expired");
        }

        User user = userService.getUserById(transactionToken.getUserId());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        try {
            Payment payment = payPalService.executePayment(paymentId, PayerID);

            if (payment.getState().equals("approved")) {
                CreditAccount creditAccount = creditAccountRepository.findByUserId(user.getId())
                        .orElseGet(() -> {
                            CreditAccount newAccount = new CreditAccount();
                            newAccount.setUser(user);
                            newAccount.setBalance(0);
                            return creditAccountRepository.save(newAccount);
                        });

                creditAccount.setBalance(creditAccount.getBalance() + transactionToken.getCredits());
                creditAccountRepository.save(creditAccount);

                Transaction transaction = new Transaction(); // Create the Transaction object
                transaction.setCreditAccount(creditAccount); // Associate with the CreditAccount
                transaction.setType("PURCHASE"); // Set the transaction type
                transaction.setCredits(transactionToken.getCredits()); // Set the credits
                transaction.setDate(LocalDateTime.now()); // Set the date and time
                transactionRepository.save(transaction); // Save the transaction

                transactionToken.setUsed(true);
                tokenRepository.save(transactionToken);

                return ResponseEntity.ok("Payment successful and " + transactionToken.getCredits() + " credits added to your account.");
            } else {
                return ResponseEntity.badRequest().body("Payment not approved"); // Explicitly handle non-approved state
            }

        } catch (PayPalRESTException e) {
            return ResponseEntity.badRequest().body("Error processing payment"); // Return error response
        }
    }

    /**
     * Endpoint para consultar el saldo de créditos.
     */
    @GetMapping("/balance")
    public ResponseEntity<Integer> getCreditBalance(@AuthenticationPrincipal User user) {
        int balance = creditService.getCreditBalance(user.getId());
        return ResponseEntity.ok(balance);
    }

    /**
     * Endpoint para consultar el historial de transacciones.
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDTO>> getTransactions(@AuthenticationPrincipal User user) {
        List<TransactionDTO> transactions = creditService.getTransactionHistory(user.getId());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/cancel")
    public ResponseEntity<String> paymentCancelled() {
        return ResponseEntity.ok("Pago cancelado");
    }

}
