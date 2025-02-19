package com.example.bulksmsAPI.Repositories;

import com.example.bulksmsAPI.Models.CreditAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditAccountRepository extends JpaRepository<CreditAccount, Long> {
    Optional<CreditAccount> findByUserId(Long userId);

}
