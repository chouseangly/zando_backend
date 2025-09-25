// chouseangly/zando_backend/zando_backend-main/src/main/java/com/example/zandobackend/model/dto/ProductResponse.java

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
    private Double price;
    private Double originalPrice;
    private Integer discount;
    private Boolean isAvailable;
    private List<GalleryResponse> gallery;
    private String description;
    private List<CategoryDto> categories;

    // ✅ ADDED: Fields for dynamic stats from the backend
    private Long sell;
    private Long view;
    private Double earning;

    @Data
    @Builder
    public static class GalleryResponse {
        private String color;
        private int quantity;
        private List<String> images;
        private List<String> sizes;
    }
}