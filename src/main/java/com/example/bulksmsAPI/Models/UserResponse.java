package com.example.bulksmsAPI.Models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;


@Getter
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;

    private String driverLicense;


}
