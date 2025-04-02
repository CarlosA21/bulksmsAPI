package com.example.bulksmsAPI.Services;

import com.example.bulksmsAPI.Models.DTO.MessageDTO;
import com.example.bulksmsAPI.Models.Messages;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Repositories.MessageRepository;
import com.example.bulksmsAPI.Repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;


    @Transactional
    public void saveMessage(MessageDTO messageDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();
        String email;

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            throw new SecurityException("Invalid authentication principal");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        System.out.println("User: " + user);
        Messages newMessage = new Messages();
        newMessage.setMessage(messageDTO.getMessage());
        newMessage.setPhoneNumber(messageDTO.getPhoneNumber());
        newMessage.setDate(messageDTO.getDate());
        newMessage.setStatus(messageDTO.getStatus());
        newMessage.setCreditvalue(messageDTO.getCreditvalue());
        newMessage.setUser(user);

        messageRepository.save(newMessage);
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
            message.setUser(messageDetails.getUser());
            return messageRepository.save(message);
        }
        return null;
    }

    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }

    public List<Messages> getMessagesByUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        return messageRepository.findByUser(user);
    }


}
