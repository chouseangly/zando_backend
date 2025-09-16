package com.example.zandobackend.service.impl;

import com.example.zandobackend.model.dto.TransactionRequest;
import com.example.zandobackend.model.entity.Auth;
import com.example.zandobackend.model.entity.Product;
import com.example.zandobackend.model.entity.Transaction;
import com.example.zandobackend.model.entity.TransactionItem;
import com.example.zandobackend.repository.AuthRepo;
import com.example.zandobackend.repository.ProductRepo;
import com.example.zandobackend.repository.TransactionRepo;
import com.example.zandobackend.service.ProductService; // ✅ Import ProductService
import com.example.zandobackend.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepo transactionRepo;
    private final ProductRepo productRepo;
    private final AuthRepo authRepo;
    private final ProductService productService; // ✅ Add ProductService

    public TransactionServiceImpl(
            TransactionRepo transactionRepo,
            ProductRepo productRepo,
            AuthRepo authRepo,
            ProductService productService // ✅ Inject ProductService
    ) {
        this.transactionRepo = transactionRepo;
        this.productRepo = productRepo;
        this.authRepo = authRepo;
        this.productService = productService; // ✅ Initialize it
    }

    @Override
    @Transactional
    public Transaction createTransaction(TransactionRequest request) {
        Auth user = authRepo.findByUserId(request.getUserId());
        if (user == null) {
            throw new RuntimeException("User not found with id: " + request.getUserId());
        }

        Transaction transaction = new Transaction();
        transaction.setUserId(user.getUserId());
        transaction.setShippingAddress(request.getShippingAddress());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setStatus("Pending");

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var itemRequest : request.getItems()) {
            Product product = productRepo.selectProductById(itemRequest.getProductId());
            if (product == null) {
                throw new RuntimeException("Product not found with id: " + itemRequest.getProductId());
            }
            BigDecimal price = BigDecimal.valueOf(product.getBasePrice());
            BigDecimal quantity = BigDecimal.valueOf(itemRequest.getQuantity());
            totalAmount = totalAmount.add(price.multiply(quantity));
        }

        transaction.setTotalAmount(totalAmount);
        transactionRepo.insertTransaction(transaction);

        for (var itemRequest : request.getItems()) {
            Product product = productRepo.selectProductById(itemRequest.getProductId());
            TransactionItem item = new TransactionItem();
            item.setTransactionId(transaction.getId());
            item.setProductId(itemRequest.getProductId());
            item.setQuantity(itemRequest.getQuantity());
            item.setPriceAtPurchase(BigDecimal.valueOf(product.getBasePrice()));
            transactionRepo.insertTransactionItem(item);
        }

        return transactionRepo.findById(transaction.getId());
    }

    @Override
    public List<Transaction> findAllTransactions() {
        // 1. Fetch the basic transaction data
        List<Transaction> transactions = transactionRepo.findAll();

        // ✅ FIX: Loop through each transaction to enrich the product data
        for (Transaction transaction : transactions) {
            for (TransactionItem item : transaction.getItems()) {
                // 2. For each item, fetch the FULL product details (including gallery)
                // and replace the simple product object with the complete one.
                Product fullProductDetails = productService.getProductById(item.getProductId());
                item.setProduct(fullProductDetails);
            }
        }
        return transactions;
    }
}