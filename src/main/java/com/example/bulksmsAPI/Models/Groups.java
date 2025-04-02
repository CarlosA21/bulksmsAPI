package com.example.bulksmsAPI.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "groups")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Groups {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long group_id;

    private String group_name;

    private String phoneNumbers;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


}
