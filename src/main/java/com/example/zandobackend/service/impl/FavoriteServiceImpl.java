package com.example.zandobackend.service.impl;

import com.example.zandobackend.model.dto.FavoriteRequest;
import com.example.zandobackend.model.dto.FavoriteResponse;
import com.example.zandobackend.model.dto.ProductResponse;
import com.example.zandobackend.model.entity.Favorite;
import com.example.zandobackend.repository.FavoriteRepo;
import com.example.zandobackend.service.FavoriteService;
import com.example.zandobackend.service.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepo favoriteRepo;
    private final ProductService productService;
    private final ModelMapper modelMapper;

    // ✅ FIX: Use a manual constructor with @Lazy to break the dependency cycle.
    public FavoriteServiceImpl(FavoriteRepo favoriteRepo, @Lazy ProductService productService, ModelMapper modelMapper) {
        this.favoriteRepo = favoriteRepo;
        this.productService = productService;
        this.modelMapper = modelMapper;
    }

    @Override
    public FavoriteResponse addFavorite(FavoriteRequest favoriteRequest) {
        if (isFavorite(favoriteRequest.getUserId(), favoriteRequest.getProductId())) {
            throw new RuntimeException("Product is already in favorites.");
        }
        // Insert into the database
        favoriteRepo.addFavorite(favoriteRequest);

        // MODIFIED: Fetch the newly created favorite record and return it
        Favorite newFavourite = favoriteRepo.findFavoriteByUserAndProduct(favoriteRequest.getUserId(), favoriteRequest.getProductId());
        return mapToFavoriteResponse(newFavourite);
    }

    @Override
    public void removeFavorite(Long userId, Long productId) {
        if (!isFavorite(userId, productId)) {
            throw new RuntimeException("Product is not in favorites.");
        }
        favoriteRepo.removeFavorite(userId, productId);
    }

    @Override
    public List<FavoriteResponse> getFavoritesByUserId(Long userId) {
        List<Favorite> favorites = favoriteRepo.findFavoritesByUserId(userId);
        return favorites.stream()
                .map(this::mapToFavoriteResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFavorite(Long userId, Long productId) {
        return favoriteRepo.isFavorite(userId, productId) > 0;
    }

    private FavoriteResponse mapToFavoriteResponse(Favorite favorite) {
        FavoriteResponse response = modelMapper.map(favorite, FavoriteResponse.class);
        if (favorite.getProduct() != null) {
            ProductResponse productResponse = productService.getProductResponse(favorite.getProduct().getProductId());
            response.setProduct(productResponse);
        }
        return response;
    }

    @Override
    public List<Long> findUserIdsByProductId(Long productId) {
        return favoriteRepo.findUserIdsByProductId(productId);
    }
}