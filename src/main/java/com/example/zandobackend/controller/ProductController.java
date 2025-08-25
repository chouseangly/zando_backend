package com.example.zandobackend.controller;

import com.example.zandobackend.model.dto.ApiResponse;
import com.example.zandobackend.model.dto.ProductRequest;
import com.example.zandobackend.model.dto.ProductResponse;
import com.example.zandobackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Delete;
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

        // Build request DTO from request parts
        ProductRequest request = new ProductRequest();
        request.setName(name);
        request.setDescription(description);
        request.setBasePrice(basePrice);
        request.setDiscountPercent(discountPercent);
        request.setVariants(productService.parseVariants(variantsJson));

        ProductResponse response = productService.createProduct(request, images);

        // FIX: Use CREATED (201) status consistently
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

        // FIX: Return a 404 NOT FOUND status if the product doesn't exist
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

    // FIX: Changed path to be more RESTful (GET /api/products)
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

    // FIX: Corrected annotations, method signature, and service call for updating
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id, // FIX: Added @PathVariable to get the product ID from the URL
            @RequestParam(value="name",required = false) String name,
            @RequestParam(value = "description",required = false) String description,
            @RequestParam(value = "basePrice",required = false) Double basePrice,
            @RequestParam(value = "discountPercent", required = false) Integer discountPercent,
            @RequestParam(value = "variants",required = false) String variantsJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws IOException {

        // Build request DTO
        ProductRequest request = new ProductRequest();
        request.setName(name);
        request.setDescription(description);
        request.setBasePrice(basePrice);
        request.setDiscountPercent(discountPercent);
        request.setVariants(productService.parseVariants(variantsJson));

        // FIX: Passed the 'id' to the service method
        ProductResponse response = productService.updateProduct(id, request, images);

        // FIX: Use OK (200) status for a successful update
        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>(
                "Product updated successfully",
                response,
                HttpStatus.OK.value(),
                LocalDateTime.now()
        );
        return ResponseEntity.ok(apiResponse);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> deleteProduct(@PathVariable Long id) {
        ProductResponse response = productService.deleteProduct(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                new ApiResponse<>("delete successfully", response, HttpStatus.OK.value(), LocalDateTime.now())
        );

    }
}