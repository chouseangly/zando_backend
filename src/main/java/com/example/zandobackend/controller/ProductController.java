package com.example.zandobackend.controller;

import com.example.zandobackend.model.dto.ApiResponse;
import com.example.zandobackend.model.dto.CreateProductRequest;
import com.example.zandobackend.model.dto.ProductResponse;
import com.example.zandobackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("basePrice") Double basePrice,
            @RequestParam(value = "discountPercent", required = false) Integer discountPercent,
            @RequestParam("variants") String variantsJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws IOException {

        // Build request
        CreateProductRequest request = new CreateProductRequest();
        request.setName(name);
        request.setDescription(description);
        request.setBasePrice(basePrice);
        request.setDiscountPercent(discountPercent);

        // Parse variants JSON string
        request.setVariants(productService.parseVariants(variantsJson));

        ProductResponse response = productService.createProduct(request, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>("post product successfully",response,HttpStatus.OK.value(), LocalDateTime.now())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long id) {
        ProductResponse response = productService.getProductResponse(id);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ApiResponse<>("Product not found", null, HttpStatus.OK.value(), LocalDateTime.now())
            );
        }
        return ResponseEntity.ok(
                new ApiResponse<>("Product found", response, HttpStatus.OK.value(), LocalDateTime.now())
        );

    }
    @GetMapping("/getAllProduct")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        List<ProductResponse> responses = productService.getAllProducts();
        return ResponseEntity.ok(
                new ApiResponse<>("Products retrieved successfully", responses, HttpStatus.OK.value(), LocalDateTime.now())
        );

    }
}
