package com.example.zandobackend.model.entity;

import lombok.Data;
import java.util.List;

@Data
public class Product {
    private Long productId;
    private String uuid;
    private String name;
    private String description;
    private Double basePrice; // original price
    private Integer discountPercent;
    private List<ProductVariant> variants; // includes color, images, sizes
    private List<String> allSizes; // static or fetched from DB: ["S", "M", "L", "XL", "XXL"]
}

