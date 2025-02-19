package com.example.bulksmsAPI.Security;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.bulksmsAPI.Models.User;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    private final String SECRET_KEY = "secreto"; // Usa una clave segura

    public String generarToken(User usuario) {
        return JWT.create()
                .withSubject(usuario.getEmail())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 86400000)) // 1 día
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }

    public String obtenerUsuarioDesdeToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(SECRET_KEY))
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

}
