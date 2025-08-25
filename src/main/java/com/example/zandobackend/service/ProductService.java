package com.example.zandobackend.service;



import com.example.zandobackend.model.dto.CreateProductRequest;
import com.example.zandobackend.model.dto.ProductResponse;
import com.example.zandobackend.model.entity.Product;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;


public interface ProductService {
    ProductResponse createProduct(CreateProductRequest request, List<MultipartFile> images) throws IOException;


    List<CreateProductRequest.VariantRequest> parseVariants(String variantsJson) throws IOException;

    ProductResponse getProductResponse(Long id);


    List<ProductResponse> getAllProducts();
}
