package com.example.zandobackend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
    private Transaction transaction;
    private Product product;
    private Integer quantity;
    private BigDecimal priceAtPurchase;
}