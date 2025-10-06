package com.example.bulksmsAPI.Models.DTO;

import com.example.bulksmsAPI.Models.LegalIdType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingUserValidationDTO {
    private Long userId;
    private String email;
    private String driverLicense; // Deprecated
    private LegalIdType legalIdType;
    private String legalIdNumber;
    private String validationImageName;
    private String validationImageBase64; // Imagen en Base64 para facilitar el uso en Angular
    private BillingAddressDTO billingAddress;
    private String registrationDate;
}
