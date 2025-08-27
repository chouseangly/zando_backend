package com.example.zandobackend.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FavouriteRequest {
    private Long userId; // Changed from Integer
    private Long productId; // Changed from Integer
    private LocalDateTime createdAt;
}