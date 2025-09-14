package com.example.zandobackend.service.impl;

import com.example.zandobackend.model.dto.DashboardDto;
import com.example.zandobackend.repository.DashboardRepo;
import com.example.zandobackend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepo dashboardRepo;

    @Override
    public DashboardDto getDashboardStats() {
        DashboardDto stats = new DashboardDto();
        stats.setSalesToday(dashboardRepo.findSalesToday());
        stats.setTotalEarning(dashboardRepo.findTotalEarning());
        stats.setTotalOrders(dashboardRepo.findTotalOrders());
        stats.setVisitorToday(dashboardRepo.findVisitorToday());
        return stats;
    }
}
