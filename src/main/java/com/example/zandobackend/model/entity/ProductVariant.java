package com.example.zandobackend.model.entity;

import lombok.Data;
import java.util.List;

@Data
public class ProductVariant {
    private Long variantId;
    private Long productId;
    private String uuid;
    private String color;
    private int quantity;
    private List<String> images;
    private List<Size> sizes; // Changed from List<String>
}