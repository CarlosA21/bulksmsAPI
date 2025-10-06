package com.example.bulksmsAPI.Models.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class profileDTO {

    private String username;
    private String email;
    private String password;
    private String roles;

}
