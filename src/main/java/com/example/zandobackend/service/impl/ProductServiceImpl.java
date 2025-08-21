package com.example.zandobackend.service.impl;

import com.example.zandobackend.model.entity.Product;
import com.example.zandobackend.repository.ProductRepo;
import com.example.zandobackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepo productRepo;



    @Override
    public List<Product> getProducts(Product product) {
        return productRepo.getProducts(product);
    }
}
