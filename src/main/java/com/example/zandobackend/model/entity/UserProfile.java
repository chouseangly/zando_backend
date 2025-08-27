package com.example.zandobackend.model.entity;

import lombok.*;

import java.time.LocalDate;
@AllArgsConstructor
@NoArgsConstructor

@Data
@Builder
public class UserProfile {
    private Long profileId;
    private Long userId;
    private String gender;
    private String phoneNumber;
    private String profileImage;
    private LocalDate birthday;
    private String address;
    private String userName;
    private String firstName;
    private String lastName;
}