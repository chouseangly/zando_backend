package com.example.zandobackend.model.entity;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class VariantSize {
    private Integer id;
    private UUID uuid = UUID.randomUUID();
    private Integer variantId;
    private Integer sizeId;
    private boolean isAvailable = true;
    private Size size;
}