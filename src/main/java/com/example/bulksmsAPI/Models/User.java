package com.example.bulksmsAPI.Models;


import jakarta.persistence.*;

import lombok.*;

import java.util.List;

@Entity
@Table(name = "User")

@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long user_id;
    private String email;
    private String password;

    private String roles;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contacts> contacts;


    public Long getId() {
        return user_id;
    }

    public void setId(Long id) {
        this.user_id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Contacts> getContactos() {
        return contacts;
    }

    public void setContactos(List<Contacts> contactos) {
        this.contacts = contactos;
    }

    public int getCredits() {
        return Credits;
    }

    public void setCredits(int credits) {
        Credits = credits;
    }
}
