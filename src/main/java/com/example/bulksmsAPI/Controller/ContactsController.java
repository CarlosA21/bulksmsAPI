package com.example.bulksmsAPI.Controller;


import com.example.bulksmsAPI.Models.Contacts;
import com.example.bulksmsAPI.Models.DTO.ContactsDTO;
import com.example.bulksmsAPI.Models.DTO.GroupsDTO;
import com.example.bulksmsAPI.Models.Groups;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Services.ContactsService;
import com.example.bulksmsAPI.Services.GroupService;
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
    @Autowired
    private GroupService groupService;

    @PostMapping("/add")
    public Contacts addContacts(@RequestBody ContactsDTO contactsDTO) {
        return contactsService.addContacts(contactsDTO);
    }
    @PostMapping("/add-batch")
    public ResponseEntity<List<Contacts>> addContactsBatch(@RequestBody List<ContactsDTO> contactsDTOList) {
        try {
            List<Contacts> savedContacts = contactsService.addContactsBatch(contactsDTOList);
            return ResponseEntity.ok(savedContacts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @PostMapping("/createGroup")
    private ResponseEntity<?> createGroup( @RequestBody GroupsDTO groupsDTO) {
        return ResponseEntity.ok(groupService.saveGroup(groupsDTO));
    }

    @GetMapping("/groupsById")
    public ResponseEntity<?> getTagbyId(@RequestParam Long userId) {
        List<Groups> groups = groupService.getTagsByUserId(userId);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/user/{usuarioId}")
    public ResponseEntity<List<Contacts>> getContactsByUser(@PathVariable Long usuarioId) {
        List<Contacts> contacts = contactsService.listContacts(usuarioId);
        return ResponseEntity.ok(contacts);
    }


    @PutMapping("/edit/{contactoId}")
    public void updateContact(@AuthenticationPrincipal User User, @PathVariable Long contactoId, @RequestBody ContactsDTO contactsDTO) {
        contactsService.updateContacts(contactoId, contactsDTO);
    }

    @DeleteMapping("/{contactoId}")
    public void deleteContact(@AuthenticationPrincipal User User, @PathVariable Long contactoId) {
        contactsService.deleteContacts(contactoId);
    }

}
