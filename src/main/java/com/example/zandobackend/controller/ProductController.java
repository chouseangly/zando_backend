package com.example.zandobackend.controller;

import com.example.zandobackend.model.dto.ApiResponse;
import com.example.zandobackend.model.entity.Product;
import com.example.zandobackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>>getProducts(@RequestBody Product product) {
        List<Product> products = productService.getProducts(product);
       return ResponseEntity.status(HttpStatus.CREATED).body(
               new ApiResponse<>("get all product successfully",products,HttpStatus.OK.value(), LocalDateTime.now())
       );
    }
}
