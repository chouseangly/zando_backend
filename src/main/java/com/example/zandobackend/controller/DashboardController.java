package com.example.zandobackend.controller;

import com.example.zandobackend.model.dto.ApiResponse;
import com.example.zandobackend.model.dto.DashboardDto;
import com.example.zandobackend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardDto>> getDashboardStats() {
        DashboardDto stats = dashboardService.getDashboardStats();
        ApiResponse<DashboardDto> response = new ApiResponse<>(
                "Dashboard statistics retrieved successfully",
                stats,
                HttpStatus.OK.value(),
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }
}