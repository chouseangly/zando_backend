package com.example.zandobackend.service.impl;

import com.example.zandobackend.jwt.JwtService;
import com.example.zandobackend.model.dto.AuthResponse;
import com.example.zandobackend.model.dto.GoogleUserDto;
import com.example.zandobackend.model.entity.Auth;
import com.example.zandobackend.model.entity.Notification;
import com.example.zandobackend.model.entity.UserProfile;
import com.example.zandobackend.repository.AuthRepo;
import com.example.zandobackend.repository.UserProfileRepo;
import com.example.zandobackend.service.AuthService;
import com.example.zandobackend.service.NotificationService; // ✅ Import NotificationService
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthRepo authRepo;
    private final UserProfileRepo userProfileRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationService notificationService; // ✅ Inject NotificationService

    // ✅ Private helper method to create a welcome notification
    private void sendWelcomeNotification(Auth user) {
        Notification notification = Notification.builder()
                .userId(user.getUserId())
                .title("Welcome to Zando")
                .content("We’re excited to have you join our community. Explore great deals and connect with others!")
                .iconUrl("https://gateway.pinata.cloud/ipfs/QmdMXVZ9KCiNGMwFHxkPMfpUfeGL8QQpMoENKeR5NKJ51F")
                .build();
        notificationService.createNotificationWithType(notification);
    }

    @Override
    public Auth findByEmail(String email) {
        return authRepo.findByEmail(email);
    }

    @Override
    @Transactional
    public void registerUser(Auth auth) {
        authRepo.insertUser(auth);
        Auth savedUser = authRepo.findByEmail(auth.getEmail());
        UserProfile userProfile = UserProfile.builder()
                .userId(savedUser.getUserId())
                .userName(savedUser.getUserName())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .build();
        userProfileRepo.createUserProfileAfterVerify(userProfile);

        // ✅ Send welcome notification for new email registrations
        sendWelcomeNotification(savedUser);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Auth auth = authRepo.findByEmail(email);
        if (auth == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return auth;
    }

    @Override
    public void enableUser(String email) {
        authRepo.enableUserByEmail(email);
    }

    @Override
    @Transactional
    public Auth resetPassword(String email, String password) {
        authRepo.updatePasswordByEmail(email, password);
        return authRepo.findByEmail(email);
    }

    @Override
    @Transactional
    public AuthResponse registerWithGoogle(GoogleUserDto googleUserDto) {
        Optional<Auth> existing = authRepo.findByEmailOptional(googleUserDto.getEmail());

        Auth auth;
        boolean isNewUser = false; // Flag to track if the user is new

        if (existing.isPresent()) {
            auth = existing.get();
            UserProfile existingProfile = userProfileRepo.getProfileByUserId(auth.getUserId());
            if (existingProfile == null) {
                UserProfile profile = new UserProfile();
                profile.setUserId(auth.getUserId());
                profile.setFirstName(auth.getFirstName());
                profile.setLastName(auth.getLastName());
                profile.setUserName(auth.getUserName());
                userProfileRepo.createUserProfileAfterVerify(profile);
            }
        } else {
            isNewUser = true; // Mark as new user
            auth = new Auth();
            auth.setFirstName(googleUserDto.getFirstName());
            auth.setLastName(googleUserDto.getLastName());
            auth.setUserName(googleUserDto.getFirstName() + googleUserDto.getLastName());
            auth.setEmail(googleUserDto.getEmail());
            auth.setPassword(passwordEncoder.encode("google_oauth_dummy_password"));
            auth.setRole("USER");
            auth.setCreatedAt(LocalDateTime.now());

            authRepo.insertUser(auth);
            auth = authRepo.findByEmail(googleUserDto.getEmail());

            UserProfile profile = new UserProfile();
            profile.setUserId(auth.getUserId());
            profile.setFirstName(auth.getFirstName());
            profile.setLastName(auth.getLastName());
            profile.setUserName(auth.getUserName());
            profile.setProfileImage(googleUserDto.getPicture());

            userProfileRepo.createUserProfileAfterVerify(profile);

            // ✅ Send welcome notification ONLY for new Google sign-ups
            sendWelcomeNotification(auth);
        }

        String token = jwtService.generateToken(auth);
        UserProfile profile = userProfileRepo.getProfileByUserId(auth.getUserId());
        String profileImage = (profile != null && profile.getProfileImage() != null)
                ? profile.getProfileImage()
                : googleUserDto.getPicture();

        return new AuthResponse(
                token,
                auth.getRole(),
                auth.getUserId(),
                auth.getEmail(),
                auth.getFirstName(),
                auth.getLastName(),
                profileImage
        );
    }

    @Override
    public List<Auth> getAllUser() {
        return authRepo.getAllUser();
    }
}