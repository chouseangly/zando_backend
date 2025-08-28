package com.example.zandobackend.service.impl;// chouseangly/deployment/deployment-main/ResellKH/ResellKH/src/main/java/com/example/resellkh/service/Impl/AuthServiceImpl.java


import com.example.zandobackend.jwt.JwtService;
import com.example.zandobackend.model.dto.AuthResponse;
import com.example.zandobackend.model.dto.GoogleUserDto;
import com.example.zandobackend.model.entity.Auth;
import com.example.zandobackend.model.entity.UserProfile;
import com.example.zandobackend.repository.AuthRepo;
import com.example.zandobackend.repository.UserProfileRepo;
import com.example.zandobackend.service.AuthService;
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

    @Override
    public Auth findByEmail(String email) {
        return authRepo.findByEmail(email);
    }

    @Override
    @Transactional
    public void registerUser(Auth auth) {
        authRepo.insertUser(auth);
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
        if (existing.isPresent()) {
            auth = existing.get();
            UserProfile existingProfile = userProfileRepo.getProfileByUserId(auth.getUserId());
            if (existingProfile == null) {
                UserProfile profile = new UserProfile();
                profile.setUserId(auth.getUserId()); // ✅ FIX: No casting needed
                profile.setFirstName(auth.getFirstName());
                profile.setLastName(auth.getLastName());
                profile.setUserName(auth.getUserName());
                userProfileRepo.createUserProfileAfterVerify(profile);
            }

        } else {
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
            profile.setUserId(auth.getUserId()); // ✅ FIX: No casting needed
            profile.setFirstName(auth.getFirstName());
            profile.setLastName(auth.getLastName());
            profile.setUserName(auth.getUserName());
            profile.setProfileImage(googleUserDto.getPicture());

            userProfileRepo.createUserProfileAfterVerify(profile);
        }

        String token = jwtService.generateToken(auth);
        UserProfile profile = userProfileRepo.getProfileByUserId(auth.getUserId()); // ✅ FIX: No casting needed
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