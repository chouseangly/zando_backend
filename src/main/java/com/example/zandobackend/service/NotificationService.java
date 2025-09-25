package com.example.zandobackend.service;




import com.example.zandobackend.model.dto.NotificationFavorite;
import com.example.zandobackend.model.dto.NotificationRequest;
import com.example.zandobackend.model.entity.Notification;

import java.util.List;

public interface NotificationService {
    Notification createNotification(NotificationRequest request);

    Notification getNotificationByUserId(Long userId);
    List<Notification> getAllNotificationsByUserId(Long userId);
    void markNotificationAsRead(Long userId, Long id);
    void createNotificationWithType(Notification notification);
    NotificationFavorite favoriteNotification(Long productId);
    Long insertproductId(Long productId, Long id);
    Long getProductIdByNoId(Long id);
    void deleteAllNotificationByProductId(Long productId);
}
