package com.example.bulksmsAPI.Controller;


import com.example.bulksmsAPI.Models.Contacts;
import com.example.bulksmsAPI.Models.DTO.ContactsDTO;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Services.ContactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contactos")
public class ContactsController {
    @Autowired
    private ContactsService contactoService;

    @PostMapping
    public ContactsDTO agregarContacto(@AuthenticationPrincipal User user, @RequestBody ContactsDTO contactsDTO) {
        Contacts contact = contactoService.addContacts(user.getId(), contactsDTO);
        return new ContactsDTO(contact);
    }

    @GetMapping
    public List<Contacts> listarContactos(@AuthenticationPrincipal User User) {
        return contactoService.listContacts(User.getId());
    }

    @PutMapping("/{contactoId}")
    public void updateContact(@AuthenticationPrincipal User User, @PathVariable Long contactoId, @RequestBody ContactsDTO contactsDTO) {
        contactoService.updateContacts(contactoId, contactsDTO);
    }

    @DeleteMapping("/{contactoId}")
    public void eliminarContact(@AuthenticationPrincipal User User, @PathVariable Long contactoId) {
        contactoService.deleteContacts(contactoId);
    }


}
