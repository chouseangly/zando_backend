package com.example.zandobackend.controller;

import com.example.zandobackend.model.dto.ApiResponse;
import com.example.zandobackend.model.dto.ProductRequest;
import com.example.zandobackend.model.entity.Product;
import com.example.zandobackend.service.FileStorageService;
import com.example.zandobackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        List<Product> products = productService.findAllProducts();
        return ResponseEntity.ok(
                new ApiResponse<>("All products fetched successfully", products, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable Integer id) {
        return productService.findProductById(id)
                .map(product -> ResponseEntity.ok(
                        new ApiResponse<>("Product found", product, HttpStatus.OK.value(), LocalDateTime.now())
                ))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ApiResponse<>("Product not found", null, HttpStatus.NOT_FOUND.value(), LocalDateTime.now())
                ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(@RequestBody ProductRequest productRequest) {
        Product createdProduct = productService.createProduct(productRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>("Product created successfully", createdProduct, HttpStatus.CREATED.value(), LocalDateTime.now())
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(@PathVariable Integer id, @RequestBody ProductRequest productRequest) {
        Product updatedProduct = productService.updateProduct(id, productRequest);
        return ResponseEntity.ok(
                new ApiResponse<>("Product updated successfully", updatedProduct, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Integer id) {
        productService.deleteProductById(id);
        return ResponseEntity.ok(
                new ApiResponse<>("Product deleted successfully", null, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }
}