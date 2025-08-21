package com.example.zandobackend.model.entity;

import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class Category {
    private Integer categoryId;
    private String name;
    private Category parent;
    private Set<Category> children;
}