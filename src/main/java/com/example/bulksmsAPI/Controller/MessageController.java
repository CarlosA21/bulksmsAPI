package com.example.bulksmsAPI.Controller;


import com.example.bulksmsAPI.Models.DTO.MessageDTO;
import com.example.bulksmsAPI.Models.Messages;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Repositories.UserRepository;
import com.example.bulksmsAPI.Services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    // 🔹 Este endpoint envía el SMS a Horisen y lo guarda en la BD
    @PostMapping("/send")
    public ResponseEntity<Messages> sendMessage(@RequestBody MessageDTO messageDTO) {
        Messages savedMessage = messageService.sendAndSaveMessage(messageDTO);
        return ResponseEntity.ok(savedMessage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Messages> getMessageById(@PathVariable Long id) {
        Messages message = messageService.getMessageById(id);
        return message != null ? ResponseEntity.ok(message) : ResponseEntity.notFound().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<Messages>> getAllMessages() {
        return ResponseEntity.ok(messageService.getAllMessages());
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
    public ResponseEntity<List<Messages>> getMessagesByUserId(@RequestParam Long userId) {
        return ResponseEntity.ok(messageService.getMessagesByUser(userId));
    }

}
