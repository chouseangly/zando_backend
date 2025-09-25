// chouseangly/zando_backend/zando_backend-main/src/main/java/com/example/zandobackend/model/entity/Product.java
package com.example.zandobackend.model.entity;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class Product {
    private Long productId;
    private String uuid;
    private String name;
    private String description;
    private Double basePrice;
    private Integer discountPercent;
    private Boolean isAvailable;
    private Long views; // ✅ ADD THIS FIELD
    private List<ProductVariant> variants = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
}