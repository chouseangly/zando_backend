package com.example.zandobackend.model.entity;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Product {
    private Integer id;
    private UUID uuid = UUID.randomUUID();
    private String name;
    private String description;
    private BigDecimal originalPrice;
    private Integer discount;
    private BigDecimal price; // This will be calculated by the database
    private OffsetDateTime createdAt = OffsetDateTime.now();
    private List<ProductVariant> variants = new ArrayList<>();
}