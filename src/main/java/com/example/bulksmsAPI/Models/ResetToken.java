package com.example.bulksmsAPI.Models;


import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;

    private LocalDateTime expiryDate;
}
