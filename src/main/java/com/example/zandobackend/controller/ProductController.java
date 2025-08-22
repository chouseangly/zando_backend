package com.example.zandobackend.controller;


import com.example.zandobackend.model.dto.ProductCreateRequest;
import com.example.zandobackend.model.dto.ProductResponse;
import com.example.zandobackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> createProduct(
            @RequestPart("request") ProductCreateRequest request,
            @RequestParam Map<String, MultipartFile[]> fileMap) {

        ProductResponse createdProduct = productService.createProduct(request, fileMap);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @GetMapping
    public ResponseEntity<Map<String, ProductResponse>> getAllProducts() {
        Map<String, ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
}