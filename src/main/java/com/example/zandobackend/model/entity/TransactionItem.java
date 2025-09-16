package com.example.zandobackend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionItem {

    private Long id;
    private Long transactionId;
    private Long productId;
    private Integer quantity;
    private BigDecimal priceAtPurchase;

    // This field will be populated by the MyBatis mapper
    private Product product;
}