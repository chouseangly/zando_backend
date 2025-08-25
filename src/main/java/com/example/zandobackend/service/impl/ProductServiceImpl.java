package com.example.zandobackend.service.impl;

import com.example.zandobackend.model.dto.CreateProductRequest;
import com.example.zandobackend.model.dto.ProductResponse;
import com.example.zandobackend.model.entity.Product;
import com.example.zandobackend.model.entity.ProductVariant;
import com.example.zandobackend.repository.ProductRepo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements com.example.zandobackend.service.ProductService {

    private final ProductRepo productRepo;

    @Value("${pinata.api.key}")
    private String pinataApiKey;

    @Value("${pinata.secret.api.key}")
    private String pinataSecretApiKey;

    private static final String PINATA_URL = "https://api.pinata.cloud/pinning/pinFileToIPFS";

    private final ObjectMapper mapper = new ObjectMapper();

    @Transactional
    @Override
    public ProductResponse createProduct(CreateProductRequest request, List<MultipartFile> images) throws IOException {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setDiscountPercent(request.getDiscountPercent());
        // --- FIX: Insert the product into the database FIRST ---
        productRepo.insertProduct(product);

        int imageIndex = 0; // This will keep track of our position in the images list

        for (CreateProductRequest.VariantRequest variantReq : request.getVariants()) {
            // ... (variant and size insertion logic remains the same) ...
            var variantDTO = new com.example.zandobackend.model.dto.VariantInsertDTO();
            variantDTO.setProductId(product.getProductId());
            variantDTO.setColor(variantReq.getColor());
            productRepo.insertVariant(variantDTO);

            Long variantId = variantDTO.getVariantId();

            // ... (size insertion logic remains the same) ...
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


            // --- NEW IMAGE UPLOAD LOGIC ---
            // This loop now processes exactly 'imageCount' images for the current variant.
            int imagesToUpload = variantReq.getImageCount();
            for (int i = 0; i < imagesToUpload && imageIndex < images.size(); i++) {
                String url = uploadToPinata(images.get(imageIndex++));
                productRepo.insertImage(variantId, url);
            }
        }

        // Fetch product with variants
        Product savedProduct = getProduct(product.getProductId());

        return mapToProductResponse(savedProduct);
    }
    @Override
    public ProductResponse getProductResponse(Long id) {
        Product product = getProduct(id);
        return mapToProductResponse(product);
    }

    // In ProductServiceImpl.java

    @Override
    public List<ProductResponse> getAllProducts() {
        // 1. Get the list of basic product ENTITIES from the repository.
        List<Product> products = productRepo.selectAllProducts();

        // 2. For each product, use your existing getProductResponse method
        //    to fully load all its data and map it to the final response object.
        return products.stream()
                .map(product -> getProductResponse(product.getProductId()))
                .collect(Collectors.toList());
    }
    @Override
    public List<CreateProductRequest.VariantRequest> parseVariants(String variantsJson) throws IOException {
        return mapper.readValue(variantsJson, new TypeReference<List<CreateProductRequest.VariantRequest>>() {});
    }

    // --- Helper methods ---
    // --- Helper methods ---
    private Product getProduct(Long id) {
        Product product = productRepo.selectProductById(id);
        List<ProductVariant> variants = productRepo.selectVariantsByProductId(id);
        for (ProductVariant variant : variants) {
            // --- FIX THESE TWO LINES ---
            variant.setImages(productRepo.selectImagesByVariantId(variant.getVariantId()));
            variant.setSizes(productRepo.selectSizesByVariantId(variant.getVariantId()));
        }
        product.setVariants(variants);
        return product;
    }

    private ProductResponse mapToProductResponse(Product product) {
        double finalPrice = product.getBasePrice();
        if (product.getDiscountPercent() != null) {
            finalPrice -= product.getBasePrice() * product.getDiscountPercent() / 100.0;
        }

        List<String> availableSizes = product.getVariants().stream()
                .flatMap(v -> v.getSizes().stream())
                .distinct()
                .toList();

        List<ProductResponse.GalleryResponse> gallery = product.getVariants().stream()
                .map(v -> ProductResponse.GalleryResponse.builder()
                        .color(v.getColor())
                        .images(v.getImages())
                        .build())
                .toList();

        return ProductResponse.builder()
                .id(product.getProductId())
                .name(product.getName())
                .price(finalPrice)
                .originalPrice(product.getBasePrice())
                .discount(product.getDiscountPercent())
                .gallery(product.getVariants().stream()
                        .map(v -> ProductResponse.GalleryResponse.builder()
                                .color(v.getColor())
                                .images(v.getImages())
                                .build())
                        .toList())
                .availableSizes(availableSizes)
                .description(product.getDescription())
                .build();
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
