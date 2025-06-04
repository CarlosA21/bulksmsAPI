package com.example.bulksmsAPI.Repositories;

import com.example.bulksmsAPI.Models.ScheduledMessages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface ScheduledMessagesRepository extends JpaRepository<ScheduledMessages, Long> {
    List<ScheduledMessages> findByScheduledDateAndScheduledTime(Date scheduledDate, String scheduledTime);

    List<ScheduledMessages> findByUserId(Long userId);

    // You can define custom query methods here if needed
    // For example, to find scheduled messages by userId or status
    // List<ScheduledMessages> findByUserId(Long userId);
    // List<ScheduledMessages> findByStatus(String status);ZZZZZZZZZZZZZZZZZXXZ
}
