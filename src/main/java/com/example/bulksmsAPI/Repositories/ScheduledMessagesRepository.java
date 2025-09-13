package com.example.bulksmsAPI.Repositories;

import com.example.bulksmsAPI.Models.ScheduledMessages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository

public interface ScheduledMessagesRepository extends JpaRepository<ScheduledMessages, Long> {

    List<ScheduledMessages> findByUserId(Long userId);

    @Query("SELECT s FROM ScheduledMessages s WHERE s.scheduledDate <= :currentTime AND s.isSent = false")
    List<ScheduledMessages> findByScheduledDateBeforeAndIsSentFalse(@Param("currentTime") Date currentTime);

}
