package com.example.bulksmsAPI.Models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "messages")


public class Messages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int messageId;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    private String phone_number;

    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "creditvalue")
    private long creditvalue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private Status status;

    @Column(name = "cancellationReason", columnDefinition = "TEXT")
    private String cancellationReason;

    public enum Status {
        SENT,
        FAILED,
        PENDING,
        PARTIAL,
        successful// Nuevo status para envíos parcialmente exitosos
    }


    public Messages() {
    }


    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhoneNumber() {
        return phone_number;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phone_number = phoneNumber;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStatus() {
        return status != null ? status.name() : null;
    }

    public void setStatus(String status) {
        this.status = Status.valueOf(status);
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @JsonIgnore
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getCreditvalue() {
        return creditvalue;
    }
    public long setCreditvalue(long creditvalue) {
        return this.creditvalue = creditvalue;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
