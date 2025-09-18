package com.example.zandobackend.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DashboardRepo {

    // Note: We are querying the 'transactions' table now
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM transactions WHERE DATE(order_date) = CURRENT_DATE")
    Double findSalesToday();

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM transactions")
    Double findTotalEarning();

    @Select("SELECT COUNT(*) FROM transactions")
    Long findTotalOrders();

    @Select("SELECT COUNT(DISTINCT user_id) FROM transactions WHERE DATE(order_date) = CURRENT_DATE")
    Long findVisitorToday();
}