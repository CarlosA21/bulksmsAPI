package com.example.bulksmsAPI.Services;

import com.example.bulksmsAPI.Models.User;
import com.example.bulksmsAPI.Repositories.UserRepository;
import com.example.bulksmsAPI.Security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service

public class UserService {
    @Autowired
    private UserRepository UserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;
    User User = new User();



    public String registerUser(String email, String password) {
        if (UserRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("El email ya está en uso");
        }
        User.setEmail(email);
        User.setPassword(passwordEncoder.encode(password));

        UserRepository.save(User);
        return jwtUtil.generarToken(User);
    }

    public String authUser(String email, String password) {
        Optional<User> User = UserRepository.findByEmail(email);

        if (User.isPresent() && passwordEncoder.matches(password, User.get().getPassword())) {
            return jwtUtil.generarToken(User.get());
        } else {
            throw new RuntimeException("Credenciales incorrectas");
        }
    }
}
