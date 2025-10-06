package com.example.bulksmsAPI.Controller;


import com.example.bulksmsAPI.Models.DTO.MessageDTO;
import com.example.bulksmsAPI.Models.Messages;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Repositories.UserRepository;
import com.example.bulksmsAPI.Services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    // 🔹 Este endpoint envía el SMS a Horisen y lo guarda en la BD
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody MessageDTO messageDTO) {
        try {
            List<Messages> savedMessages = messageService.sendAndSaveMessage(messageDTO);

            // Crear respuesta con información de todos los mensajes creados
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalMessages", savedMessages.size());
            response.put("messages", savedMessages);
            response.put("message", "Messages created successfully for " + savedMessages.size() + " phone numbers");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 🔹 Endpoint para que el admin apruebe y envíe un mensaje pendiente
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveMessage(@PathVariable Long id) {
        try {
            Messages approvedMessage = messageService.approveAndSendMessage(id);
            return ResponseEntity.ok(approvedMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error approving message: " + e.getMessage());
        }
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAllMessages() {
        try {
            messageService.deleteAllMessages();
            return ResponseEntity.ok("All messages deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting messages: " + e.getMessage());
        }
    }

    // 🔹 Endpoint para que el admin cancele un mensaje pendiente con motivo
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cancelMessage(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Cancellation reason is required");
            }
            Messages cancelledMessage = messageService.cancelMessage(id, reason);
            return ResponseEntity.ok(cancelledMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error cancelling message: " + e.getMessage());
        }
    }

    // 🔹 Endpoint para obtener todos los mensajes pendientes (solo admin)
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MessageDTO>> getPendingMessages() {
        List<MessageDTO> pendingMessages = messageService.getPendingMessagesDTO();
        return ResponseEntity.ok(pendingMessages);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Messages> getMessageById(@PathVariable Long id) {
        Messages message = messageService.getMessageById(id);
        return message != null ? ResponseEntity.ok(message) : ResponseEntity.notFound().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<MessageDTO>> getAllMessages() {
        return ResponseEntity.ok(messageService.getAllMessagesDTO());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Messages> updateMessage(@PathVariable Long id, @RequestBody Messages messageDetails) {
        Messages updatedMessage = messageService.updateMessage(id, messageDetails);
        return updatedMessage != null ? ResponseEntity.ok(updatedMessage) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return ResponseEntity.ok("Message deleted successfully");
    }

    @GetMapping("/user")
    public ResponseEntity<List<MessageDTO>> getMessagesByUserId(@RequestParam Long userId) {
        List<MessageDTO> messages = messageService.getMessagesByUser(userId);
        return ResponseEntity.ok(messages);
    }

    // 🔹 Endpoint de prueba para diagnosticar el DTO
    @PostMapping("/test-dto")
    public ResponseEntity<?> testMessageDTO(@RequestBody MessageDTO messageDTO) {
        Map<String, Object> response = new HashMap<>();
        response.put("received_message", messageDTO.getMessage());
        response.put("received_phoneNumbers", messageDTO.getPhoneNumbers());
        response.put("received_phone_number_single", messageDTO.getPhone_number());

        System.out.println("=== TESTING DTO RECEPTION ===");
        System.out.println("MessageDTO received:");
        System.out.println("- message: " + messageDTO.getMessage());
        System.out.println("- phoneNumbers: " + messageDTO.getPhoneNumbers());
        System.out.println("- phone_number (single): " + messageDTO.getPhone_number());
        System.out.println("- phoneNumbers size: " + (messageDTO.getPhoneNumbers() != null ? messageDTO.getPhoneNumbers().size() : "null"));
        System.out.println("=== END TESTING ===");

        return ResponseEntity.ok(response);
    }

}
