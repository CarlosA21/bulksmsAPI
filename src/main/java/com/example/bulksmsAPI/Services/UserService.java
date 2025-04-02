package com.example.bulksmsAPI.Services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.bulksmsAPI.Models.CreditAccount;
import com.example.bulksmsAPI.Models.DTO.AuthResponse;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Models.BillingAddress;
import com.example.bulksmsAPI.Repositories.CreditAccountRepository;
import com.example.bulksmsAPI.Repositories.UserRepository;
import com.example.bulksmsAPI.Security.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;



@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreditAccountRepository creditAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String CLIENT_ID = "344365743547-965vrlju1l75ot5agnbv6g22l3c71isa.apps.googleusercontent.com";

    public User registerUser(String email, String password, String driverLicense, BillingAddress billingAddress) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already in use");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles("USER");
        user.setDriverLicense(driverLicense);
        user.setBillingAddress(billingAddress);
        billingAddress.setUser(user);

        User savedUser = userRepository.save(user);

        CreditAccount creditAccount = new CreditAccount();
        creditAccount.setUser(savedUser);
        creditAccount.setBalance(0);
        creditAccountRepository.save(creditAccount);

        return savedUser;
    }

    public User authUser(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception ex) {
            throw new RuntimeException("Incorrect credentials");
        }
    }

    public AuthResponse googleAuthUser(String idTokenString) {
        try {
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), jsonFactory)
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();

                Optional<User> optionalUser = userRepository.findByEmail(email);
                User user;
                if (optionalUser.isPresent()) {
                    user = optionalUser.get();
                } else {
                    user = new User();
                    user.setEmail(email);
                    user.setPassword(passwordEncoder.encode("google-auth"));
                    user.setRoles("USER");
                    user.setDriverLicense("");
                    User savedUser = userRepository.save(user);

                    CreditAccount creditAccount = new CreditAccount();
                    creditAccount.setUser(savedUser);
                    creditAccount.setBalance(0);
                    creditAccountRepository.save(creditAccount);

                    user = savedUser;
                }
                String token = jwtUtil.generateToken(user);
                return new AuthResponse(token, user.getEmail(), String.valueOf(user.getId()));
            } else {
                throw new RuntimeException("Invalid Google ID Token");
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Google Authentication failed", e);
        }
    }

    public void logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }

    public void changePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}