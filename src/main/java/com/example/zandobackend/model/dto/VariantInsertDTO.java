package com.example.zandobackend.model.dto;

import lombok.Data;

@Data
public class VariantInsertDTO {
    private Long variantId;
    private Long productId;
    private String color;
    private int quantity; // ✅ ADDED
}