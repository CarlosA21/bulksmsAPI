package com.example.bulksmsAPI.Controller;

import com.example.bulksmsAPI.Models.DTO.PurchaseRequest;
import com.example.bulksmsAPI.Models.DTO.TransactionDTO;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Services.CreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credits")
public class CreditController {
    @Autowired
    private CreditService creditService;

    @GetMapping("/balance")
    public ResponseEntity<Integer> getBalance(@AuthenticationPrincipal User user) {
        int balance = creditService.getBalance(user.getId());
        return ResponseEntity.ok(balance);
    }
    @PostMapping("/buy")
    public ResponseEntity<String> buyCredits(@AuthenticationPrincipal User user, @RequestBody PurchaseRequest request) {
        creditService.processPayment(user.getId(), request);
        return ResponseEntity.ok("Compra de créditos exitosa");
    }

    @PostMapping("/use")
    public ResponseEntity<String> useCredits(@AuthenticationPrincipal User user, @RequestParam int credits) {
        creditService.useCredits(user.getId(), credits);
        return ResponseEntity.ok("Uso de créditos registrado");
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDTO>> getTransactions(@AuthenticationPrincipal User user) {
        List<TransactionDTO> transactions = creditService.getTransactionHistory(user.getId());
        return ResponseEntity.ok(transactions);
    }


}
