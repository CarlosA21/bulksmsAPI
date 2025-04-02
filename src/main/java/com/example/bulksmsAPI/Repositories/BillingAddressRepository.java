package com.example.bulksmsAPI.Repositories;

import com.example.bulksmsAPI.Models.BillingAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillingAddressRepository extends JpaRepository<BillingAddress, Long> {
    Optional<BillingAddress> findByUserId(Long userId);
}
