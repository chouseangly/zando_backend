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
    private List<VariantRequest> variants;
    private List<String> allSizes;
    private List<Integer> categoryIds; // <-- ADD THIS LINE

    @Data
    public static class VariantRequest {
        private String color;
        private List<String> sizes;
        private int imageCount; // Combined the fields into one class
    }
}