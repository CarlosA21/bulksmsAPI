package com.example.bulksmsAPI.Services;

import com.example.bulksmsAPI.Models.CreditAccount;
import com.example.bulksmsAPI.Models.DTO.PlanRequest;
import com.example.bulksmsAPI.Models.DTO.StripeResponse;
import com.example.bulksmsAPI.Models.Transaction;
import com.example.bulksmsAPI.Models.TransactionToken;
import com.example.bulksmsAPI.Repositories.CreditAccountRepository;
import com.example.bulksmsAPI.Repositories.TransactionRepository;
import com.example.bulksmsAPI.Repositories.TransactionTokenRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class StripeService {

    private final CreditAccountRepository creditAccountRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    private TransactionTokenRepository tokenRepository;

    @Autowired
    private UserService userService;

    public StripeService(@Value("${STRIPE_API_KEY:${stripe.api.key:}}") String stripeApiKey,
                         CreditAccountRepository creditAccountRepository,
                         TransactionRepository transactionRepository) {
        Stripe.apiKey = stripeApiKey;
        this.creditAccountRepository = creditAccountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public StripeResponse checkoutProducts(PlanRequest planRequest, Long userId) {
        try {
            // Generar token único para Stripe similar a PayPal
            String secureToken = UUID.randomUUID().toString();

            // Guardar el token en la base de datos (similar a PayPal)
            TransactionToken token = new TransactionToken();
            token.setToken(secureToken);
            token.setUserId(userId);
            token.setCredits(planRequest.getCredits());
            token.setUsed(false);
            token.setExpiry(Instant.now().plus(Duration.ofMinutes(30)));

            try {
                tokenRepository.save(token);
            } catch (Exception e) {
                throw new RuntimeException("Error saving token: " + e.getMessage());
            }

            // Define product data with credits amount included
            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(planRequest.getPlanName() + " - " + planRequest.getCredits() + " Credits")
                            .build();

            // Define price data usando el amount del planRequest
            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(planRequest.getCurrency() != null ? planRequest.getCurrency() : "USD")
                            .setUnitAmount(planRequest.getAmount()) // Usar amount del frontend
                            .setProductData(productData)
                            .build();

            // Define line item
            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams.LineItem.builder()
                            .setQuantity(planRequest.getQuantity())
                            .setPriceData(priceData)
                            .build();

            // URLs actualizadas para coincidir con PayPal
            String successUrl = "http://localhost:8000/api/payment/stripe-success" +
                    "?token=" + secureToken +
                    "&userId=" + userId +
                    "&credits=" + planRequest.getCredits() +
                    "&session_id={CHECKOUT_SESSION_ID}";

            String cancelUrl = "http://localhost:4200/credits";

            // Create Stripe checkout session
            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl(successUrl)
                            .setCancelUrl(cancelUrl)
                            .addLineItem(lineItem)
                            .putMetadata("userId", userId.toString())
                            .putMetadata("credits", String.valueOf(planRequest.getCredits()))
                            .putMetadata("token", secureToken)
                            .build();

            Session session = Session.create(params);

            return StripeResponse.builder()
                    .status("SUCCESS")
                    .message("Payment session created successfully")
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .token(secureToken)
                    .build();

        } catch (StripeException e) {
            return StripeResponse.builder()
                    .status("FAILED")
                    .message("Error creating payment session: " + e.getMessage())
                    .build();
        }
    }

    @Transactional
    public void handleStripeSuccess(String token, String sessionId, Long userId, int credits,
                                    HttpServletResponse response) throws IOException {
        try {
            // Verificar el token en la base de datos (similar a PayPal)
            Optional<TransactionToken> optionalToken = tokenRepository.findByTokenAndUsedFalse(token);
            if (optionalToken.isEmpty()) {
                String redirectUrl = "http://localhost:4200/payment-success-callback" +
                        "?status=error&message=" + URLEncoder.encode("Invalid or expired token", "UTF-8") +
                        "&provider=stripe";
                response.sendRedirect(redirectUrl);
                return;
            }

            TransactionToken transactionToken = optionalToken.get();
            if (transactionToken.getExpiry().isBefore(Instant.now())) {
                String redirectUrl = "http://localhost:4200/payment-success-callback" +
                        "?status=error&message=" + URLEncoder.encode("Token expired", "UTF-8") +
                        "&provider=stripe";
                response.sendRedirect(redirectUrl);
                return;
            }

            // Verificar que la sesión de Stripe sea válida
            Session session = Session.retrieve(sessionId);

            if ("complete".equals(session.getStatus()) && "paid".equals(session.getPaymentStatus())) {
                // Procesar el pago exitoso usando el método unificado
                processSuccessfulPayment(userId, transactionToken.getCredits());

                // Marcar token como usado
                transactionToken.setUsed(true);
                tokenRepository.save(transactionToken);

                // Redireccionar con los parámetros adaptados para coincidir con PayPal
                String redirectUrl = "http://localhost:4200/payment-success-callback" +
                        "?token=" + token +
                        "&paymentId=" + sessionId +
                        "&PayerID=" + (session.getCustomer() != null ? session.getCustomer() : "stripe_customer") +
                        "&status=success" +
                        "&credits=" + transactionToken.getCredits() +
                        "&provider=stripe";

                response.sendRedirect(redirectUrl);
            } else {
                String redirectUrl = "http://localhost:4200/payment-success-callback" +
                        "?status=error" +
                        "&message=" + URLEncoder.encode("Payment not completed", "UTF-8") +
                        "&provider=stripe";
                response.sendRedirect(redirectUrl);
            }

        } catch (StripeException e) {
            String redirectUrl = "http://localhost:4200/payment-success-callback" +
                    "?status=error" +
                    "&message=" + URLEncoder.encode("Error verifying payment: " + e.getMessage(), "UTF-8") +
                    "&provider=stripe";
            response.sendRedirect(redirectUrl);
        }
    }

    // Método unificado para procesar pagos exitosos
    @Transactional
    public void processSuccessfulPayment(Long userId, int credits) {
        // Buscar o crear cuenta de créditos
        CreditAccount creditAccount = creditAccountRepository.findByUserId(userId)
                .orElseGet(() -> {
                    CreditAccount newAccount = new CreditAccount();
                    newAccount.setUser(userService.getUserById(userId));
                    newAccount.setBalance(0);
                    return creditAccountRepository.save(newAccount);
                });

        // Actualizar saldo
        creditAccount.setBalance(creditAccount.getBalance() + credits);
        creditAccountRepository.save(creditAccount);

        // Registrar transacción
        Transaction transaction = new Transaction();
        transaction.setCreditAccount(creditAccount);
        transaction.setType("PURCHASE");
        transaction.setCredits(credits);
        transaction.setDate(LocalDateTime.now());
        transaction.setPaymentMethod("STRIPE");
        transactionRepository.save(transaction);
    }

    @Transactional
    public void handleFailedPayment(Long userId, int amount) {
        Optional<CreditAccount> optionalAccount = creditAccountRepository.findByUserId(userId);

        if (optionalAccount.isPresent()) {
            CreditAccount creditAccount = optionalAccount.get();

            // Registrar la transacción fallida sin modificar el saldo
            Transaction transaction = new Transaction();
            transaction.setCreditAccount(creditAccount);
            transaction.setType("FAILED_PAYMENT");
            transaction.setPaymentMethod("STRIPE");
            transaction.setCredits(amount);
            transaction.setDate(LocalDateTime.now());

            transactionRepository.save(transaction);
        } else {
            throw new RuntimeException("No se encontró la cuenta de créditos para el usuario con ID: " + userId);
        }
    }
}
