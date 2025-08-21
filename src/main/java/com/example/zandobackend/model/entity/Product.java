package com.example.zandobackend.model.entity;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
public class Product {
    private Integer productId;
    private String uuid;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private Integer discountPercent;
    private BigDecimal finalPrice; // This is a generated column in the DB
    private OffsetDateTime createdAt;
    private List<ProductVariant> variants; // To hold the product's variants
}