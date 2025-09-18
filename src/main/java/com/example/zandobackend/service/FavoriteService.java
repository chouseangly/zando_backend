package com.example.zandobackend.service;

import com.example.zandobackend.model.dto.FavoriteRequest;
import com.example.zandobackend.model.dto.FavoriteResponse;

import java.util.List;

public interface FavoriteService {
    void addFavorite(FavoriteRequest favoriteRequest);
    void removeFavorite(Long userId, Long productId);
    List<FavoriteResponse> getFavoritesByUserId(Long userId);
    boolean isFavorite(Long userId, Long productId);
}