package com.example.bulksmsAPI.Services;

import com.example.bulksmsAPI.Models.TransactionToken;
import com.example.bulksmsAPI.Repositories.TransactionTokenRepository;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PayPalService {

    private static final Logger log = LoggerFactory.getLogger(PayPalService.class);

    @Autowired
    private TransactionTokenRepository tokenRepository;

    @Value("${paypal.clientId}")
    private String clientId;

    @Value("${paypal.clientSecret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;



    @Transactional
    public String createPayment(int credits, Long userId) throws PayPalRESTException {
        String secureToken = UUID.randomUUID().toString();
        TransactionToken token = new TransactionToken();
        token.setToken(secureToken);
        token.setUserId(userId);
        token.setCredits(credits); // Set credits in the token
        token.setUsed(false);
        token.setExpiry(Instant.now().plus(Duration.ofMinutes(30)));

        try {
            tokenRepository.save(token);
            log.info("Token saved: {}", secureToken);
        } catch (Exception e) {
            log.error("Error saving token: {}", e.getMessage(), e);
            throw e; // Re-throw to rollback if necessary
        }

        String successUrl = "http://localhost:8080/api/payment/success?token=" + secureToken;
        String cancelUrl = "http://localhost:8080/api/payment/cancel";

        APIContext apiContext = new APIContext(clientId, clientSecret, mode);

        Amount amount = new Amount();
        amount.setCurrency("USD");
        amount.setTotal(String.format("%.2f", credits * 0.10));

        Transaction transaction = new Transaction();
        transaction.setDescription("Compra de Créditos");
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);


        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);

        try {
            Payment createdPayment = payment.create(apiContext);

            for (Links link : createdPayment.getLinks()) {
                if (link.getRel().equals("approval_url")) {
                    return link.getHref();
                }
            }
        } catch (PayPalRESTException e) {
            log.error("Error creating PayPal payment: {}", e.getMessage(), e);
            throw e;
        }

        return null; // Handle the case where no approval URL is found
    }


    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        APIContext apiContext = new APIContext(clientId, clientSecret, mode);
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        return payment.execute(apiContext, paymentExecution);
    }


}




