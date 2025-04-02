package com.example.bulksmsAPI.Controller;


import com.example.bulksmsAPI.Models.CreditAccount;
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
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public RedirectView paymentSuccess(
            @RequestParam String token,
            @RequestParam String paymentId,
            @RequestParam String PayerID) {

        // Extract the token (before the comma if present).
        String cleanToken = token;
        if (token.contains(",")) {
            cleanToken = token.substring(0, token.indexOf(","));
        }

        System.out.println("Cleaned Token: " + cleanToken); // Log the cleaned token

        Optional<TransactionToken> optionalToken = tokenRepository.findByTokenAndUsedFalse(cleanToken);
        if (optionalToken.isEmpty()) {
            return new RedirectView("/credits?paymentStatus=error&message=Invalid or expired token"); // Redirect with error
        }

        TransactionToken transactionToken = optionalToken.get();
        if (transactionToken.getExpiry().isBefore(Instant.now())) {
            return new RedirectView("/credits?paymentStatus=error&message=Token expired"); // Redirect with error
        }

        User user = userService.getUserById(transactionToken.getUserId());
        if (user == null) {
            return new RedirectView("/credits?paymentStatus=error&message=User not found"); // Redirect with error
        }

        try {
            Payment payment = payPalService.executePayment(paymentId, PayerID);

            if (payment.getState().equals("approved")) {
                // ... credit account update and transaction saving (same as before) ...

                CreditAccount creditAccount = creditAccountRepository.findByUserId(user.getId())
                        .orElseGet(() -> {
                            CreditAccount newAccount = new CreditAccount();
                            newAccount.setUser(user);
                            newAccount.setBalance(0);
                            return creditAccountRepository.save(newAccount); // <-- RETURN statement is here now
                        });

                creditAccount.setBalance(creditAccount.getBalance() + transactionToken.getCredits());
                creditAccountRepository.save(creditAccount);

                Transaction transaction = new Transaction();
                transaction.setCreditAccount(creditAccount);
                transaction.setType("PURCHASE");
                transaction.setCredits(transactionToken.getCredits());
                transaction.setDate(LocalDateTime.now());
                transaction.setPaymentMethod("PAYPAL");
                transactionRepository.save(transaction);

                transactionToken.setUsed(true);
                tokenRepository.save(transactionToken);

                // Redirect to Angular route with success message as query parameter
                String successMessage = "Payment successful and " + transactionToken.getCredits() + " credits added to your account.";
                String redirectUrl = "/api/paypal/payment-success-callback?paymentStatus=success&message=" + java.net.URLEncoder.encode(successMessage, "UTF-8"); // Encode message
                return new RedirectView(redirectUrl);


            } else {
                return new RedirectView("/credits?paymentStatus=error&message=Payment not approved"); // Redirect with error
            }

        } catch (PayPalRESTException e) {
            return new RedirectView("/credits?paymentStatus=error&message=Error processing payment"); // Redirect with error
        } catch (java.io.UnsupportedEncodingException e) { // Handle encoding exception
            return new RedirectView("/credits?paymentStatus=error&message=Error encoding success message");
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
        StripeResponse response = stripeService.checkoutProducts(planRequest, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stripe-success")
    public ResponseEntity<String> paymentSuccess(@RequestParam Long userId, @RequestParam int amount) {
        stripeService.handleSuccessfulPayment(userId, amount);
        return ResponseEntity.ok("Payment successful! Credits added.");
    }
    @PostMapping("/stripe-cancel")
    public ResponseEntity<String> paymentFailed(@RequestParam Long userId, @RequestParam int amount) {
        stripeService.handleFailedPayment(userId, amount);
        return ResponseEntity.ok("Payment canceled! No credits added.");
    }

    @GetMapping("/cancel")
    public ResponseEntity<String> paymentCancelled() {
        return ResponseEntity.ok("Pago cancelado");
    }

    @PostMapping("/deduct")
    public ResponseEntity deductCredits(@RequestParam Long userId, @RequestParam int credits) {
        creditService.deductCredits(userId, credits);
        return ResponseEntity.ok().build();
    }
}
