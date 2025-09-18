package com.example.zandobackend.model.dto;

import com.example.zandobackend.model.entity.Product;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FavoriteResponse {
    private Long favoriteId;
    private Long userId;
    private Long productId;
    private ProductResponse product;
    private LocalDateTime createdAt;// Embed the detailed product response
}