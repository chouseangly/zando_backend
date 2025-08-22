package com.example.zandobackend.service.impl;

import com.example.zandobackend.model.dto.ImageRequest;
import com.example.zandobackend.model.dto.ProductRequest;
import com.example.zandobackend.model.dto.SizeRequest;
import com.example.zandobackend.model.dto.VariantRequest;
import com.example.zandobackend.model.entity.*;
import com.example.zandobackend.repository.ProductRepo;
import com.example.zandobackend.repository.SizeRepo;
import com.example.zandobackend.service.ProductService;
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
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;
    private final SizeRepo sizeRepo;
    private final ModelMapper modelMapper = new ModelMapper();

    // 1. Inject Pinata keys directly into this class
    @Value("${pinata.api.key}")
    private String pinataApiKey;

    @Value("${pinata.secret.api.key}")
    private String pinataSecretApiKey;

    private static final String PINATA_URL = "https://api.pinata.cloud/pinning/pinFileToIPFS";

    @Override
    public List<Product> findAllProducts() {
        return productRepo.findAll();
    }

    @Override
    public Optional<Product> findProductById(Integer id) {
        return productRepo.findById(id);
    }



    @Transactional
    @Override
    public Product createProduct(ProductRequest productRequest, Map<String, MultipartFile> files) {
        Product product = modelMapper.map(productRequest, Product.class);
        productRepo.insertProduct(product);

        if (productRequest.getVariants() != null) {
            for (VariantRequest variantReq : productRequest.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setProductId(product.getProductId());
                variant.setColor(variantReq.getColor());
                productRepo.insertVariant(variant);

                if (variantReq.getImages() != null) {
                    for (ImageRequest imageReq : variantReq.getImages()) {
                        MultipartFile file = files.get(imageReq.getImageRef());
                        if (file == null) {
                            throw new RuntimeException("File not found for reference key: " + imageReq.getImageRef());
                        }

                        // 3. Call the internal private method to upload the file
                        String imageUrl = uploadFileToPinata(file);

                        ProductImage image = new ProductImage();
                        image.setVariantId(variant.getVariantId());
                        image.setImageUrl(imageUrl);
                        productRepo.insertImage(image);
                    }
                }

                if (variantReq.getAvailableSizes() != null) {
                    for (SizeRequest sizeReq : variantReq.getAvailableSizes()) {
                        Size size = sizeRepo.findByName(sizeReq.getName())
                                .orElseThrow(() -> new RuntimeException("Size not found: " + sizeReq.getName()));

                        VariantSize variantSize = new VariantSize();
                        variantSize.setVariantId(variant.getVariantId());
                        variantSize.setSizeId(size.getSizeId());
                        variantSize.setAvailable(sizeReq.isAvailable());
                        productRepo.insertVariantSize(variantSize);
                    }
                }
            }
        }
        return findProductById(product.getProductId())
                .orElseThrow(() -> new RuntimeException("Failed to fetch newly created product"));
    }

    // 2. The Pinata upload logic is now a private method inside this service
    private String uploadFileToPinata(MultipartFile file) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(PINATA_URL);
            // Use the injected keys
            post.setHeader("pinata_api_key", pinataApiKey);
            post.setHeader("pinata_secret_api_key", pinataSecretApiKey);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", file.getInputStream(), ContentType.DEFAULT_BINARY,
                    UUID.randomUUID() + "_" + file.getOriginalFilename());

            HttpEntity entity = builder.build();
            post.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                String json = EntityUtils.toString(response.getEntity());
                // The public gateway URL for the uploaded file
                return "https://gateway.pinata.cloud/ipfs/" + new ObjectMapper().readTree(json).get("IpfsHash").asText();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Pinata", e);
        }
    }

    @Transactional
    @Override
    public Product updateProduct(Integer id, ProductRequest productRequest, Map<String, MultipartFile> files) {
        // Update logic would follow the same pattern, using the private uploadFileToPinata method
        throw new UnsupportedOperationException("Update not yet implemented.");
    }

    @Override
    public void deleteProductById(Integer id) {
        productRepo.deleteById(id);
    }
}