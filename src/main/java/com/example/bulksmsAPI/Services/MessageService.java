package com.example.bulksmsAPI.Services;

import com.example.bulksmsAPI.Models.DTO.MessageDTO;
import com.example.bulksmsAPI.Models.Messages;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Repositories.MessageRepository;
import com.example.bulksmsAPI.Repositories.UserRepository;
import jakarta.annotation.PostConstruct;
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
import java.util.*;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CreditService creditService;


    @Transactional
    public List<Messages> sendAndSaveMessage(MessageDTO messageDTO) {
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

        // Log para verificar los datos recibidos
        System.out.println("Message from DTO: " + messageDTO.getMessage());
        System.out.println("Phone numbers from DTO: " + messageDTO.getPhoneNumbers());
        System.out.println("Credit value from DTO: " + messageDTO.getCreditvalue());

        // Obtener lista de números de teléfono
        List<String> phoneNumbers = messageDTO.getPhoneNumbers();
        if (phoneNumbers == null || phoneNumbers.isEmpty()) {
            // Fallback a phone_number único si existe
            String singlePhone = messageDTO.getPhone_number();
            if (singlePhone != null && !singlePhone.isEmpty()) {
                phoneNumbers = List.of(singlePhone);
            } else {
                throw new IllegalArgumentException("No phone numbers provided");
            }
        }

        // Concatenar todos los números de teléfono en un solo string separado por comas
        String concatenatedPhones = String.join(",", phoneNumbers);
        System.out.println("Total phone numbers to process: " + phoneNumbers.size());
        System.out.println("Concatenated phone numbers: " + concatenatedPhones);

        // Crear UN SOLO mensaje con todos los números concatenados
        Messages newMessage = new Messages();
        newMessage.setMessage(messageDTO.getMessage());
        newMessage.setPhoneNumber(concatenatedPhones); // Todos los números juntos
        newMessage.setDate(messageDTO.getDate() != null ? messageDTO.getDate() : new Date());
        newMessage.setCreditvalue(0L); // No se deduce crédito hasta aprobación
        newMessage.setStatus(Messages.Status.PENDING);
        newMessage.setUser(user);

        System.out.println("Before saving - Phone numbers: " + newMessage.getPhoneNumber());

        Messages savedMessage = messageRepository.saveAndFlush(newMessage);

        System.out.println("After saving - Phone numbers: " + savedMessage.getPhoneNumber());
        System.out.println("Saved message ID: " + savedMessage.getMessageId());

        // Retornar una lista con el único mensaje creado para mantener compatibilidad
        return List.of(savedMessage);
    }

    @Transactional
    public Messages approveAndSendMessage(Long messageId) {
        Messages message = messageRepository.findById(messageId.intValue())
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        if (message.getStatus() == null || !message.getStatus().equals("PENDING")) {
            throw new IllegalStateException("Message is not pending approval");
        }

        // Obtener todos los números de teléfono del campo concatenado
        String phoneNumberField = message.getPhoneNumber();
        String[] phoneNumbers = phoneNumberField.split(",");

        System.out.println("Approving message for " + phoneNumbers.length + " phone numbers: " + phoneNumberField);

        // Limpiar espacios en blanco de todos los números
        List<String> cleanPhoneNumbers = new ArrayList<>();
        for (String phoneNumber : phoneNumbers) {
            cleanPhoneNumbers.add(phoneNumber.trim());
        }

        String finalStatus;
        double creditValue = 0.0;

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
        body.put("dcs", "GSM");
        body.put("text", message.getMessage());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request;

        // Si hay más de un número, usar el campo 'messages' como array
        if (cleanPhoneNumbers.size() > 1) {
            List<Map<String, String>> messagesList = new ArrayList<>();
            for (String num : cleanPhoneNumbers) {
                Map<String, String> msgObj = new HashMap<>();
                msgObj.put("receiver", num);
                msgObj.put("text", message.getMessage());
                messagesList.add(msgObj);
            }
            body.remove("receiver");
            body.remove("text");
            body.put("messages", messagesList);
        } else {
            // Un solo número, usar 'receiver' y 'text'
            body.put("receiver", cleanPhoneNumbers.get(0));
            body.put("text", message.getMessage());
        }

        request = new HttpEntity<>(body, headers);

        try {
            System.out.println("Sending bulk SMS request to Horisen. Numbers: " + cleanPhoneNumbers.size());
            ResponseEntity<String> response = new RestTemplate().postForEntity(
                    "http://194.0.137.123:32112/bulk/sendsms",
                    request,
                    String.class
            );
            System.out.println("Horisen API response: " + response.getBody());
            if (response.getStatusCode().is2xxSuccessful()) {
                finalStatus = "SENT";
                creditValue = cleanPhoneNumbers.size();
                System.out.println("Bulk SMS sent successfully to " + cleanPhoneNumbers.size() + " numbers");
                // Descontar créditos solo si el envío fue exitoso
                if (message.getUser() != null) {
                    creditService.deductCredits(message.getUser().getId(), cleanPhoneNumbers.size());
                }
            } else {
                finalStatus = "FAILED";
                creditValue = 0.0;
                System.out.println("Bulk SMS sending failed");
            }
        } catch (Exception ex) {
            finalStatus = "FAILED";
            creditValue = 0.0;
            System.out.println("Bulk SMS sending failed. Exception: " + ex.getMessage());
        }

        message.setStatus(finalStatus);
        message.setCreditvalue((long) creditValue);
        return messageRepository.save(message);
    }

    @Transactional
    public Messages cancelMessage(Long messageId, String reason) {
        Messages message = messageRepository.findById(messageId.intValue())
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        if (message.getStatus() == null || !message.getStatus().equals("PENDING")) {
            throw new IllegalStateException("Only pending messages can be cancelled");
        }

        message.setStatus(Messages.Status.FAILED);
        message.setCancellationReason(reason);
        messageRepository.save(message);

        // Send cancellation email to user
        try {
            emailService.sendCancellationEmail(message.getUser().getEmail(), reason, message);
        } catch (Exception e) {
            System.out.println("Failed to send cancellation email: " + e.getMessage());
        }

        return message;
    }

    public List<Messages> getPendingMessages() {
        return messageRepository.findByStatus(Messages.Status.PENDING);
    }

    public Messages getMessageById(Long id) {
        return messageRepository.findById(id.intValue()).orElse(null);
    }

    public List<Messages> getAllMessages() {
        return messageRepository.findAll();
    }

    public Messages updateMessage(Long id, Messages messageDetails) {
        Messages message = messageRepository.findById(id.intValue()).orElse(null);
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
        messageRepository.deleteById(id.intValue());
    }
    public void deleteAllMessages() {
        messageRepository.deleteAll();
    }

    public List<MessageDTO> getMessagesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<Messages> messages = messageRepository.findByUser(user);
        List<MessageDTO> dtos = new ArrayList<>();
        for (Messages m : messages) {
            MessageDTO dto = new MessageDTO();
            dto.setMessageId(String.valueOf(m.getMessageId()));
            dto.setMessage(m.getMessage());
            dto.setPhone_number(m.getPhoneNumber());
            dto.setStatus(m.getStatus() != null ? m.getStatus() : null);
            dto.setRecipient(m.getPhoneNumber());
            dto.setDenialReason(m.getCancellationReason());
            dtos.add(dto);
        }
        return dtos;
    }

    public List<MessageDTO> getAllMessagesDTO() {
        List<Messages> messages = getAllMessages();
        List<MessageDTO> dtos = new ArrayList<>();
        for (Messages m : messages) {
            MessageDTO dto = new MessageDTO();
            dto.setMessageId(String.valueOf(m.getMessageId()));
            dto.setMessage(m.getMessage());
            dto.setPhone_number(m.getPhoneNumber());
            dto.setDate(m.getDate());
            dto.setUser(m.getUser() != null ? m.getUser().getEmail() : null);
            dto.setRecipient(m.getPhoneNumber());
            dto.setCreditvalue(m.getCreditvalue());
            dto.setStatus(m.getStatus() != null ? m.getStatus() : null);
            dto.setDenialReason(m.getCancellationReason());
            dtos.add(dto);
        }
        return dtos;
    }

    public List<MessageDTO> getPendingMessagesDTO() {
        List<Messages> messages = getPendingMessages();
        List<MessageDTO> dtos = new ArrayList<>();
        for (Messages m : messages) {
            MessageDTO dto = new MessageDTO();
            dto.setMessageId(String.valueOf(m.getMessageId()));
            dto.setMessage(m.getMessage());
            dto.setPhone_number(m.getPhoneNumber());
            dto.setDate(m.getDate());
            dto.setUser(m.getUser() != null ? m.getUser().getEmail() : null);
            dto.setRecipient(m.getPhoneNumber());
            dto.setCreditvalue(m.getCreditvalue());
            dto.setStatus(m.getStatus() != null ? m.getStatus() : null);
            dto.setDenialReason(m.getCancellationReason());
            dtos.add(dto);
        }
        return dtos;
    }
}
