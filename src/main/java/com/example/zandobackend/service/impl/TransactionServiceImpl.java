package com.example.zandobackend.service.impl;

import com.example.zandobackend.model.dto.TransactionRequest;
import com.example.zandobackend.model.entity.*;
import com.example.zandobackend.repository.AuthRepo;
import com.example.zandobackend.repository.ProductRepo;
import com.example.zandobackend.repository.TransactionRepo;
import com.example.zandobackend.service.AuthService; // ✅ IMPORT AuthService
import com.example.zandobackend.service.NotificationService;
import com.example.zandobackend.service.ProductService;
import com.example.zandobackend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepo transactionRepo;
    private final ProductRepo productRepo;
    private final AuthRepo authRepo;
    private final ProductService productService;
    private final NotificationService notificationService;
    private final AuthService authService; // ✅ INJECT AuthService

    @Override
    @Transactional
    public Transaction createTransaction(TransactionRequest request) {
        Auth user = authRepo.findByUserId(request.getUserId());
        if (user == null) {
            throw new RuntimeException("User not found with id: " + request.getUserId());
        }

        // ... (existing transaction creation logic)
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
        // ... (end of existing logic)

        // Notify the user who placed the order
        Notification userNotification = Notification.builder()
                .userId(user.getUserId())
                .title("New Order Placed")
                .content("Your order #" + transaction.getId() + " has been successfully placed.")
                .iconUrl("https://gateway.pinata.cloud/ipfs/your-order-icon-hash") // Placeholder icon
                .build();
        notificationService.createNotificationWithType(userNotification);

        // ✅ ADDED: Notify all admins about the new order
        List<Auth> admins = authService.findAllAdmins();
        for (Auth admin : admins) {
            Notification adminNotification = Notification.builder()
                    .userId(admin.getUserId())
                    .title("New Order Received")
                    .content("A new order #" + transaction.getId() + " was placed by " + user.getUserName() + ".")
                    .iconUrl("https://gateway.pinata.cloud/ipfs/your-admin-order-icon-hash") // Placeholder
                    .build();
            notificationService.createNotificationWithType(adminNotification);
        }

        return transactionRepo.findById(transaction.getId());
    }

    @Override
    public List<Transaction> findAllTransactions() {
        List<Transaction> transactions = transactionRepo.findAll();
        for (Transaction transaction : transactions) {
            for (TransactionItem item : transaction.getItems()) {
                Product fullProductDetails = productService.getProductById(item.getProductId());
                item.setProduct(fullProductDetails);
            }
        }
        return transactions;
    }

    @Override
    @Transactional
    public Transaction updateTransactionStatus(Long id, String status) {
        Transaction transaction = transactionRepo.findById(id);
        if (transaction == null) {
            throw new RuntimeException("Transaction not found with id: " + id);
        }
        transactionRepo.updateStatus(id, status);
        return transactionRepo.findById(id);
    }
}