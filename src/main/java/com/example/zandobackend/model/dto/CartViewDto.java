package com.example.zandobackend.model.dto;

import com.example.zandobackend.model.entity.CartItemDto;
import lombok.Data;

import java.util.List;

@Data
public class CartViewDto {
    private Long cartId;
    private Long userId;
    private List<CartItemDto> items;
}