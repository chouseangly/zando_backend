package com.example.zandobackend.controller;

import com.example.zandobackend.model.dto.ApiResponse;
import com.example.zandobackend.model.dto.FavoriteRequest;
import com.example.zandobackend.model.dto.FavoriteResponse;
import com.example.zandobackend.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<FavoriteResponse>> addFavorite(@RequestBody FavoriteRequest favoriteRequest) {
        try {
            // ✅ MODIFIED: The service now returns the created favorite object
            FavoriteResponse newFavorite = favoriteService.addFavorite(favoriteRequest);
            ApiResponse<FavoriteResponse> response = new ApiResponse<>(
                    "Product added to favorites successfully.",
                    newFavorite, // Set the returned object as the payload
                    HttpStatus.CREATED.value(),
                    LocalDateTime.now()
            );
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            ApiResponse<FavoriteResponse> response = new ApiResponse<>(
                    e.getMessage(),
                    null,
                    HttpStatus.BAD_REQUEST.value(),
                    LocalDateTime.now()
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> removeFavorite(@RequestParam Long userId, @PathVariable Long productId) {
        try {
            favoriteService.removeFavorite(userId, productId);
            ApiResponse<String> response = new ApiResponse<>(
                    "Product removed from favorites successfully.",
                    null,
                    HttpStatus.OK.value(),
                    LocalDateTime.now()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            ApiResponse<String> response = new ApiResponse<>(
                    e.getMessage(),
                    null,
                    HttpStatus.BAD_REQUEST.value(),
                    LocalDateTime.now()
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getFavorites(@PathVariable Long userId) {
        List<FavoriteResponse> favorites = favoriteService.getFavoritesByUserId(userId);
        ApiResponse<List<FavoriteResponse>> response = new ApiResponse<>(
                "Favorites retrieved successfully.",
                favorites,
                HttpStatus.OK.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}