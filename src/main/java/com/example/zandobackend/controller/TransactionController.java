package com.example.zandobackend.controller;

import com.example.zandobackend.model.dto.ApiResponse;
import com.example.zandobackend.model.dto.TransactionRequest;
import com.example.zandobackend.model.entity.Transaction;
import com.example.zandobackend.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@CrossOrigin(origins = "http://localhost:3000")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Transaction>> createTransaction(@RequestBody TransactionRequest request) {
        try {
            Transaction newTransaction = transactionService.createTransaction(request);
            ApiResponse<Transaction> response = new ApiResponse<>("Transaction created successfully", newTransaction, HttpStatus.CREATED.value(), LocalDateTime.now());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            ApiResponse<Transaction> response = new ApiResponse<>(e.getMessage(), null, HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Transaction>>> getAllTransactions() {
        List<Transaction> transactions = transactionService.findAllTransactions();
        ApiResponse<List<Transaction>> response = new ApiResponse<>("Transactions retrieved successfully", transactions, HttpStatus.OK.value(), LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}