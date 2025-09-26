package com.example.zandobackend.service.impl;

import com.example.zandobackend.model.dto.CartItemRequest;
import com.example.zandobackend.model.dto.CartViewDto;
import com.example.zandobackend.model.entity.CartItemDto;
import com.example.zandobackend.repository.CartRepo;
import com.example.zandobackend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepo cartRepo;

    @Override
    @Transactional
    public CartViewDto addOrUpdateCart(CartItemRequest request) {
        Long cartId = cartRepo.findCartIdByUserId(request.getUserId());
        if (cartId == null) {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", request.getUserId());
            cartRepo.createCart(params);
            cartId = (Long) params.get("cart_id");
        }

        CartItemDto existingItem = cartRepo.findCartItem(cartId, request.getProductId(), request.getVariantId(), request.getSizeId());
        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            cartRepo.updateCartItemQuantity(existingItem.getCartItemId(), newQuantity);
        } else {
            cartRepo.addCartItem(cartId, request.getProductId(), request.getVariantId(), request.getSizeId(), request.getQuantity());
        }
        return getCartByUserId(request.getUserId());
    }

    @Override
    public CartViewDto getCartByUserId(Long userId) {
        Long cartId = cartRepo.findCartIdByUserId(userId);
        if (cartId == null) {
            return new CartViewDto();
        }
        List<CartItemDto> items = cartRepo.findCartItemsByCartId(cartId);
        CartViewDto cartView = new CartViewDto();
        cartView.setCartId(cartId);
        cartView.setUserId(userId);
        cartView.setItems(items);
        return cartView;
    }

    @Override
    public void removeCartItem(Long cartItemId) {
        cartRepo.removeCartItem(cartItemId);
    }
}
