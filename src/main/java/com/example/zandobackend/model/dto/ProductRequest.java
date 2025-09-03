package com.example.zandobackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    private String name;
    private String description;
    private Double basePrice;
    private Integer discountPercent;
    private Boolean isAvailable; // Ensure this line is present
    private List<VariantRequest> variants;
    private List<String> allSizes;
    private List<Integer> categoryIds;

    @Data
    public static class VariantRequest {
        private String color;
        private List<String> sizes;
        private int imageCount;
    }
}