package com.example.zandobackend.service;

import com.example.zandobackend.model.dto.ProductRequest;
import com.example.zandobackend.model.dto.ProductResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request, List<MultipartFile> images) throws IOException;

    List<ProductRequest.VariantRequest> parseVariants(String variantsJson) throws IOException;

    ProductResponse getProductResponse(Long id);

    List<ProductResponse> getAllProducts();

    // MODIFIED: Added the product 'id' parameter
    ProductResponse updateProduct(Long id, ProductRequest request, List<MultipartFile> images) throws IOException;

    ProductResponse deleteProduct(Long id);
}