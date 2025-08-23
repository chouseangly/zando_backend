package com.example.zandobackend.model.entity;

import lombok.Data;
import java.util.List;

@Data
public class ProductVariant {
    private String color;
    private List<String> images; // image URLs
    private List<String> sizes; // available sizes for this color
}
