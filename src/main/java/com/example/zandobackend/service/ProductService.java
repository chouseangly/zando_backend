package com.example.zandobackend.service;


import com.example.zandobackend.model.dto.ProductCreateRequest;
import com.example.zandobackend.model.dto.ProductResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface ProductService {
    ProductResponse createProduct(ProductCreateRequest request, Map<String, MultipartFile[]> fileMap);
    Map<String, ProductResponse> getAllProducts();

}