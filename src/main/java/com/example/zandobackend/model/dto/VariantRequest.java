package com.example.zandobackend.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class VariantRequest {
    private String color;
    private List<ImageRequest> images;
    private List<SizeRequest> availableSizes;
}