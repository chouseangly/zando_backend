package com.example.zandobackend.controller;

import com.example.zandobackend.model.dto.ApiResponse;
import com.example.zandobackend.model.dto.CartItemRequest;
import com.example.zandobackend.model.dto.CartViewDto;
import com.example.zandobackend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CartViewDto>> addOrUpdateCart(@RequestBody CartItemRequest cartItemRequest) {
        try {
            CartViewDto cart = cartService.addOrUpdateCart(cartItemRequest);
            ApiResponse<CartViewDto> response = new ApiResponse<>(
                    "Item added to cart successfully.",
                    cart,
                    HttpStatus.OK.value(),
                    LocalDateTime.now()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            ApiResponse<CartViewDto> response = new ApiResponse<>(
                    e.getMessage(),
                    null,
                    HttpStatus.BAD_REQUEST.value(),
                    LocalDateTime.now()
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CartViewDto>> getCartByUserId(@PathVariable Long userId) {
        CartViewDto cart = cartService.getCartByUserId(userId);
        ApiResponse<CartViewDto> response = new ApiResponse<>(
                "Cart retrieved successfully.",
                cart,
                HttpStatus.OK.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/item/{cartItemId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> removeCartItem(@PathVariable Long cartItemId) {
        try {
            cartService.removeCartItem(cartItemId);
            ApiResponse<String> response = new ApiResponse<>(
                    "Item removed from cart successfully.",
                    null,
                    HttpStatus.OK.value(),
                    LocalDateTime.now()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            ApiResponse<String> response = new ApiResponse<>(
                    e.getMessage(),
                    null,
                    HttpStatus.BAD_REQUEST.value(),
                    LocalDateTime.now()
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}