package com.example.zandobackend.service;



import com.example.zandobackend.model.dto.AuthResponse;
import com.example.zandobackend.model.dto.GoogleUserDto;
import com.example.zandobackend.model.entity.Auth;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface AuthService extends UserDetailsService {
    Auth findByEmail(String email);
    void registerUser(Auth auth);

    UserDetails loadUserByUsername(String email);

    void enableUser(String email);
    Auth resetPassword(String email, String password);

    AuthResponse registerWithGoogle(GoogleUserDto googleUserDto);

    List<Auth> getAllUser( );

}

