package com.example.zandobackend.model.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private Double price;          // final price
    private Double originalPrice;  // base price
    private Integer discount;
    private List<GalleryResponse> gallery;
    private List<String> availableSizes;
    private String description;

    @Data
    @Builder
    public static class GalleryResponse {
        private String color;
        private List<String> images;
    }
}
