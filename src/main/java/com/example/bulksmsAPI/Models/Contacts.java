package com.example.bulksmsAPI.Models;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contacts")

@NoArgsConstructor
@AllArgsConstructor
public class Contacts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contact_id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "`group`") // Usar comillas invertidas para evitar conflictos con palabras reservadas
    private String group;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User usuario;

    public Long getId() {
        return contact_id;
    }

    public void setId(Long id) {
        this.contact_id = id;
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

