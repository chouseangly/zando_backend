package com.example.zandobackend.service;

import com.example.zandobackend.model.dto.FavoriteRequest;
import com.example.zandobackend.model.dto.FavoriteResponse;

import java.util.List;

public interface FavoriteService {
    // MODIFIED: This method will now return the created favorite response
    FavoriteResponse addFavorite(FavoriteRequest favoriteRequest);
    void removeFavorite(Long userId, Long productId);
    List<FavoriteResponse> getFavoritesByUserId(Long userId);
    boolean isFavorite(Long userId, Long productId);
}