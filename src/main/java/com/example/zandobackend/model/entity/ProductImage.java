package com.example.zandobackend.model.entity;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class ProductImage {
    private Integer id;
    private UUID uuid = UUID.randomUUID();
    private String imageUrl;
    private Integer variantId; // Foreign Key
}