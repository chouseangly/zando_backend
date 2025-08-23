package com.example.zandobackend.controller;

import com.example.zandobackend.model.dto.CreateProductRequest;
import com.example.zandobackend.model.dto.ProductResponse;
import com.example.zandobackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("basePrice") Double basePrice,
            @RequestParam(value = "discountPercent", required = false) Integer discountPercent,
            @RequestParam(value = "allSizes", required = false) List<String> allSizes,
            @RequestParam("variants") String variantsJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws IOException {

        // Build request
        CreateProductRequest request = new CreateProductRequest();
        request.setName(name);
        request.setDescription(description);
        request.setBasePrice(basePrice);
        request.setDiscountPercent(discountPercent);
        request.setAllSizes(allSizes);

        // Parse variants JSON string
        request.setVariants(productService.parseVariants(variantsJson));

        ProductResponse response = productService.createProduct(request, images);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        ProductResponse response = productService.getProductResponse(id);
        return ResponseEntity.ok(response);
    }
}
