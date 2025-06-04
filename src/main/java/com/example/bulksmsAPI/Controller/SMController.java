package com.example.bulksmsAPI.Controller;

import com.example.bulksmsAPI.Models.ScheduledMessages;
import com.example.bulksmsAPI.Services.ScheduledMesssagesServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduledmessages")
public class SMController {
    @Autowired
    private ScheduledMesssagesServices scheduledMessagesServices;

    @PostMapping("/create")
    public ResponseEntity<ScheduledMessages> createScheduledMessage(@RequestBody ScheduledMessages scheduledMessage) {
        ScheduledMessages createdMessage = scheduledMessagesServices.createScheduledMessage(scheduledMessage);
        return ResponseEntity.ok(createdMessage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduledMessages> getScheduledMessageById(@PathVariable Long id) {
        ScheduledMessages message = scheduledMessagesServices.getScheduledMessageById(id);
        return message != null ? ResponseEntity.ok(message) : ResponseEntity.notFound().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<ScheduledMessages>> getAllScheduledMessages() {
        List<ScheduledMessages> messages = scheduledMessagesServices.getAllScheduledMessages();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ScheduledMessages>> getScheduledMessagesByUserId(@PathVariable Long userId) {
        List<ScheduledMessages> messages = scheduledMessagesServices.getScheduledMessagesByUserId(userId);
        return ResponseEntity.ok(messages);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ScheduledMessages> updateScheduledMessage(@PathVariable Long id, @RequestBody ScheduledMessages updatedMessage) {
        ScheduledMessages message = scheduledMessagesServices.updateScheduledMessage(id, updatedMessage);
        return message != null ? ResponseEntity.ok(message) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteScheduledMessage(@PathVariable Long id) {
        boolean isDeleted = scheduledMessagesServices.deleteScheduledMessage(id);
        return isDeleted ? ResponseEntity.ok("Scheduled message deleted successfully") : ResponseEntity.notFound().build();
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendScheduledMessages() {
        scheduledMessagesServices.sendScheduledMessages();
        return ResponseEntity.ok("Scheduled messages processed successfully");
    }
}
