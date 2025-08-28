package com.example.zandobackend.controller;

import com.example.zandobackend.model.dto.ApiResponse;
import com.example.zandobackend.model.entity.Notification;
import com.example.zandobackend.model.entity.UserProfile;
import com.example.zandobackend.service.NotificationService;
import com.example.zandobackend.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;
    private final NotificationService notificationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfile>> createUserProfile(
            @RequestParam("userId") Long userId,
            @RequestParam("gender") String gender,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate birthday,
            @RequestParam("address") String address,
            @RequestPart("profileImage") MultipartFile profileImage
    ) {
        try {
            UserProfile created = profileService.createUserProfile(userId, gender, phoneNumber, birthday, address, profileImage);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("User profile created/updated successfully", created, HttpStatus.CREATED.value(), LocalDateTime.now()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to process image", null, HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now()));
        }
    }

    @PutMapping(path = "edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<UserProfile>> updateProfile(
            @RequestParam("userId") Long userId,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthday,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        try {
            UserProfile updated = profileService.updateUserProfileWithImage(
                    userId, gender, phoneNumber, birthday, address,
                    userName, firstName, lastName, profileImage);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = LocalDateTime.now().format(formatter);
            Notification notification = Notification.builder()
                    .userId(userId)
                    .title("Update Profile")
                    .content("You successfully update profile at "+ formattedDateTime)
                    .iconUrl("https://gateway.pinata.cloud/ipfs/QmdMXVZ9KCiNGMwFHxkPMfpUfeGL8QQpMoENKeR5NKJ51F")
                    .build();
            notificationService.createNotificationWithType(notification);
            return ResponseEntity.ok(new ApiResponse<>("User profile updated successfully", updated, HttpStatus.OK.value(), LocalDateTime.now()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to update image", null, HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserProfile>>> getUserProfiles() {
        List<UserProfile> userProfiles = profileService.getUserProfiles();
        if (userProfiles == null || userProfiles.isEmpty()) {
            return ResponseEntity.ok(
                    new ApiResponse<>("No user profiles found", Collections.emptyList(), HttpStatus.OK.value(), LocalDateTime.now())
            );
        }
        return ResponseEntity.ok(
                new ApiResponse<>("Get user profiles successfully", userProfiles, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfile>> getProfile(@PathVariable Long userId) {
        UserProfile userProfile = profileService.getProfile(userId);
        if (userProfile == null) {
            return ResponseEntity.ok(
                    new ApiResponse<>("User profile not found", null, HttpStatus.OK.value(), LocalDateTime.now())
            );
        }
        return ResponseEntity.ok(
                new ApiResponse<>("User profile fetched successfully", userProfile, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteProfile(@PathVariable Long userId) {
        String result = profileService.deleteProfile(userId);
        return ResponseEntity.ok(
                new ApiResponse<>("User profile deleted", result, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }
}