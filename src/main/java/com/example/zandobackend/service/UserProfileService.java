package com.example.zandobackend.service;


import com.example.zandobackend.model.entity.UserProfile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface UserProfileService {
    UserProfile getProfile(Long userId);
    String deleteProfile(Long userId);
    UserProfile createUserProfile(Long userId, String gender, String phoneNumber, LocalDate birthday, String address, MultipartFile profileImage, MultipartFile coverImage) throws IOException;UserProfile updateUserProfileWithImage(Long userId, String gender, String phoneNumber, LocalDate birthday, String address, String telegramUrl, String slogan, String userName, MultipartFile profileImage) throws IOException;
    List<UserProfile> getUserProfiles();
    void createUserProfileAfterVerify(UserProfile userProfile);
    boolean existsByUserId(Long userId);


}
