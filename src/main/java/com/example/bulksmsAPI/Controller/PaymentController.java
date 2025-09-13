package com.example.bulksmsAPI.Controller;


import com.example.bulksmsAPI.Models.CreditAccount;
import com.example.bulksmsAPI.Models.DTO.PlanDTO;
import com.example.bulksmsAPI.Models.DTO.PlanRequest;
import com.example.bulksmsAPI.Models.DTO.StripeResponse;
import com.example.bulksmsAPI.Models.DTO.TransactionDTO;
import com.example.bulksmsAPI.Models.Transaction;
import com.example.bulksmsAPI.Models.TransactionToken;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Repositories.CreditAccountRepository;
import com.example.bulksmsAPI.Repositories.TransactionRepository;
import com.example.bulksmsAPI.Repositories.TransactionTokenRepository;
import com.example.bulksmsAPI.Services.CreditService;
import com.example.bulksmsAPI.Services.PayPalService;
import com.example.bulksmsAPI.Services.StripeService;
import com.example.bulksmsAPI.Services.UserService;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private UserService userService;
    @Autowired
    private PayPalService payPalService;

    @Autowired
    private CreditService creditService;

    @Autowired
    private StripeService stripeService;

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
    public ResponseEntity<String> createPayment(
            @RequestParam int credits,
            @RequestParam Long userId,
            @RequestParam double amount) {

        User user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // Validación del monto
        if (amount <= 0) {
            return ResponseEntity.badRequest().body("Invalid amount");
        }

        try {
            String paymentUrl = payPalService.createPayment(credits, user.getId(), amount);
            return ResponseEntity.ok(paymentUrl);
        } catch (PayPalRESTException e) {
            return ResponseEntity.badRequest().body("Error creating payment");
        }
    }

    @GetMapping("/success")
    public void paymentSuccess(
            @RequestParam String token,
            @RequestParam String paymentId,
            @RequestParam String PayerID,
            HttpServletResponse response) throws IOException {

        // Extract the token (before the comma if present)
        String cleanToken = token;
        if (token.contains(",")) {
            cleanToken = token.substring(0, token.indexOf(","));
        }

        Optional<TransactionToken> optionalToken = tokenRepository.findByTokenAndUsedFalse(cleanToken);
        if (optionalToken.isEmpty()) {
            String redirectUrl = "http://localhost:4200/payment-success-callback" +
                    "?status=error&message=" + URLEncoder.encode("Invalid or expired token", "UTF-8");
            response.sendRedirect(redirectUrl);
            return;
        }

        TransactionToken transactionToken = optionalToken.get();
        if (transactionToken.getExpiry().isBefore(Instant.now())) {
            String redirectUrl = "http://localhost:4200/payment-success-callback" +
                    "?status=error&message=" + URLEncoder.encode("Token expired", "UTF-8");
            response.sendRedirect(redirectUrl);
            return;
        }

        User user = userService.getUserById(transactionToken.getUserId());
        if (user == null) {
            String redirectUrl = "http://localhost:4200/payment-success-callback" +
                    "?status=error&message=" + URLEncoder.encode("User not found", "UTF-8");
            response.sendRedirect(redirectUrl);
            return;
        }

        try {
            Payment payment = payPalService.executePayment(paymentId, PayerID);

            if (payment.getState().equals("approved")) {
                // Update credit account
                CreditAccount creditAccount = creditAccountRepository.findByUserId(user.getId())
                        .orElseGet(() -> {
                            CreditAccount newAccount = new CreditAccount();
                            newAccount.setUser(user);
                            newAccount.setBalance(0);
                            return creditAccountRepository.save(newAccount);
                        });

                creditAccount.setBalance(creditAccount.getBalance() + transactionToken.getCredits());
                creditAccountRepository.save(creditAccount);

                // Save transaction
                Transaction transaction = new Transaction();
                transaction.setCreditAccount(creditAccount);
                transaction.setType("PURCHASE");
                transaction.setCredits(transactionToken.getCredits());
                transaction.setDate(LocalDateTime.now());
                transaction.setPaymentMethod("PAYPAL");
                transactionRepository.save(transaction);

                // Mark token as used
                transactionToken.setUsed(true);
                tokenRepository.save(transactionToken);

                // Redirect to success callback
                String redirectUrl = "http://localhost:4200/payment-success-callback" +
                        "?token=" + cleanToken +
                        "&paymentId=" + paymentId +
                        "&PayerID=" + PayerID +
                        "&status=success" +
                        "&credits=" + transactionToken.getCredits();

                response.sendRedirect(redirectUrl);

            } else {
                String redirectUrl = "http://localhost:4200/payment-success-callback" +
                        "?status=error&message=" + URLEncoder.encode("Payment not approved", "UTF-8");
                response.sendRedirect(redirectUrl);
            }

        } catch (PayPalRESTException e) {
            String redirectUrl = "http://localhost:4200/payment-success-callback" +
                    "?status=error&message=" + URLEncoder.encode("Error processing payment: " + e.getMessage(), "UTF-8");
            response.sendRedirect(redirectUrl);
        }
    }


    /**
     * Endpoint para consultar el saldo de créditos.
     */
    @GetMapping("/balance/{userId}")
    public ResponseEntity<Integer> getCreditBalance(@PathVariable Long userId) {
        int balance = creditService.getCreditBalance(userId);
        return ResponseEntity.ok(balance);
    }

    /**
     * Endpoint para consultar el historial de transacciones.
     */
    @GetMapping("/transactions")
        public ResponseEntity<List<TransactionDTO>> getTransactions(@RequestParam Long userId) {
            List<TransactionDTO> transactions = creditService.getTransactionHistory(userId);
            return ResponseEntity.ok(transactions);
    }

//    @PostMapping("/StripeCreatePayment")
//    public ResponseEntity<Map<String, Object>> createPayment(@RequestParam long amount, @RequestParam String currency, @RequestParam String description) {
//        try {
//            PaymentIntent paymentIntent = stripeService.createPaymentIntent(amount, currency, description);
//            Map<String, Object> responseData = new HashMap<>();
//            responseData.put("id", paymentIntent.getId());
//            responseData.put("status", paymentIntent.getStatus());
//            responseData.put("amount", paymentIntent.getAmount());
//            responseData.put("currency", paymentIntent.getCurrency());
//            responseData.put("description", paymentIntent.getDescription());
//            return ResponseEntity.ok(responseData);
//        } catch (StripeException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }

//    @PostMapping("/checkout-session")
//    public Map<String, String> createCheckoutSession(@RequestParam String priceId) {
//        Map<String, String> response = new HashMap<>();
//        try {
//            String sessionUrl = stripeService.createCheckoutSession(priceId);
//            response.put("url", sessionUrl);
//        } catch (StripeException e) {
//            response.put("error", e.getMessage());
//        }
//        return response;
//    }

    @PostMapping("/checkout-session")
    public ResponseEntity<StripeResponse> createCheckoutSession(@RequestBody PlanRequest planRequest,
                                                                @RequestParam Long userId) {

        User user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body(
                    StripeResponse.builder()
                            .status("FAILED")
                            .message("User not found")
                            .build()
            );
        }

        // Validación del monto
        if (planRequest.getAmount() <= 0) {
            return ResponseEntity.badRequest().body(
                    StripeResponse.builder()
                            .status("FAILED")
                            .message("Invalid amount")
                            .build()
            );
        }

        // Validación de créditos
        if (planRequest.getCredits() <= 0) {
            return ResponseEntity.badRequest().body(
                    StripeResponse.builder()
                            .status("FAILED")
                            .message("Invalid credits amount")
                            .build()
            );
        }

        try {
            StripeResponse response = stripeService.checkoutProducts(planRequest, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    StripeResponse.builder()
                            .status("FAILED")
                            .message("Error creating payment session: " + e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/stripe-success")
    public void stripeSuccess(
            @RequestParam String token,
            @RequestParam String session_id,
            @RequestParam Long userId,
            @RequestParam int credits,
            HttpServletResponse response) throws IOException {

        stripeService.handleStripeSuccess(token, session_id, userId, credits, response);
    }
    @PostMapping("/stripe-cancel")
    public ResponseEntity<String> paymentFailed(@RequestParam Long userId, @RequestParam int amount) {
        stripeService.handleFailedPayment(userId, amount);
        return ResponseEntity.ok("Payment canceled! No credits added.");
    }

    @PostMapping("/deduct")
    public ResponseEntity deductCredits(@RequestParam Long userId, @RequestParam int credits) {
        creditService.deductCredits(userId, credits);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/transactions/all")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(creditService.getAllTransactions());
    }
}
