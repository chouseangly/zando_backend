package com.example.zandobackend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {
    private Long favoriteId;
    private Long userId;
    private Long productId;
    private LocalDateTime createdAt;
    private Product product;
}