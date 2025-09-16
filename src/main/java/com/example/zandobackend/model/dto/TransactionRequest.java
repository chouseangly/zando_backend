package com.example.zandobackend.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class TransactionRequest {
    private Long userId;
    private String shippingAddress;
    private String paymentMethod;
    private List<TransactionItemRequest> items;
}