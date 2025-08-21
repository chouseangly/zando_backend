package com.example.zandobackend.service;

import com.example.zandobackend.model.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    /**
     * Retrieves all categories and structures them as a hierarchical tree.
     * @return A list of top-level CategoryDtos, each containing its children.
     */
    List<CategoryDto> getCategoryTree();
}