package com.example.bulksmsAPI.Services;

import com.example.bulksmsAPI.Models.CreditAccount;
import com.example.bulksmsAPI.Models.DTO.PlanRequest;
import com.example.bulksmsAPI.Models.DTO.StripeResponse;
import com.example.bulksmsAPI.Models.Transaction;
import com.example.bulksmsAPI.Repositories.CreditAccountRepository;
import com.example.bulksmsAPI.Repositories.TransactionRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class StripeService {

    private final CreditAccountRepository creditAccountRepository;
    private final TransactionRepository transactionRepository;

    public StripeService(@Value("${stripe.api.key}") String stripeApiKey,
                         CreditAccountRepository creditAccountRepository,
                         TransactionRepository transactionRepository) {
        Stripe.apiKey = stripeApiKey;
        this.creditAccountRepository = creditAccountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public StripeResponse checkoutProducts(PlanRequest planRequest, Long userId) {
        try {
            // Define product data with credits amount included
            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(planRequest.getPlanName() + " - " + planRequest.getCredits() + " Credits")
                            .build();

            // Define price data
            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(planRequest.getCurrency() != null ? planRequest.getCurrency() : "USD")
                            .setUnitAmount(planRequest.getAmount()) // Stripe uses cents, ensure amount is multiplied by 100 if needed
                            .setProductData(productData)
                            .build();

            // Define line item
            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams.LineItem.builder()
                            .setQuantity(planRequest.getQuantity())
                            .setPriceData(priceData)
                            .build();

            // Create Stripe checkout session
            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl("http://localhost:4200/checkout")
                            .setCancelUrl("http://localhost:8080/api/payment/stripe-cancel")
                            .addLineItem(lineItem)
                            .build();

            Session session = Session.create(params);
            handleSuccessfulPayment(userId, planRequest.getCredits());

            return StripeResponse.builder()
                    .status("SUCCESS")
                    .message("Payment session created successfully")
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .build();
        } catch (StripeException e) {
            return StripeResponse.builder()
                    .status("FAILED")
                    .message("Error creating payment session: " + e.getMessage())
                    .build();
        }
    }

    @Transactional
    public void handleSuccessfulPayment(Long userId, int amount) {
        Optional<CreditAccount> optionalAccount = creditAccountRepository.findByUserId(userId);

        if (optionalAccount.isPresent()) {
            CreditAccount creditAccount = optionalAccount.get();
            creditAccount.setBalance(creditAccount.getBalance() + amount);
            creditAccountRepository.save(creditAccount);

            // Registrar la transacción
            Transaction transaction = new Transaction();
            transaction.setCreditAccount(creditAccount);
            transaction.setType("PURCHASE");
            transaction.setPaymentMethod("STRIPE");
            transaction.setCredits(amount);
            transaction.setDate(LocalDateTime.now());

            transactionRepository.save(transaction);
        } else {
            throw new RuntimeException("No se encontró la cuenta de créditos para el usuario con ID: " + userId);
        }
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
            transaction.setPaymentMethod("Stripe");
            transaction.setCredits(amount);
            transaction.setDate(LocalDateTime.now());

            transactionRepository.save(transaction);
        } else {
            throw new RuntimeException("No se encontró la cuenta de créditos para el usuario con ID: " + userId);
        }
    }
}
