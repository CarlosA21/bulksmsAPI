package com.example.bulksmsAPI.Services;

import com.example.bulksmsAPI.Models.Messages;
import com.example.bulksmsAPI.Repositories.MessageRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;


    public void sendPasswordResetEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset Request");
        message.setText("Click the link below to reset your password:\n" + resetLink);
        message.setFrom("theglobalmessaging@gmail.com");

        mailSender.send(message);
    }

    public void sendCancellationEmail(String to, String reason, Messages msg) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Message Cancelled - SMS Service");
        message.setText("Your SMS message to " + msg.getPhoneNumber() +
            " has been cancelled by an administrator.\n\n" +
            "Message content: " + msg.getMessage() + "\n" +
            "Cancellation reason: " + reason + "\n\n" +
            "If you have any questions, please contact support.");
        message.setFrom("theglobalmessaging@gmail.com");

        mailSender.send(message);
    }

    public void sendAccountApprovedEmail( String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Account Verification Approved - Global Messaging");
        message.setText("Dear " + (email != null ? email : "User") + ",\n\n" +
            "Great news! Your account verification has been approved.\n\n" +
            "You now have full access to all features of Global Messaging services.\n\n" +
            "You can now:\n" +
            "- Send SMS messages\n" +
            "- Manage your contacts\n" +
            "- Schedule messages\n" +
            "- Access all premium features\n\n" +
            "Thank you for choosing Global Messaging!\n\n" +
            "Best regards,\n" +
            "The Global Messaging Team\n\n" +
            "---\n" +
            "If you have any questions, please contact our support team.");
        message.setFrom("theglobalmessaging@gmail.com");

        mailSender.send(message);
    }

    public void sendAccountRejectedEmail(String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Account Verification Update - Global Messaging");
        message.setText("Dear " + (email != null ? email : "User") + ",\n\n" +
            "We regret to inform you that your account verification could not be approved at this time.\n\n" +
            "Please ensure that:\n" +
            "- Your identification document is clear and readable\n" +
            "- All information matches your registration details\n" +
            "- The document is valid and not expired\n\n" +
            "You can upload a new validation document from your account settings.\n\n" +
            "If you have any questions, please contact our support team.\n\n" +
            "Best regards,\n" +
            "The Global Messaging Team");
        message.setFrom("theglobalmessaging@gmail.com");

        mailSender.send(message);
    }
}
