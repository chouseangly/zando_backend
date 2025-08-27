package com.example.zandobackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String role;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String profileImage;
}