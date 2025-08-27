package com.example.zandobackend.model.dto;


import com.example.zandobackend.model.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Favourite {
    private Long favouriteId;
    private Long userId; // Changed from Integer
    private Long productId; // Changed from Integer
    private Product product;
    private LocalDateTime createdAt;
}