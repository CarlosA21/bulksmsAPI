package com.example.bulksmsAPI.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.example.bulksmsAPI.Repositories.UserRepository; // Your user repository
import com.example.bulksmsAPI.Models.User; // Your user model

@Configuration
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository; // Inject your UserRepository

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> { // Use email as the username
            User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if (user == null) {
                throw new UsernameNotFoundException("User not found");
            }
            return new org.springframework.security.core.userdetails.User( // Create UserDetails object
                    user.getEmail(), // Username (email)
                    user.getPassword(),
                    AuthorityUtils.NO_AUTHORITIES // Or get authorities from user.getRoles() if you have roles
            );
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Example
                        .anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }
}