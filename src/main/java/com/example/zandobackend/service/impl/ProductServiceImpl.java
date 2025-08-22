package com.example.zandobackend.service.impl;



import com.example.zandobackend.model.dto.ProductCreateRequest;
import com.example.zandobackend.model.dto.ProductResponse;
import com.example.zandobackend.model.dto.VariantCreateRequest;
import com.example.zandobackend.model.entity.*;
import com.example.zandobackend.repository.ProductRepo;
import com.example.zandobackend.repository.SizeRepo;
import com.example.zandobackend.service.ProductService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;
    private final SizeRepo sizeRepo;
    private final RestTemplate restTemplate;

    @Value("${pinata.jwt}")
    private String pinataJwt;

    @Value("${pinata.api.base-url}")
    private String pinataApiBaseUrl;

    private static final String PINATA_GATEWAY_URL = "https://gateway.pinata.cloud/ipfs/";


    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request, Map<String, MultipartFile[]> fileMap) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setDiscount(request.getDiscount());
        productRepo.insertProduct(product);

        Map<String, Size> sizeMap = new HashMap<>();
        if (request.getAllSizes() != null) {
            for (String sizeName : request.getAllSizes()) {
                Size size = sizeRepo.findByName(sizeName).orElseGet(() -> {
                    Size newSize = new Size(sizeName);
                    sizeRepo.insertSize(newSize);
                    return newSize;
                });
                sizeMap.put(sizeName, size);
            }
        }

        for (VariantCreateRequest variantRequest : request.getVariants()) {
            ProductVariant variant = new ProductVariant();
            variant.setColor(variantRequest.getColor());
            variant.setProductId(product.getId());
            productRepo.insertProductVariant(variant);

            String fileMapKey = "files_" + variantRequest.getColor();
            MultipartFile[] files = fileMap.getOrDefault(fileMapKey, new MultipartFile[0]);
            if (files.length > 6) {
                throw new IllegalArgumentException("Cannot upload more than 6 images for color: " + variantRequest.getColor());
            }

            for (MultipartFile file : files) {
                try {
                    String imageUrl = uploadFileToPinata(file);
                    ProductImage productImage = new ProductImage();
                    productImage.setImageUrl(imageUrl);
                    productImage.setVariantId(variant.getId());
                    productRepo.insertProductImage(productImage);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload image: " + file.getOriginalFilename(), e);
                }
            }

            if (variantRequest.getAvailableSizes() != null) {
                for (String availableSizeName : variantRequest.getAvailableSizes()) {
                    Size size = sizeMap.get(availableSizeName);
                    if (size != null) {
                        VariantSize variantSize = new VariantSize();
                        variantSize.setVariantId(variant.getId());
                        variantSize.setSizeId(size.getId());
                        variantSize.setAvailable(true);
                        productRepo.insertVariantSize(variantSize);
                    }
                }
            }
        }
        // In a real app, you would fetch the newly created product to build the response
        // But for a POST response, returning a confirmation with the ID is often sufficient.
        return ProductResponse.builder().id(product.getId()).name(product.getName()).build();
    }

    @Override
    public Map<String, ProductResponse> getAllProducts() {
        List<Product> products = productRepo.findAllProducts();

        // Use a LinkedHashMap to preserve insertion order
        return products.stream()
                .map(this::mapToProductResponse) // Convert each Product POJO to a ProductResponse DTO
                .collect(Collectors.toMap(
                        response -> String.valueOf(response.getId()),
                        response -> response,
                        (oldValue, newValue) -> oldValue, // In case of duplicate keys, keep the old one
                        LinkedHashMap::new
                ));
    }

    // Helper method to convert the entity POJO to the response DTO
    private ProductResponse mapToProductResponse(Product product) {
        // Get all available sizes for this product
        Set<String> availableSizesSet = product.getVariants().stream()
                .flatMap(variant -> variant.getVariantSizes().stream())
                .filter(VariantSize::isAvailable)
                .map(vs -> vs.getSize().getName())
                .collect(Collectors.toSet());

        // Get all possible sizes (assuming they are consistently stored across variants of a product)
        Set<String> allSizesSet = product.getVariants().stream()
                .flatMap(variant -> variant.getVariantSizes().stream())
                .map(vs -> vs.getSize().getName())
                .collect(Collectors.toSet());

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .discount(product.getDiscount())
                .availableSizes(new ArrayList<>(availableSizesSet))
                .allSizes(new ArrayList<>(allSizesSet)) // This is a simplification; in reality, you might store this on the product table
                .gallery(product.getVariants().stream().map(variant ->
                        ProductResponse.GalleryResponse.builder()
                                .color(variant.getColor())
                                .images(variant.getImages().stream()
                                        .map(ProductImage::getImageUrl)
                                        .collect(Collectors.toList()))
                                .build()
                ).collect(Collectors.toList()))
                .build();
    }

    // === PINATA UPLOAD LOGIC ===
    private String uploadFileToPinata(MultipartFile file) throws IOException {
        String url = pinataApiBaseUrl + "/pinning/pinFileToIPFS";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(pinataJwt.replace("Bearer ", ""));

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
        body.add("file", fileResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, requestEntity, JsonNode.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String ipfsHash = response.getBody().get("IpfsHash").asText();
                return PINATA_GATEWAY_URL + ipfsHash;
            } else {
                throw new RuntimeException("Pinata API Error: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload to Pinata", e);
        }
    }
}