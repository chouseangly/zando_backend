package com.example.zandobackend.model.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class CategoryDto {
    private Integer id;
    private String name;
    private Set<CategoryDto> children;
}