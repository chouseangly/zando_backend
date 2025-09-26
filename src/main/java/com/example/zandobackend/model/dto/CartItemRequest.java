package com.example.zandobackend.model.dto;

import lombok.Data;

@Data
public class CartItemRequest {
    private Long userId;
    private Long productId;
    private Long variantId;
    private Long sizeId;
    private int quantity;
}