package com.example.zandobackend.model.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VariantSize {
    private Integer id;
    private String uuid;
    private Integer variantId;
    private Integer sizeId;
    private boolean isAvailable;
}