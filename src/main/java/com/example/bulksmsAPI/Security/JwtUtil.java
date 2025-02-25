package com.example.bulksmsAPI.Security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.bulksmsAPI.Models.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    public String generateToken(User user) {  // Accepts the User object
        return JWT.create()
                .withSubject(user.getEmail())  // Use user's email
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 86400000))
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }


    public String getEmailFromToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(SECRET_KEY))
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException e) {
            return null; // Or handle the exception as needed
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String email = getEmailFromToken(token);
        return email != null && email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        try {
            Date expirationDate = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                    .build()
                    .verify(token)
                    .getExpiresAt();
            return expirationDate.before(new Date());
        } catch (JWTVerificationException e) {
            return true; // Treat invalid tokens as expired
        }
    }
}