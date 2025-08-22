package com.example.zandobackend.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductCreateRequest {
    private String name;
    private String description;
    private BigDecimal originalPrice;
    private Integer discount;
    private List<String> allSizes;
    private List<VariantCreateRequest> variants;
}