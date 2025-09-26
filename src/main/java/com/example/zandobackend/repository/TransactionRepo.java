package com.example.zandobackend.repository;

import com.example.zandobackend.model.entity.Transaction;
import com.example.zandobackend.model.entity.TransactionItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TransactionRepo {

    @Insert("INSERT INTO transactions (user_id, total_amount, status, shipping_address, payment_method) " +
            "VALUES (#{userId}, #{totalAmount}, #{status}, #{shippingAddress}, #{paymentMethod})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertTransaction(Transaction transaction);

    @Insert("INSERT INTO transaction_items (transaction_id, product_id, quantity, price_at_purchase) " +
            "VALUES (#{transactionId}, #{productId}, #{quantity}, #{priceAtPurchase})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertTransactionItem(TransactionItem transactionItem);

    @Select("SELECT * FROM transactions ORDER BY order_date DESC")
    @ResultMap("transactionResultMap")
    List<Transaction> findAll();

    @Select("SELECT * FROM transaction_items WHERE transaction_id = #{transactionId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "transactionId", column = "transaction_id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "quantity", column = "quantity"),
            @Result(property = "priceAtPurchase", column = "price_at_purchase"),
            @Result(property = "product", column = "product_id",
                    one = @One(select = "com.example.zandobackend.repository.ProductRepo.selectProductById"))
    })
    List<TransactionItem> findItemsByTransactionId(Long transactionId);

    @Update("UPDATE transactions SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    @Select("SELECT * FROM transactions WHERE id = #{id}")
    @Results(id = "transactionResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "totalAmount", column = "total_amount"),
            @Result(property = "status", column = "status"),
            @Result(property = "shippingAddress", column = "shipping_address"),
            @Result(property = "paymentMethod", column = "payment_method"),
            @Result(property = "orderDate", column = "order_date"),
            @Result(property = "user", column = "user_id",
                    one = @One(select = "com.example.zandobackend.repository.AuthRepo.findByUserId")),
            @Result(property = "items", column = "id",
                    many = @Many(select = "findItemsByTransactionId"))
    })
    Transaction findById(Long id);



}