package com.example.zandobackend.controller;

import com.example.zandobackend.model.dto.ApiResponse;
import com.example.zandobackend.model.dto.ProductRequest;
import com.example.zandobackend.model.dto.ProductResponse;
import com.example.zandobackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping(value ="/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("basePrice") Double basePrice,
            @RequestParam(value = "discountPercent", required = false) Integer discountPercent,
            @RequestParam(value = "isAvailable", required = false) Boolean isAvailable,
            @RequestParam("variants") String variantsJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "categoryIds", required = false) List<Integer> categoryIds
    ) throws IOException {

        ProductRequest request = new ProductRequest();
        request.setName(name);
        request.setDescription(description);
        request.setBasePrice(basePrice);
        request.setDiscountPercent(discountPercent);
        request.setIsAvailable(isAvailable);
        request.setVariants(productService.parseVariants(variantsJson));
        request.setCategoryIds(categoryIds);

        ProductResponse response = productService.createProduct(request, images);

        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>(
                "Product created successfully",
                response,
                HttpStatus.CREATED.value(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long id) {
        ProductResponse response = productService.getProductResponse(id);
        if (response == null) {
            ApiResponse<ProductResponse> apiResponse = new ApiResponse<>(
                    "Product not found with id: " + id,
                    null,
                    HttpStatus.NOT_FOUND.value(),
                    LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }

        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>(
                "Product retrieved successfully",
                response,
                HttpStatus.OK.value(),
                LocalDateTime.now()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        List<ProductResponse> responses = productService.getAllProducts();
        ApiResponse<List<ProductResponse>> apiResponse = new ApiResponse<>(
                "Products retrieved successfully",
                responses,
                HttpStatus.OK.value(),
                LocalDateTime.now()
        );
        return ResponseEntity.ok(apiResponse);
    }

    // ✅ **THIS IS THE CRITICAL BACKEND FIX**
    @PutMapping(value = "/admin/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @RequestParam(value="name",required = false) String name,
            @RequestParam(value = "description",required = false) String description,
            @RequestParam(value = "basePrice",required = false) Double basePrice,
            @RequestParam(value = "discountPercent", required = false) Integer discountPercent,
            @RequestParam(value = "isAvailable", required = false) Boolean isAvailable, // This parameter was missing
            @RequestParam(value = "variants",required = false) String variantsJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "categoryIds", required = false) List<Integer> categoryIds
    ) throws IOException {

        ProductRequest request = new ProductRequest();
        request.setName(name);
        request.setDescription(description);
        request.setBasePrice(basePrice);
        request.setDiscountPercent(discountPercent);
        request.setIsAvailable(isAvailable); // Now we handle the 'isAvailable' field
        request.setVariants(productService.parseVariants(variantsJson));
        request.setCategoryIds(categoryIds);

        ProductResponse response = productService.updateProduct(id, request, images);

        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>(
                "Product updated successfully",
                response,
                HttpStatus.OK.value(),
                LocalDateTime.now()
        );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> deleteProduct(@PathVariable Long id) {
        ProductResponse response = productService.deleteProduct(id);
        return ResponseEntity.ok(
                new ApiResponse<>("delete successfully", response, HttpStatus.OK.value(), LocalDateTime.now())
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByCategory(@PathVariable Integer categoryId) {
        List<ProductResponse> responses = productService.getProductsByCategoryId(categoryId);
        ApiResponse<List<ProductResponse>> apiResponse = new ApiResponse<>(
                "Products for category " + categoryId + " retrieved successfully",
                responses,
                HttpStatus.OK.value(),
                LocalDateTime.now()
        );
        return ResponseEntity.ok(apiResponse);
    }
}