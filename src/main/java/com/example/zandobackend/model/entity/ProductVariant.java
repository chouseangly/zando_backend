package com.example.zandobackend.model.entity;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ProductVariant {
    private Integer variantId;
    private String uuid;
    private Integer productId;
    private String color;
    private List<ProductImage> images; // A variant can have multiple images
    private List<VariantSize> availableSizes; // A variant can have multiple sizes
}