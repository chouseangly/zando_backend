package com.example.zandobackend.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class VariantCreateRequest {
    private String color;
    private List<String> availableSizes;
}