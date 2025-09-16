package com.example.zandobackend.service;

import com.example.zandobackend.model.dto.ProductRequest;
import com.example.zandobackend.model.dto.ProductResponse;
// ✅ Add this import
import com.example.zandobackend.model.entity.Product;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request, List<MultipartFile> images) throws IOException;

    List<ProductRequest.VariantRequest> parseVariants(String variantsJson) throws IOException;

    ProductResponse getProductResponse(Long id);

    // ✅ Add this new method to get the full Product entity
    Product getProductById(Long id);

    List<ProductResponse> getAllProducts();

    ProductResponse updateProduct(Long id, ProductRequest request, List<MultipartFile> images) throws IOException;

    ProductResponse deleteProduct(Long id);

    List<ProductResponse> getProductsByCategoryId(Integer categoryId);
}