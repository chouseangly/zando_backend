package com.example.zandobackend.model.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductImage {
    private Integer imageId;
    private String uuid;
    private Integer variantId;
    private String imageUrl;
}