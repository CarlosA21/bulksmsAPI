package com.example.bulksmsAPI.Controller;


import com.example.bulksmsAPI.Models.DTO.UserDTO;
import com.example.bulksmsAPI.Repositories.UserRepository;
import com.example.bulksmsAPI.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
//eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOm51bGwsImlhdCI6MTczOTk4MTI3MSwiZXhwIjoxNzQwMDY3NjcxfQ.DliPbYW_XxM8j1cE6pnOIxAtLDPHuKrJ5LuigHi9jWQ
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public Map<String, String> registrar(@RequestBody UserDTO request) {
        String token = userService.registerUser(request.getEmail(), request.getPassword());
        return Map.of("token", token);
    }


    @PostMapping("/login")
    public Map<String, String> login(@RequestBody UserDTO request) {
        String token = userService.authUser(request.getEmail(), request.getPassword());
        return Map.of("token", token);
    }

}

