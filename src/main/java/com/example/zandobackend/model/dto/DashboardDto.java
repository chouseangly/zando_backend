package com.example.zandobackend.model.dto;

import lombok.Data;

@Data
public class DashboardDto {
    private Double salesToday;
    private Double totalEarning;
    private Long totalOrders;
    private Long visitorToday;
}
