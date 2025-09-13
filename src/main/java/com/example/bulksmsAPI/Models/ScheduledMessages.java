package com.example.bulksmsAPI.Models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "scheduled_messages")


@NoArgsConstructor
@AllArgsConstructor
public class ScheduledMessages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String message;
    private String phoneNumbers;
    // Solo este campo para fecha y hora de envío (hasta minutos)
    @Temporal(TemporalType.TIMESTAMP)
    private Date scheduledDate;

    private Long creditValue;// Assuming you have a date type for scheduledDate
    private boolean isSent; // To track if the message has been sent
    private String status; // To track the status of the scheduled message (e.g., "Scheduled", "Sent", "Failed")
    private Long userId;

    @Version
    private Long version;// Assuming you want to track which user scheduled the message


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(String phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public Long getCreditValue() {
        return creditValue;
    }

    public void setCreditValue(Long creditValue) {
        this.creditValue = creditValue;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

}
