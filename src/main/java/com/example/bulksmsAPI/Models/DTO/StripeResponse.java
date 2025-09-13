package com.example.bulksmsAPI.Models.DTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StripeResponse {
    private String status;
    private String message;
    private String sessionId;
    private String sessionUrl;
    private String token;
}
