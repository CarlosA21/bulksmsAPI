package com.example.bulksmsAPI.Services;

import com.example.bulksmsAPI.Models.DTO.MessageDTO;
import com.example.bulksmsAPI.Models.Messages;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Repositories.MessageRepository;
import com.example.bulksmsAPI.Repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Messages sendAndSaveMessage(MessageDTO messageDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();
        String email;

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            email = (String) principal;
        } else {
            throw new SecurityException("Invalid authentication principal");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            String port = "8080";
            System.out.println("Server IP: " + ip + ", Port: " + port);
        } catch (Exception e) {
            System.out.println("Could not determine server IP: " + e.getMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("type", "text");

        Map<String, String> auth = new HashMap<>();
        auth.put("username", "15525_BRMRetail");
        auth.put("password", "HNvdMRqZ");
        body.put("auth", auth);

        body.put("sender", "BulkTest");
        body.put("receiver", messageDTO.getPhoneNumber());
        body.put("dcs", "GSM");
        body.put("text", messageDTO.getMessage());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        String status;
        double creditValue = 0.0; // Inicializar en 0

        try {
            ResponseEntity<String> response = new RestTemplate().postForEntity(
                    "http://194.0.137.123:32112/bulk/sendsms",
                    request,
                    String.class
            );

            System.out.println("Horisen API response: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                status = "successful";
                creditValue = messageDTO.getCreditvalue(); // Solo deducir si es exitoso
                System.out.println("SMS sent successfully.");
            } else {
                status = "failed";
                System.out.println("SMS sending failed.");
            }
        } catch (Exception ex) {
            status = "failed";
            System.out.println("SMS sending failed. Exception: " + ex.getMessage());
        }

        Messages newMessage = new Messages();
        newMessage.setMessage(messageDTO.getMessage());
        newMessage.setPhoneNumber(messageDTO.getPhoneNumber());
        newMessage.setDate(new Date());
        newMessage.setCreditvalue((long) creditValue);
        newMessage.setStatus(status);
        newMessage.setUser(user);

        return messageRepository.save(newMessage);
    }


    public Messages getMessageById(Long id) {
        return messageRepository.findById(id).orElse(null);
    }

    public List<Messages> getAllMessages() {
        return messageRepository.findAll();
    }

    public Messages updateMessage(Long id, Messages messageDetails) {
        Messages message = messageRepository.findById(id).orElse(null);
        if (message != null) {
            message.setMessage(messageDetails.getMessage());
            message.setPhoneNumber(messageDetails.getPhoneNumber());
            message.setDate(messageDetails.getDate());
            message.setStatus(messageDetails.getStatus());
            message.setCreditvalue(messageDetails.getCreditvalue());
            message.setUser(messageDetails.getUser());
            return messageRepository.save(message);
        }
        return null;
    }

    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }

    public List<Messages> getMessagesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return messageRepository.findByUser(user);
    }
}

