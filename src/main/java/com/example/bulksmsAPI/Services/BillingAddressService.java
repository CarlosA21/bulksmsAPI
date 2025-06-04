package com.example.bulksmsAPI.Services;


import com.example.bulksmsAPI.Models.BillingAddress;
import com.example.bulksmsAPI.Models.DTO.BillingAddressDTO;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Repositories.BillingAddressRepository;
import com.example.bulksmsAPI.Repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BillingAddressService {

    @Autowired
    private BillingAddressRepository billingAddressRepository;

    @Autowired
    private UserRepository userRepository;

    public BillingAddress saveBillingAddress(BillingAddressDTO billingAddressDTO, Long userId) {
        // Fetch the User entity by userId
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found for ID: " + userId));

        // Create a new BillingAddress entity from the DTO
        BillingAddress billingAddress = new BillingAddress();

        billingAddress.setAddressLine1(billingAddressDTO.getAddressLine1());
        billingAddress.setCity(billingAddressDTO.getCity());
        billingAddress.setState(billingAddressDTO.getState());
        billingAddress.setZipCode(billingAddressDTO.getZipCode());
        billingAddress.setCountry(billingAddressDTO.getCountry());
        billingAddress.setUser(user); // Pass User object instead of userId

        // Save the entity to the repository
        return billingAddressRepository.save(billingAddress);
    }
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

    public Optional<BillingAddressDTO> findBillingAddressByUser(Long userId) {
        return billingAddressRepository.findByUserId(userId)
                .map(billingAddress -> new BillingAddressDTO(
                        billingAddress.getAddressLine1(),
                        billingAddress.getCity(),
                        billingAddress.getState(),
                        billingAddress.getZipCode(),
                        billingAddress.getCountry()
                ));
    }
}
