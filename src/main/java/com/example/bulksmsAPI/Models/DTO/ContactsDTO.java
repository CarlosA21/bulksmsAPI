package com.example.bulksmsAPI.Models.DTO;

import com.example.bulksmsAPI.Models.Contacts;
import com.example.bulksmsAPI.Models.User;
import jakarta.persistence.*;

public class ContactsDTO {
    private Long contact_id;

    private String name;

    private String phoneNumber;

    private String group;

    private User usuario;

    public ContactsDTO(Contacts contact) {
    }

    public Long getContact_id() {
        return contact_id;
    }

    public void setContact_id(Long contact_id) {
        this.contact_id = contact_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }
}
