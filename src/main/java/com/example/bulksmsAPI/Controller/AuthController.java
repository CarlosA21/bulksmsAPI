package com.example.bulksmsAPI.Controller;

// ... other imports

import com.example.bulksmsAPI.Models.BillingAddress;
import com.example.bulksmsAPI.Models.DTO.*;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Models.UserResponse;
import com.example.bulksmsAPI.Models.ValidationStatus;
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
        try {
            BillingAddress billingAddress = registrationRequest.getBillingAddress();
            User user = userService.registerUser(
                    registrationRequest.getEmail(),
                    registrationRequest.getPassword(),
                    registrationRequest.getDriverLicense(), // Mantener compatibilidad
                    billingAddress,
                    registrationRequest.getLegalIdType(),
                    registrationRequest.getLegalIdNumber()
            );
            String token = jwtUtil.generateToken(user);
            String username = user.getUsername();
            String userID = String.valueOf(user.getId());
            ValidationStatus accountValidated = ValidationStatus.PENDING;

            String Role = String.valueOf(user.getRoles());
            return ResponseEntity.ok(new AuthResponse(token, userID, Role, accountValidated,  user.getSecretKey()));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
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
                    String.valueOf(user.getId()),
                    user.getRoles(),
                    user.getAccountValidated(),
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
            String idToken = Optional.ofNullable(request.get("idToken"))
                    .or(() -> Optional.ofNullable(request.get("token")))
                    .or(() -> Optional.ofNullable(request.get("id_token")))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .orElse(null);

            if (idToken == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Missing idToken"));
            }

            // Truncated safe logging
            try {
                String trunc = idToken.length() > 30 ? idToken.substring(0, 30) + "..." : idToken;
                System.out.println("googleLogin - received idToken (truncated): " + trunc);
            } catch (Exception ignored) {}

            AuthResponse authResponse = userService.googleAuthUser(idToken);
            if (authResponse == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid Google ID Token"));
            }
            return ResponseEntity.ok(authResponse);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
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
            String resetLink = "https://theglobalmessaging.com/reset-password?token=" + resetToken;

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
                user.getLegalIdNumber(),
                user.getAccountValidated()
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
            UserResponseDTO userDTO = new UserResponseDTO(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/createuser")
    public ResponseEntity<?> createUser(@RequestBody profileDTO profileRequest) {
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Admin role required.");
        }
        try {
            User user = userService.createProfile(profileRequest);
            UserResponseDTO userDTO = new UserResponseDTO(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
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
            UserResponseDTO userDTO = new UserResponseDTO(updatedUser);
            return ResponseEntity.ok(userDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // List all users
    @GetMapping("/list")
    public ResponseEntity<List<UserResponseDTO>> listUsers() {
        List<User> users = userService.listUsers();
        List<UserResponseDTO> userDTOs = users.stream()
                .map(UserResponseDTO::new)
                .toList();
        return ResponseEntity.ok(userDTOs);
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

    // Upload validation image
    @PostMapping("/upload-validation-image/{userId}")
    public ResponseEntity<?> uploadValidationImage(
            @PathVariable Long userId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            User updatedUser = userService.uploadValidationImage(userId, file);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Validation image uploaded successfully");
            response.put("imageName", updatedUser.getValidationImageName());
            response.put("accountValidated", updatedUser.getAccountValidated());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Get validation image
    @GetMapping("/validation-image/{userId}")
    public ResponseEntity<byte[]> getValidationImage(@PathVariable Long userId) {
        try {
            byte[] imageBytes = userService.getValidationImage(userId);
            return ResponseEntity.ok()
                    .header("Content-Type", "image/jpeg")
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Validate user account (admin only)
    @PostMapping("/validate-account/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> validateUserAccount(
            @PathVariable Long userId) {
        try {
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Admin role required.");
            }
            User updatedUser = userService.validateUserAccount(userId);
            updatedUser.setAccountValidated(ValidationStatus.APPROVED);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User account validation status updated");
            response.put("userId", updatedUser.getId());
            response.put("accountValidated", updatedUser.getAccountValidated());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    // deny user account (admin only)
    @PostMapping("/deny-account/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> denyUserAccount(
            @PathVariable Long userId
            ) {
        try {
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Admin role required.");
            }
            User updatedUser = userService.denyUserAccount(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User account has been denied");
            response.put("userId", updatedUser.getId());
            response.put("accountValidated", updatedUser.getAccountValidated());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }


    // Get all pending validation users (admin only)
    @GetMapping("/pending-validations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingValidations() {
        try {
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Admin role required.");
            }
            List<com.example.bulksmsAPI.Models.DTO.PendingUserValidationDTO> pendingUsers = userService.getPendingValidationUsers();
            return ResponseEntity.ok(pendingUsers);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
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
