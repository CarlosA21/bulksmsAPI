package com.example.bulksmsAPI.Controller;


import com.example.bulksmsAPI.Models.BillingAddress;
import com.example.bulksmsAPI.Models.DTO.BillingAddressDTO;
import com.example.bulksmsAPI.Services.BillingAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    @Autowired
    private BillingAddressService billingService;

    @PostMapping
    public ResponseEntity<?> saveBillingAddress(@RequestBody BillingAddressDTO billingAddressDTO, @RequestParam Long userId) {
        BillingAddress savedBillingAddress = billingService.saveBillingAddress(billingAddressDTO, userId);
        return ResponseEntity.ok(savedBillingAddress);
    }
    @PutMapping("/update")
    public ResponseEntity<?> updateBillingAddress(@RequestBody BillingAddressDTO billingAddressDTO, @RequestParam Long userId) {
        BillingAddressDTO updatedBillingAddress = billingService.updateAddress(userId, billingAddressDTO);
        return ResponseEntity.ok(updatedBillingAddress);
    }

    @GetMapping("/find-billing")
    public ResponseEntity<?> findBillingAddress(@RequestParam Long id) {
        Optional<BillingAddressDTO> billingAddress = billingService.listBillingAddress(id);
        return ResponseEntity.ok(billingAddress);
    }

    @GetMapping("/find-billing-by-user")
    public ResponseEntity<?> findBillingAddressByUser(@RequestParam Long userId) {
        Optional<BillingAddressDTO> billingAddress = billingService.findBillingAddressByUser(userId);
        return ResponseEntity.ok(billingAddress);
    }

}
