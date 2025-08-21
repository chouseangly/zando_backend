package com.example.zandobackend.service;

import com.example.zandobackend.model.entity.Product;

import java.util.List;

public interface ProductService {
    List<Product> getProducts(Product product);
}
