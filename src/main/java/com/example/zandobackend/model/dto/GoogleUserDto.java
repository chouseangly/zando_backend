package com.example.zandobackend.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GoogleUserDto {
    private String email;
    private String firstName;
    private String lastName;
    private String picture;
}
