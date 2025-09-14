package com.example.zandobackend.service.impl;

import com.example.zandobackend.model.dto.CategoryDto;
import com.example.zandobackend.model.dto.ProductRequest;
import com.example.zandobackend.model.dto.ProductResponse;
import com.example.zandobackend.model.dto.VariantInsertDTO;
import com.example.zandobackend.model.entity.Category;
import com.example.zandobackend.model.entity.Product;
import com.example.zandobackend.model.entity.ProductVariant;
import com.example.zandobackend.repository.ProductRepo;
import com.example.zandobackend.service.ProductService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;

    @Value("${pinata.api.key}")
    private String pinataApiKey;

    @Value("${pinata.secret.api.key}")
    private String pinataSecretApiKey;

    private static final String PINATA_URL = "https://api.pinata.cloud/pinning/pinFileToIPFS";

    private final ObjectMapper mapper = new ObjectMapper();


    @Transactional
    @Override
    public ProductResponse createProduct(ProductRequest request, List<MultipartFile> images) throws IOException {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setDiscountPercent(request.getDiscountPercent());
        // ✅ MODIFIED: Use the value from the request, defaulting to true if null
        product.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);

        productRepo.insertProduct(product);

        if (request.getCategoryIds() != null) {
            for (Integer categoryId : request.getCategoryIds()) {
                productRepo.insertProductCategory(product.getProductId(), categoryId);
            }
        }

        processVariantsAndImages(product.getProductId(), request.getVariants(), images);

        return getProductResponse(product.getProductId());
    }

    @Transactional
    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request, List<MultipartFile> images) throws IOException {
        Product existingProduct = productRepo.selectProductById(id);
        if (existingProduct == null) {
            throw new RuntimeException("Product not found with id: " + id);
        }

        Product productToUpdate = new Product();
        productToUpdate.setProductId(id);

        // ✅ MODIFIED: Only set fields if they are provided in the request
        if (request.getName() != null) productToUpdate.setName(request.getName());
        if (request.getDescription() != null) productToUpdate.setDescription(request.getDescription());
        if (request.getBasePrice() != null) productToUpdate.setBasePrice(request.getBasePrice());
        if (request.getDiscountPercent() != null) productToUpdate.setDiscountPercent(request.getDiscountPercent());
        if (request.getIsAvailable() != null) productToUpdate.setIsAvailable(request.getIsAvailable());

        productRepo.updateProduct(productToUpdate);

        if (request.getCategoryIds() != null) {
            productRepo.deleteProductCategoriesByProductId(id);
            for (Integer categoryId : request.getCategoryIds()) {
                productRepo.insertProductCategory(id, categoryId);
            }
        }

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            productRepo.deleteVariantsByProductId(id);
            processVariantsAndImages(id, request.getVariants(), images);
        }

        return getProductResponse(id);
    }

    // ... (rest of the file remains the same)
    @Transactional
    @Override
    public ProductResponse deleteProduct(Long id) {
        ProductResponse productToDelete = getProductResponse(id);

        if (productToDelete == null) {
            throw new RuntimeException("Product not found with id: " + id);
        }

        productRepo.deleteProductById(id);
        return productToDelete;
    }

    @Override
    public List<ProductResponse> getProductsByCategoryId(Integer categoryId) {
        List<Product> products = productRepo.selectProductsByCategoryId(categoryId);
        return products.stream()
                .map(product -> getProductResponse(product.getProductId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    @Override
    public ProductResponse getProductResponse(Long id) {
        Product product = getProduct(id);
        if (product == null) {
            return null;
        }
        return mapToProductResponse(product);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepo.selectAllProducts();
        return products.stream()
                .map(product -> getProductResponse(product.getProductId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductRequest.VariantRequest> parseVariants(String variantsJson) throws IOException {
        return mapper.readValue(variantsJson, new TypeReference<>() {});
    }


    private Product getProduct(Long id) {
        Product product = productRepo.selectProductById(id);
        if (product == null) {
            return null;
        }
        product.setCategories(productRepo.selectCategoriesByProductId(id));
        List<ProductVariant> variants = productRepo.selectVariantsByProductId(id);
        for (ProductVariant variant : variants) {
            variant.setImages(productRepo.selectImagesByVariantId(variant.getVariantId()));
            variant.setSizes(productRepo.selectSizesByVariantId(variant.getVariantId()));
        }
        product.setVariants(variants);
        return product;
    }

    private void processVariantsAndImages(Long productId, List<ProductRequest.VariantRequest> variantRequests, List<MultipartFile> images) throws IOException {
        int imageIndex = 0;

        if (variantRequests == null) return;

        for (ProductRequest.VariantRequest variantReq : variantRequests) {
            var variantDTO = new VariantInsertDTO();
            variantDTO.setProductId(productId);
            variantDTO.setColor(variantReq.getColor());
            productRepo.insertVariant(variantDTO);
            Long variantId = variantDTO.getVariantId();

            if(variantReq.getSizes() != null) {
                for (String size : variantReq.getSizes()) {
                    Long sizeId = productRepo.getSizeIdByName(size);
                    if (sizeId == null) {
                        Map<String, Object> params = new java.util.HashMap<>();
                        params.put("name", size);
                        productRepo.insertSize(params);
                        sizeId = ((Number) params.get("size_id")).longValue();
                    }
                    productRepo.insertVariantSize(variantId, sizeId);
                }
            }

            int imagesToUpload = variantReq.getImageCount();
            if (images != null) {
                for (int i = 0; i < imagesToUpload && imageIndex < images.size(); i++) {
                    String url = uploadToPinata(images.get(imageIndex++));
                    productRepo.insertImage(variantId, url);
                }
            }
        }
    }

    private ProductResponse mapToProductResponse(Product product) {
        double finalPrice = product.getBasePrice();
        if (product.getDiscountPercent() != null) {
            finalPrice -= product.getBasePrice() * product.getDiscountPercent() / 100.0;
        }

        List<CategoryDto> categoryDtos = product.getCategories().stream()
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList());

        return ProductResponse.builder()
                .id(product.getProductId())
                .name(product.getName())
                .price(finalPrice)
                .originalPrice(product.getBasePrice())
                .discount(product.getDiscountPercent())
                .isAvailable(product.getIsAvailable())
                .gallery(product.getVariants().stream()
                        .map(v -> ProductResponse.GalleryResponse.builder()
                                .color(v.getColor())
                                .images(v.getImages())
                                .sizes(v.getSizes())
                                .build())
                        .toList())
                .description(product.getDescription())
                .categories(categoryDtos)
                .build();
    }

    private CategoryDto mapToCategoryDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getCategoryId());
        dto.setName(category.getName());

        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            dto.setChildren(
                    category.getChildren().stream()
                            .map(this::mapToCategoryDto)
                            .collect(Collectors.toSet())
            );
        } else {
            dto.setChildren(Collections.emptySet());
        }
        return dto;
    }

    private String uploadToPinata(MultipartFile file) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(PINATA_URL);
            post.setHeader("pinata_api_key", pinataApiKey);
            post.setHeader("pinata_secret_api_key", pinataSecretApiKey);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", file.getInputStream(),
                    ContentType.APPLICATION_OCTET_STREAM, file.getOriginalFilename());
            post.setEntity(builder.build());

            try (CloseableHttpResponse response = client.execute(post)) {
                String resp = EntityUtils.toString(response.getEntity());
                return "https://maroon-fantastic-crab-577.mypinata.cloud/ipfs/" +
                        mapper.readTree(resp).get("IpfsHash").asText();
            }
        }
    }
}