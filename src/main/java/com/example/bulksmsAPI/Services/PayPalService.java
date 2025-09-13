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

    @Value("${PAYPAL_CLIENT_ID:${paypal.clientId:}}")
    private String clientId;

    @Value("${PAYPAL_CLIENT_SECRET:${paypal.clientSecret:}}")
    private String clientSecret;

    @Value("${PAYPAL_MODE:${paypal.mode:sandbox}}")
    private String mode;

    @Transactional
    public String createPayment(int credits, Long userId, double amount) throws PayPalRESTException {
        String secureToken = UUID.randomUUID().toString();
        TransactionToken token = new TransactionToken();
        token.setToken(secureToken);
        token.setUserId(userId);
        token.setCredits(credits);
        token.setUsed(false);
        token.setExpiry(Instant.now().plus(Duration.ofMinutes(30)));

        try {
            tokenRepository.save(token);
            log.info("Token saved: {}", secureToken);
        } catch (Exception e) {
            log.error("Error saving token: {}", e.getMessage(), e);
            throw e;
        }

        // URLs actualizadas según la conversación anterior
        String successUrl = "http://localhost:8080/api/payment/success?token=" + secureToken;
        String cancelUrl = "http://localhost:4200/credits"; // Redirección directa a credits

        APIContext apiContext = new APIContext(clientId, clientSecret, mode);

        Amount paypalAmount = new Amount();
        paypalAmount.setCurrency("USD");
        // Usar el amount que viene del frontend en lugar del cálculo fijo
        paypalAmount.setTotal(String.format("%.2f", amount));

        Transaction transaction = new Transaction();
        transaction.setDescription("Compra de Créditos - " + credits + " credits");
        transaction.setAmount(paypalAmount);

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

        return null;
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
