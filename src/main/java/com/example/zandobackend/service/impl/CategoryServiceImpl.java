package com.example.zandobackend.service.impl;

import com.example.zandobackend.model.dto.CategoryDto;
import com.example.zandobackend.model.entity.Category;
import com.example.zandobackend.repository.CategoryRepo;
import com.example.zandobackend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepo categoryRepo;

    @Override
    public List<CategoryDto> getCategoryTree() {
        List<Category> mainCategories = categoryRepo.findByParentIsNull();
        if (mainCategories.isEmpty()) {
            return Collections.emptyList();
        }
        return mainCategories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private CategoryDto convertToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getCategoryId());
        dto.setName(category.getName());

        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            dto.setChildren(
                    category.getChildren().stream()
                            .map(this::convertToDto)
                            .collect(Collectors.toSet())
            );
        } else {
            dto.setChildren(Collections.emptySet());
        }
        return dto;
    }
}