package com.example.bulksmsAPI.Controller;


import com.example.bulksmsAPI.Models.Contacts;
import com.example.bulksmsAPI.Models.DTO.ContactsDTO;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Services.ContactsService;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactsController {
    @Autowired
    private ContactsService contactsService;

    @PostMapping
    public Contacts addContacts(@RequestBody ContactsDTO contactsDTO) {
        return contactsService.addContacts(contactsDTO);
    }

    @GetMapping("/user/{usuarioId}")
    public ResponseEntity<List<Contacts>> getContactsByUser(@PathVariable Long usuarioId) {
        List<Contacts> contacts = contactsService.listContacts(usuarioId);
        return ResponseEntity.ok(contacts);
    }


    @PutMapping("/{contactoId}")
    public void updateContact(@AuthenticationPrincipal User User, @PathVariable Long contactoId, @RequestBody ContactsDTO contactsDTO) {
        contactsService.updateContacts(contactoId, contactsDTO);
    }

    @DeleteMapping("/{contactoId}")
    public void deleteContact(@AuthenticationPrincipal User User, @PathVariable Long contactoId) {
        contactsService.deleteContacts(contactoId);
    }


}
