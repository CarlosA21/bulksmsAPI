package com.example.bulksmsAPI.Services;


import com.example.bulksmsAPI.Models.BillingAddress;
import com.example.bulksmsAPI.Models.DTO.BillingAddressDTO;
import com.example.bulksmsAPI.Repositories.BillingAddressRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BillingAddressService {

    @Autowired
    private BillingAddressRepository billingAddressRepository;

    public Optional<BillingAddressDTO> listBillingAddress(Long userId) {
        return billingAddressRepository.findByUserId(userId)
                .map(billingAddress -> new BillingAddressDTO(
                        billingAddress.getAddressLine1(),
                        billingAddress.getCity(),
                        billingAddress.getState(),
                        billingAddress.getZipCode(),
                        billingAddress.getCountry()
                ));
    }
    public BillingAddressDTO updateAddress(Long userId, BillingAddressDTO billingAddressDTO) {
        BillingAddress billingAddress = billingAddressRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Billing address not found for user ID: " + userId));

        // Update fields
        billingAddress.setAddressLine1(billingAddressDTO.getAddressLine1());
        billingAddress.setCity(billingAddressDTO.getCity());
        billingAddress.setCountry(billingAddressDTO.getCountry());
        billingAddress.setZipCode(billingAddressDTO.getZipCode());
        billingAddress.setState(billingAddressDTO.getState());

        // Save updated entity
        BillingAddress updatedBillingAddress = billingAddressRepository.save(billingAddress);

        // Convert to DTO inline
        return new BillingAddressDTO(
                updatedBillingAddress.getAddressLine1(),
                updatedBillingAddress.getCity(),
                updatedBillingAddress.getCountry(),
                updatedBillingAddress.getZipCode(),
                updatedBillingAddress.getState()
        );
    }

}
