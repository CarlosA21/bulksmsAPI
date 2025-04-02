package com.example.bulksmsAPI.Repositories;

import com.example.bulksmsAPI.Models.Messages;
import com.example.bulksmsAPI.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Messages, Long> {
    List<Messages> findByUser(User user);

}
