package com.example.zandobackend.service.impl;

import com.example.zandobackend.model.dto.NotificationFavorite;
import com.example.zandobackend.model.dto.NotificationRequest;
import com.example.zandobackend.model.entity.Notification;
import com.example.zandobackend.repository.NotificationRepo;
import com.example.zandobackend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ✅ Import Transactional

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2 // Added for logging
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepo notificationRepo;

    @Override
    public Notification createNotification(NotificationRequest request) {
        // This method seems unused but we'll leave it
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setContent(request.getContent());
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepo.createNotificationWithType(notification);
        return notification;
    }

    @Override
    public Notification getNotificationByUserId(Long userId) {
        List<Notification> notifications = notificationRepo.getNotificationsByUserId(userId);
        return notifications.isEmpty() ? null : notifications.get(0);
    }

    @Override
    public List<Notification> getAllNotificationsByUserId(Long userId) {
        return notificationRepo.getAllNotificationsByUserId(userId);
    }

    /**
     * ✅ FIX: Added @Transactional to ensure the database commit happens.
     * Also checks if the update was successful.
     */
    @Override
    @Transactional
    public void markNotificationAsRead(Long userId, Long notificationId) {
        int updatedRows = notificationRepo.markNotificationAsRead(userId, notificationId);
        if (updatedRows == 0) {
            log.warn("Attempted to mark notification as read, but no rows were updated. UserId: {}, NotificationId: {}", userId, notificationId);
        }
    }

    @Override
    @Transactional
    public void createNotificationWithType(Notification notification) {
        notificationRepo.createNotificationWithType(notification);
    }

    @Override
    public NotificationFavorite favoriteNotification(Long productId) {
        return notificationRepo.getFavoriteNotification(productId);
    }

    @Override
    public Long insertproductId(Long productId, Long id) {
        return notificationRepo.getProductIdByNoId(productId);
    }

    @Override
    public Long getProductIdByNoId(Long id) {
        return notificationRepo.getProductIdByNoId(id);
    }

    @Override
    @Transactional
    public void deleteAllNotificationByProductId(Long productId) {
        notificationRepo.deleteAllNotificationsByProductId(productId);
    }
}