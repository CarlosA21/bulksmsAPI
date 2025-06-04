package com.example.bulksmsAPI.Services;

import com.example.bulksmsAPI.Models.Contacts;
import com.example.bulksmsAPI.Models.DTO.ContactsDTO;
import com.example.bulksmsAPI.Models.DTO.UserDTO;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Repositories.ContactsRepository;
import com.example.bulksmsAPI.Repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@Service
public class ContactsService {

    private static final Logger logger = LoggerFactory.getLogger(ContactsService.class);

    @Autowired
    private ContactsRepository contactsRepository; // Corrected variable name for consistency

    @Autowired
    private UserRepository userRepository; // Corrected variable name for consistency



    public Contacts addContacts(ContactsDTO contactsDTO) {
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

        // Create and save the contact
        Contacts contact = new Contacts();
        contact.setUsuario(user);
        contact.setName(contactsDTO.getName());
        contact.setPhoneNumber(contactsDTO.getPhoneNumber());
        contact.setGroup(contactsDTO.getGroup());

        return contactsRepository.save(contact);
    }

    public List<Contacts> listContacts(Long usuarioId) {
        return contactsRepository.findByUsuarioId(usuarioId);
    }

    public void updateContacts(Long contactsId, ContactsDTO contactsDTO) {
        Contacts contact = contactsRepository.findById(contactsId)
                .orElseThrow(() -> new RuntimeException("Contacts Not Found"));

        contact.setName(contactsDTO.getName());
        contact.setPhoneNumber(contactsDTO.getPhoneNumber());
        contact.setGroup(contactsDTO.getGroup());

        contactsRepository.save(contact);
    }

    public void deleteContacts(Long contactsId) {
        contactsRepository.deleteById(contactsId);
    }




}