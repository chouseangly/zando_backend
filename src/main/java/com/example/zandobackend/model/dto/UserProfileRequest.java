package com.example.zandobackend.model.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileRequest {
    private Long userId;
    private String gender;
    private String phoneNumber;
    @Schema(required = false)
    private String profileImage;
    @Schema(required = false)

    private LocalDate birthday;
    private String address;    // fixed lowercase 'a'
    private String userName;
    private String firstName;
    private String lastName;
}
