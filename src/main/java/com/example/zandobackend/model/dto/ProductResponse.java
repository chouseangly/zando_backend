package com.example.zandobackend.model.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price; // Final price
    private BigDecimal originalPrice;
    private Integer discount;
    private List<String> allSizes;
    private List<String> availableSizes;
    private List<GalleryResponse> gallery;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GalleryResponse {
        private String color;
        private List<String> images;
    }
}