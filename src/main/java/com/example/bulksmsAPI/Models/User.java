package com.example.bulksmsAPI.Models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "User")

@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long user_id;
    private String username;
    private String email;
    private String password;
    private String roles;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Contacts> contacts;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private CreditAccount creditAccount;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return the user's authorities (roles/permissions).  This is crucial.
        // Example using a simple role:
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")); // Or get from your database.
        // Or, if you have multiple roles:
        // List<GrantedAuthority> authorities = new ArrayList<>();
        // authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        // if (isAdmin) { authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN")); }
        // return authorities;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getUsername() {
        return username; // Return the user's username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Or implement logic based on your user model
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Or implement logic based on your user model
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Or implement logic based on your user model
    }

    @Override
    public boolean isEnabled() {
        return true; // Or implement logic based on your user model
    }


    public Long getId() {
        return user_id;
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

    public String getRoles() {
        return roles;
    }
    public void setRoles(String roles) {
        this.roles = roles;
    }
    public CreditAccount getCreditAccount() {
        return creditAccount;
    }

    public void setCreditAccount(CreditAccount creditAccount) {
        this.creditAccount = creditAccount;
        creditAccount.setUser(this);
    }

}
