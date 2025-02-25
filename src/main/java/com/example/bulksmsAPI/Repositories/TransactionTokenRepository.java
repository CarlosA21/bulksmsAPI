package com.example.bulksmsAPI.Repositories;

import com.example.bulksmsAPI.Models.TransactionToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionTokenRepository extends JpaRepository<TransactionToken, Long> {
    Optional<TransactionToken> findByTokenAndUsedFalse(String token);

}
