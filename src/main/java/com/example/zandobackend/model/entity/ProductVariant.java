package com.example.zandobackend.model.entity;

import lombok.Data;
import java.util.List;

@Data
public class ProductVariant {
    private Long variantId; // <-- ADD THIS LINE
    private Long productId;
    private String uuid;
    private String color;
    private List<String> images; // image URLs
    private List<String> sizes; // available sizes for this color
}