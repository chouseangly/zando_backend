package com.example.zandobackend.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {
    private String name;
    private String description;
    private BigDecimal basePrice;
    private Integer discountPercent;
    private List<VariantRequest> variants;
}