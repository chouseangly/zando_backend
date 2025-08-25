package com.example.zandobackend.model.entity;

import lombok.Data;
import java.util.ArrayList; // <-- Make sure this import is added
import java.util.List;

@Data
public class Product {
    private Long productId;
    private String uuid;
    private String name;
    private String description;
    private Double basePrice;
    private Integer discountPercent;
    private List<ProductVariant> variants = new ArrayList<>();
}