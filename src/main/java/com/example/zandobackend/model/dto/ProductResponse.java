// chouseangly/zando_backend/zando_backend-main/src/main/java/com/example/zandobackend/model/dto/ProductResponse.java

package com.example.zandobackend.model.dto;

import com.example.zandobackend.model.entity.Size;
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

    private Long sell;
    private Long view;
    private Double earning;

    @Data
    @Builder
    public static class GalleryResponse {
        private Long variantId; // ✅ ADD THIS LINE
        private String color;
        private int quantity;
        private List<String> images;
        private List<Size> sizes;
    }
}