package com.example.zandobackend.model.entity;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ProductVariant {
    private Integer id;
    private UUID uuid = UUID.randomUUID();
    private String color;
    private Integer productId; // Foreign Key
    private List<ProductImage> images = new ArrayList<>();
    private List<VariantSize> variantSizes = new ArrayList<>();
}