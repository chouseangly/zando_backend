package com.example.zandobackend.service;

import com.example.zandobackend.model.dto.ProductRequest;
import com.example.zandobackend.model.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> findAllProducts();
    Optional<Product> findProductById(Integer id);
    Product createProduct(ProductRequest productRequest);
    Product updateProduct(Integer id, ProductRequest productRequest);
    void deleteProductById(Integer id);
}