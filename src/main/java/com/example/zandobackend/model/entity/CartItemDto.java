package com.example.zandobackend.model.entity;

import lombok.Data;

@Data
public class CartItemDto {
    private Long cartItemId;
    private Long cartId;
    private Long productId;
    private Long variantId;
    private Long sizeId;
    private int quantity;
    private Product product;
}