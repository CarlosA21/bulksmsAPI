package com.example.bulksmsAPI.Controller;

// ... other imports

import com.example.bulksmsAPI.Models.DTO.AuthResponse;
import com.example.bulksmsAPI.Models.DTO.UserDTO;
import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Security.JwtUtil;
import com.example.bulksmsAPI.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO registrationRequest) {
        User user = userService.registerUser(registrationRequest.getEmail(), registrationRequest.getPassword());
        String token = jwtUtil.generateToken(user); // Generate token *after* registration
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO loginRequest) {
        User user = userService.authUser(loginRequest.getEmail(), loginRequest.getPassword());
        String token = jwtUtil.generateToken(user); // Generate token *after* authentication
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        userService.logout();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

}
