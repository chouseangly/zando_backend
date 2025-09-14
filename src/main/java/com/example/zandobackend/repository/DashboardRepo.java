package com.example.zandobackend.repository;

import com.example.zandobackend.model.dto.DashboardDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DashboardRepo {

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE DATE(created_at) = CURRENT_DATE")
    Double findSalesToday();

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM orders")
    Double findTotalEarning();

    @Select("SELECT COUNT(*) FROM orders")
    Long findTotalOrders();

    @Select("SELECT COUNT(DISTINCT user_id) FROM orders WHERE DATE(created_at) = CURRENT_DATE")
    Long findVisitorToday();
}
