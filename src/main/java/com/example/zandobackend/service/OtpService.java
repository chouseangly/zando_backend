package com.example.zandobackend.service;


import com.example.zandobackend.model.dto.OtpRequest;

public interface OtpService {
    void sendOtp(String email);
    boolean verifyOtp(OtpRequest otpRequest);
}