package com.example.zandobackend.model.dto;

import lombok.Data;

@Data
public class FavoriteRequest {
    private Long userId;
    private Long productId;
}