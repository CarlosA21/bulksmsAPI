package com.example.bulksmsAPI.Controller;

// ... other imports

import com.example.bulksmsAPI.Models.BillingAddress;
import com.example.bulksmsAPI.Models.DTO.AuthResponse;
import com.example.bulksmsAPI.Models.DTO.BillingAddressDTO;
import com.example.bulksmsAPI.Models.DTO.UserDTO;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Security.JwtUtil;
import com.example.bulksmsAPI.Services.BillingAddressService;
import com.example.bulksmsAPI.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private BillingAddressService billingAddressService;

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
        return ResponseEntity.ok(new AuthResponse(token, username, userID));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO loginRequest) {
        User user = userService.authUser(loginRequest.getEmail(), loginRequest.getPassword());
        String token = jwtUtil.generateToken(user); // Generate token *after* authentication
        String userId = String.valueOf(user.getId());
        String username = user.getUsername();
        System.out.println("User ID: " + userId);

        return ResponseEntity.ok(new AuthResponse(token, username, userId));
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
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody UserDTO passwordRequest) {
        userService.changePassword(passwordRequest.getEmail(), passwordRequest.getPassword());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }
    @GetMapping("/find-user")
    public ResponseEntity<?> findUser(@RequestParam Long id) {
        Optional<BillingAddressDTO> billingAddress = billingAddressService.listBillingAddress(id);
        return ResponseEntity.ok(billingAddress);
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        userService.logout();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

}
