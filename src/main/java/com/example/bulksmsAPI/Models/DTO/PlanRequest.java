package com.example.bulksmsAPI.Models.DTO;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanRequest {
    private String planCredit;

    private Long amount;

    private Long quantity;

    private String planName;

    private String Currency;
}
