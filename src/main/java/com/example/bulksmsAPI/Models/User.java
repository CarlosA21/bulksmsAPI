package com.example.bulksmsAPI.Models;


import com.example.bulksmsAPI.Security.EncryptionUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "User")

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter

public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long user_id;
    private String username;
    private String email;
    private String password;
    private String roles;

    @Column(name = "legal_id_type")
    @Enumerated(EnumType.STRING)
    private LegalIdType legalIdType;

    @Column(name = "legal_id_number")
    private String legalIdNumber;


    @Column(name = "validation_image_path")
    private String validationImagePath;

    @Column(name = "validation_image_name")
    private String validationImageName;

    @Column(name = "account_validated")
    private ValidationStatus accountValidated ;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Contacts> contacts;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private CreditAccount creditAccount;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private BillingAddress billingAddress;

    private String secretKey; // Agregar este campo



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Use the actual roles field from the database
        if (roles != null && !roles.isEmpty()) {
            // If the role already has ROLE_ prefix, use it as is, otherwise add the prefix
            String roleWithPrefix = roles.startsWith("ROLE_") ? roles : "ROLE_" + roles;
            return Collections.singleton(new SimpleGrantedAuthority(roleWithPrefix));
        }
        // Default fallback if no role is set
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
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
