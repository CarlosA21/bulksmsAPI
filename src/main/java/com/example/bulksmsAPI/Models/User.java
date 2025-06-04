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

    @Column(name = "driver_license", nullable = false)
    private String driverLicense;
    private Date dob;

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
        // Return the user's authorities (roles/permissions).  This is crucial.
        // Example using a simple role:
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")); // Or get from your database.
        // Or, if you have multiple roles:
        // List<GrantedAuthority> authorities = new ArrayList<>();
        // authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        // if (isAdmin) { authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN")); }
        // return authorities;
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

    public String getDriverLicense() {
        try {
            return EncryptionUtil.decrypt(this.driverLicense);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting driver license", e);
        }
    }

    public void setDriverLicense(String driverLicense) {
        try {
            this.driverLicense = EncryptionUtil.encrypt(driverLicense);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting driver license", e);
        }
    }

    public BillingAddress getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(BillingAddress billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
