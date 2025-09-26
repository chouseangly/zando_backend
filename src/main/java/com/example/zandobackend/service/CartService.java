package com.example.zandobackend.service;

import com.example.zandobackend.model.dto.CartItemRequest;
import com.example.zandobackend.model.dto.CartViewDto;

public interface CartService {
    CartViewDto addOrUpdateCart(CartItemRequest cartItemRequest);
    CartViewDto getCartByUserId(Long userId);
    void removeCartItem(Long cartItemId);
}