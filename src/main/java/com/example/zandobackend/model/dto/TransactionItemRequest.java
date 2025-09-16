package com.example.zandobackend.model.dto;

import lombok.Data;

@Data
public class TransactionItemRequest {
    private Long productId;
    private Integer quantity;
}