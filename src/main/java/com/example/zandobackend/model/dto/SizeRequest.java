package com.example.zandobackend.model.dto;

import lombok.Data;

@Data
public class SizeRequest {
    private String name; // e.g., "S", "M", "L"
    private boolean isAvailable;
}