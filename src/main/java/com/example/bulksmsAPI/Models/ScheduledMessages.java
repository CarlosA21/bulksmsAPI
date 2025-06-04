package com.example.bulksmsAPI.Models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "ScheduledMessages")

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledMessages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String message;
    private String phoneNumbers;
    private String scheduledTime;
    private Date scheduledDate;
    private Long creditValue;// Assuming you have a date type for scheduledDate
    private boolean isSent; // To track if the message has been sent
    private String status; // To track the status of the scheduled message (e.g., "Scheduled", "Sent", "Failed")
    private Long userId; // Assuming you want to track which user scheduled the message
}
