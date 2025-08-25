package com.example.zandobackend.service.impl;

import com.example.zandobackend.model.dto.ProductRequest;
import com.example.zandobackend.model.dto.ProductResponse;
import com.example.zandobackend.model.dto.VariantInsertDTO;
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

        productRepo.insertProduct(product); // Insert to get the new product ID

        // Process variants, sizes, and images
        processVariantsAndImages(product.getProductId(), request.getVariants(), images);

        return getProductResponse(product.getProductId());
    }

    @Transactional
    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request, List<MultipartFile> images) throws IOException {
        // 1. Ensure the product exists
        Product existingProduct = productRepo.selectProductById(id);
        if (existingProduct == null) {
            throw new RuntimeException("Product not found with id: " + id); // Or a custom exception
        }

        // 2. Update the core product details
        Product productToUpdate = new Product();
        productToUpdate.setProductId(id);
        productToUpdate.setName(request.getName());
        productToUpdate.setDescription(request.getDescription());
        productToUpdate.setBasePrice(request.getBasePrice());
        productToUpdate.setDiscountPercent(request.getDiscountPercent());
        productRepo.updateProduct(productToUpdate);

        // 3. Delete all old variants, which will cascade delete images and size links
        productRepo.deleteVariantsByProductId(id);

        // 4. Create the new variants, sizes, and images from the request
        processVariantsAndImages(id, request.getVariants(), images);

        // 5. Fetch the updated product and return the response
        return getProductResponse(id);
    }

    @Transactional
    @Override
    public ProductResponse deleteProduct(Long id) {
        // 1. Fetch the full product details and map it to a response object *before* deleting.
        ProductResponse productToDelete = getProductResponse(id);

        // 2. Check if the product exists.
        if (productToDelete == null) {
            throw new RuntimeException("Product not found with id: " + id); // Or a custom exception
        }

        // 3. Delete the product from the database.
        productRepo.deleteProductById(id);

        // 4. Return the response object you saved earlier.
        return productToDelete;
    }

    @Override
    public ProductResponse getProductResponse(Long id) {
        Product product = getProduct(id);
        if (product == null) {
            return null; // Or throw an exception
        }
        return mapToProductResponse(product);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepo.selectAllProducts();
        return products.stream()
                .map(product -> getProductResponse(product.getProductId()))
                .filter(Objects::nonNull) // Ensure no null products are in the list
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductRequest.VariantRequest> parseVariants(String variantsJson) throws IOException {
        return mapper.readValue(variantsJson, new TypeReference<>() {});
    }

    // --- Helper methods ---

    private Product getProduct(Long id) {
        Product product = productRepo.selectProductById(id);
        if (product == null) {
            return null;
        }
        List<ProductVariant> variants = productRepo.selectVariantsByProductId(id);
        for (ProductVariant variant : variants) {
            variant.setImages(productRepo.selectImagesByVariantId(variant.getVariantId()));
            variant.setSizes(productRepo.selectSizesByVariantId(variant.getVariantId()));
        }
        product.setVariants(variants);
        return product;
    }

    /**
     * Helper method to create variants, sizes, and images for a given product.
     * This is used by both create and update operations.
     */
    private void processVariantsAndImages(Long productId, List<ProductRequest.VariantRequest> variantRequests, List<MultipartFile> images) throws IOException {
        int imageIndex = 0; // Tracks the current position in the flattened list of all images

        for (ProductRequest.VariantRequest variantReq : variantRequests) {
            // Insert the variant
            var variantDTO = new VariantInsertDTO();
            variantDTO.setProductId(productId);
            variantDTO.setColor(variantReq.getColor());
            productRepo.insertVariant(variantDTO);
            Long variantId = variantDTO.getVariantId();

            // Handle sizes for the variant
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

            // Handle image uploads for the variant
            int imagesToUpload = variantReq.getImageCount();
            for (int i = 0; i < imagesToUpload && imageIndex < images.size(); i++) {
                String url = uploadToPinata(images.get(imageIndex++));
                productRepo.insertImage(variantId, url);
            }
        }
    }

    private ProductResponse mapToProductResponse(Product product) {
        double finalPrice = product.getBasePrice();
        if (product.getDiscountPercent() != null) {
            finalPrice -= product.getBasePrice() * product.getDiscountPercent() / 100.0;
        }

        // FIX: Removed the unused 'availableSizes' list calculation.
        // The sizes are now correctly handled inside the 'gallery' object.

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
                                .sizes(v.getSizes())
                                .build())
                        .toList())
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