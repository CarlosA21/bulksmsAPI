package com.example.bulksmsAPI.Controller;

// ... other imports

import com.example.bulksmsAPI.Models.BillingAddress;
import com.example.bulksmsAPI.Models.DTO.AuthResponse;
import com.example.bulksmsAPI.Models.DTO.BillingAddressDTO;
import com.example.bulksmsAPI.Models.DTO.UserDTO;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Models.UserResponse;
import com.example.bulksmsAPI.Security.JwtUtil;
import com.example.bulksmsAPI.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private TwoFactorAuthService twoFactorAuthService;

    @Autowired
    private BillingAddressService billingAddressService;

    @Autowired
    private ResetTokenService resetTokenService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO registrationRequest) {
        BillingAddress billingAddress = registrationRequest.getBillingAddress();
        User user = userService.registerUser(
                registrationRequest.getEmail(),
                registrationRequest.getPassword(),
                registrationRequest.getDriverLicense(),
                billingAddress
        );
        String token = jwtUtil.generateToken(user);
        String username = user.getUsername();
        String userID = String.valueOf(user.getId());
        String Role = String.valueOf(user.getRoles());
        return ResponseEntity.ok(new AuthResponse(token, username, userID,  Role, user.getSecretKey()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO loginRequest) {
        try {
            // Autenticar al usuario
            User user = userService.authUser(loginRequest.getEmail(), loginRequest.getPassword());

            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
            }
            // Verificar si el usuario es ADMIN
            boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRoles());

            // Generar token JWT
            String token = jwtUtil.generateToken(user);

            // Crear la respuesta
            AuthResponse authResponse = new AuthResponse(
                    token,
                    user.getUsername(),
                    String.valueOf(user.getId()),
                    user.getRoles(),
                    user.getSecretKey()
            );

            // Mensaje personalizado si es ADMIN
            if (isAdmin) {
                authResponse.setMessage("Welcome, Admin! You have access to restricted endpoints.");
            } else {
                authResponse.setMessage("Welcome! You have limited access.");
            }
            return ResponseEntity.ok(authResponse);

        } catch (Exception ex) {
            // Manejo de excepciones
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + ex.getMessage());
        }
    }


    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request) {
        try {
            String idToken = request.get("idToken");
            if (idToken == null || idToken.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Missing idToken");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            AuthResponse authResponse = userService.googleAuthUser(idToken);


            return ResponseEntity.ok(authResponse);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/enable-2fa")
    public ResponseEntity<byte[]> enable2FA(@RequestParam String email) {
        byte[] qrCodeImage = userService.enable2FA(email, "SMS App");
        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(qrCodeImage);
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<Boolean> verify2FA(@RequestParam String email, @RequestParam int code) {
        boolean isVerified = userService.verify2FA(email, code);
        return ResponseEntity.ok(isVerified);
    }


    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestParam String email) {
        try {
            String resetToken = userService.createResetToken(email);
            String resetLink = "http://localhost:4200/reset-password?token=" + resetToken;

            // Usa el servicio para enviar el correo
            emailService.sendPasswordResetEmail(email, resetLink);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset link sent to email");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");

            // Validaciones básicas
            if (token == null || token.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Token is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            if (newPassword == null || newPassword.length() < 8) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Password must be at least 8 characters long");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Validar y usar el token
            userService.validateResetToken(token);
            userService.resetPassword(token, newPassword);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset successfully");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid or expired reset token");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    @GetMapping(value = "/getuserinfo/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> getUserInfo(@PathVariable Long userId) {
        User user = userService.getUserById(userId);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        UserResponse dto = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDriverLicense()
        );

        return ResponseEntity.ok(dto);
    }




    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        userService.logout();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/createadmin")
    public ResponseEntity<?> createUser(@RequestParam String email,
                                        @RequestParam String password,
                                        @RequestParam String roles) {
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Admin role required.");
        }
        try {
            User user = userService.createUser(email, password, roles);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    // Edit user details
    @PutMapping("/edit/{userId}")
    public ResponseEntity<?> editUser(@PathVariable Long userId,
                                      @RequestParam(required = false) String email,
                                      @RequestParam(required = false) String password,
                                      @RequestParam(required = false) String driverLicense,
                                      @RequestParam(required = false) String roles) {
        try {
            User updatedUser = userService.editUser(userId, email, password, driverLicense, roles);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // List all users
    @GetMapping("/list")
    public ResponseEntity<List<User>> listUsers() {
        List<User> users = userService.listUsers();
        return ResponseEntity.ok(users);
    }

    // Delete user by ID
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok("User deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            return auth.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        }
        return false;
    }

}
