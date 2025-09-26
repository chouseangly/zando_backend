package com.example.zandobackend.service;

import com.example.zandobackend.model.dto.TransactionRequest;
import com.example.zandobackend.model.entity.Transaction;
import java.util.List;

public interface TransactionService {
    Transaction createTransaction(TransactionRequest transactionRequest);
    List<Transaction> findAllTransactions();
    Transaction updateTransactionStatus(Long id, String status);
}