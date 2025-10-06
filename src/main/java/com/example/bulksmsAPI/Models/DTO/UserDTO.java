package com.example.bulksmsAPI.Models.DTO;


import com.example.bulksmsAPI.Models.BillingAddress;
import com.example.bulksmsAPI.Models.Contacts;
import com.example.bulksmsAPI.Models.LegalIdType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String email;
    private String password;

    private String driverLicense; // Deprecated - usar legalIdNumber
    private LegalIdType legalIdType;
    private String legalIdNumber;

    private List<Contacts> contacts;

    private int Credits;
    private String roles;
    private BillingAddress billingAddress;
    private Integer twoFactorCode;
    private Date dob;
}
