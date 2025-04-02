package com.example.bulksmsAPI.Models.DTO;

import java.util.Date;

public class MessageDTO {
    private String messageId;
    private String message;
    private String phoneNumber;
    private Date date;
    private Long user;
    private long creditvalue;
    private String status;


    public MessageDTO(String messageId, String message, String phoneNumber, Date date, String email, Long user, String status) {
        this.messageId = messageId;
        this.message = message;
        this.phoneNumber = phoneNumber;
        this.date = date;
        this.user = user;
        this.status = status;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getUserId() {
        return user;
    }

    public void setUserId(Long userId) {
        this.user = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreditvalue() {
        return creditvalue;
    }
    public long setCreditvalue(long creditvalue) {
        return this.creditvalue = creditvalue;
    }
}
