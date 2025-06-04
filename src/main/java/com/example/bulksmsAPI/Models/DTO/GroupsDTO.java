package com.example.bulksmsAPI.Models.DTO;

import com.example.bulksmsAPI.Models.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@Setter

public class GroupsDTO {
    private Long group_id;

    private String group_name;

    private UserDTO usuario;


}
