package com.example.bulksmsAPI.Repositories;

import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Models.ValidationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository  extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);

    User findByUsername(String username);

    List<User> findByAccountValidated(ValidationStatus accountValidated);
}
