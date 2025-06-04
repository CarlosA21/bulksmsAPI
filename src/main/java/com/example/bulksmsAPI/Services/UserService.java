package com.example.bulksmsAPI.Services;

import com.example.bulksmsAPI.Models.CreditAccount;
import com.example.bulksmsAPI.Models.DTO.AuthResponse;
import com.example.bulksmsAPI.Models.ResetToken;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Models.BillingAddress;
import com.example.bulksmsAPI.Repositories.CreditAccountRepository;
import com.example.bulksmsAPI.Repositories.ResetTokenRepository;
import com.example.bulksmsAPI.Repositories.UserRepository;
import com.example.bulksmsAPI.Security.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
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
import java.time.LocalDateTime;
import java.util.*;


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
    private TwoFactorAuthService twoFactorAuthService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ResetTokenRepository resetTokenRepository;
    private final String FRONTEND_URL = "https://your-frontend-url.com/reset-password";



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
        billingAddress.setUser(user); // Pass User object instead of userId

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
                return new AuthResponse(token, user.getEmail(), String.valueOf(user.getId()), user.getRoles(), user.getSecretKey());
            } else {
                throw new RuntimeException("Invalid Google ID Token");
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Google Authentication failed", e);
        }
    }


    public byte[] enable2FA(String email, String appName) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        String secretKey = twoFactorAuthService.generateSecretKey();
        user.setSecretKey(secretKey);
        userRepository.save(user);
        return twoFactorAuthService.generateQRCode(secretKey, email, appName);
    }

    public boolean verify2FA(String email, int verificationCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return twoFactorAuthService.verifyCode(user.getSecretKey(), verificationCode);
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

    public String createResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String token = UUID.randomUUID().toString();
        ResetToken resetToken = new ResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        resetTokenRepository.save(resetToken);

        return token;
    }
    public void validateResetToken(String token) {
        ResetToken resetToken = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired");
        }
    }
    // Method to request password reset
    public void resetPassword(String token, String newPassword) {
        ResetToken resetToken = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete the token after successful password reset
        resetTokenRepository.delete(resetToken);
    }
    public void changePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public User createUser(String email, String password, String roles) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(roles);


        User savedUser = userRepository.save(user);

        CreditAccount creditAccount = new CreditAccount();
        creditAccount.setUser(savedUser);
        creditAccount.setBalance(0);
        creditAccountRepository.save(creditAccount);

        return savedUser;
    }

    // Edit user details
    public User editUser(Long userId, String email, String password, String driverLicense, String roles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (email != null && !email.isEmpty()) {
            user.setEmail(email);
        }
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        if (driverLicense != null && !driverLicense.isEmpty()) {
            user.setDriverLicense(driverLicense);
        }
        if (roles != null && !roles.isEmpty()) {
            user.setRoles(roles);
        }

        return userRepository.save(user);
    }

    // List all users
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    // Delete user by ID
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }



}