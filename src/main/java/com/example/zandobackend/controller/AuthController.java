package com.example.zandobackend.controller;

import com.example.zandobackend.jwt.JwtService;
import com.example.zandobackend.model.dto.*;
import com.example.zandobackend.model.entity.Auth;
import com.example.zandobackend.model.entity.Notification;
import com.example.zandobackend.model.entity.UserProfile;
import com.example.zandobackend.service.AuthService;
import com.example.zandobackend.service.NotificationService;
import com.example.zandobackend.service.OtpService;
import com.example.zandobackend.service.UserProfileService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/auths")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileService userProfileService;
    private final NotificationService notificationService; // ✅ Injected NotificationService

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest authRequest) {
        Auth auth = new Auth();
        auth.setFirstName(authRequest.getFirstName());
        auth.setLastName(authRequest.getLastName());
        auth.setEmail(authRequest.getEmail());
        auth.setUserName(authRequest.getFirstName() + " " + authRequest.getLastName());
        auth.setPassword(passwordEncoder.encode(authRequest.getPassword()));
        auth.setRole("USER");
        auth.setCreatedAt(LocalDateTime.now());

        authService.registerUser(auth);
        otpService.sendOtp(auth.getEmail());

        return ResponseEntity.ok("User registered successfully. OTP sent.");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpRequest otpRequest) {
        boolean isValid = otpService.verifyOtp(otpRequest);
        if (isValid) {
            authService.enableUser(otpRequest.getEmail());

            Auth auth = authService.findByEmail(otpRequest.getEmail());
            Long userId = auth.getUserId();

            if (!userProfileService.existsByUserId(userId)) {
                UserProfile profile = UserProfile.builder()
                        .userId(userId)
                        .firstName(auth.getFirstName())
                        .lastName(auth.getLastName())
                        .userName(auth.getUserName())
                        .build();
                userProfileService.createUserProfileAfterVerify(profile);
            }
            return ResponseEntity.ok("OTP verified. User activated.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP");
        }
    }

    @GetMapping("/finduserbyemail")
    public ResponseEntity<Auth> findUserByEmail(@RequestParam String email) {
        Auth auth = authService.findByEmail(email);
        return ResponseEntity.ok(auth);
    }


    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(@RequestBody OtpRequest otpRequest) {
        otpService.sendOtp(otpRequest.getEmail());
        return ResponseEntity.ok(Map.of("message", "OTP resent successfully."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest login, HttpServletResponse response) {
        Auth auth = authService.findByEmail(login.getEmail());

        if (auth == null || !passwordEncoder.matches(login.getPassword(), auth.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }

        if (!auth.isEnabled()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Account not verified"));
        }

        // ✅ Create and send login notification
        Notification notification = Notification.builder()
                .userId(auth.getUserId())
                .title("Successful Login")
                .content("Your account was just accessed. If this wasn't you, please secure your account.")
                .iconUrl("https://gateway.pinata.cloud/ipfs/QmdMXVZ9KCiNGMwFHxkPMfpUfeGL8QQpMoENKeR5NKJ51F") // Placeholder icon
                .build();
        notificationService.createNotificationWithType(notification);

        String token = jwtService.generateToken(auth);
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) TimeUnit.HOURS.toSeconds(5));
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "role", auth.getRole(),
                "userId", auth.getUserId(),
                "firstName", auth.getFirstName(),
                "lastName", auth.getLastName(),
                "userName", auth.getUserName(),
                "email", auth.getEmail(),
                "message", "Login successful"
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        otpService.sendOtp(email);
        return ResponseEntity.ok("OTP sent to your email");
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<String> verifyResetOtp(@RequestBody OtpRequest otpRequest) {
        boolean isValid = otpService.verifyOtp(otpRequest);

        if (isValid) {
            return ResponseEntity.ok("OTP verified");
        } else {
            return ResponseEntity.ok("Invalid or expired OTP");
        }
    }

    @PutMapping("/reset-new-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetRequest) {
        if (!resetRequest.getNewPassword().equals(resetRequest.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Passwords do not match");
        }

        Auth user = authService.findByEmail(resetRequest.getEmail());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(
                            "User not found",
                            null,
                            HttpStatus.NOT_FOUND.value(),
                            LocalDateTime.now()
                    )
            );
        }

        String encodedPassword = passwordEncoder.encode(resetRequest.getNewPassword());
        Auth updated = authService.resetPassword(resetRequest.getEmail(), encodedPassword);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Password reset successfully",
                        updated,
                        HttpStatus.OK.value(),
                        LocalDateTime.now()
                )
        );
    }

    @PostMapping("/google")
    public ResponseEntity<Map<String, Object>> googleLogin(@RequestBody GoogleUserDto googleUserDto) {
        AuthResponse authResponse = authService.registerWithGoogle(googleUserDto);
        Map<String, Object> response = new HashMap<>();
        response.put("payload", authResponse);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<Auth>>> getAllUser() {
        List<Auth> users = authService.getAllUser();
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(
                "Get all users successfully",
                users,
                HttpStatus.OK.value(),
                LocalDateTime.now()
        ));
    }
}