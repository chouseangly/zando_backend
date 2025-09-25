package com.example.zandobackend.repository;

import com.example.zandobackend.model.dto.NotificationFavorite;
import com.example.zandobackend.model.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationRepo {

    @Insert("INSERT INTO notifications (user_id, product_id, title, content, icon_url) " +
            "VALUES (#{userId}, #{productId}, #{title}, #{content}, #{iconUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void createNotificationWithType(Notification notification);

    @Select("SELECT * FROM notifications WHERE user_id = #{userId} ORDER BY created_at DESC")
    // ✅ FIX: Added an 'id' to the @Results annotation to create a reusable mapping.
    @Results(id = "notificationResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "isRead", column = "is_read"),
            @Result(property = "iconUrl", column = "icon_url"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<Notification> getNotificationsByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM notifications WHERE user_id = #{userId} ORDER BY created_at DESC")
    // ✅ FIX: Changed the reference to the new, correct ID 'notificationResultMap'.
    @ResultMap("notificationResultMap")
    List<Notification> getAllNotificationsByUserId(@Param("userId") Long userId);

    @Update("UPDATE notifications SET is_read = true, updated_at = NOW() WHERE user_id = #{userId} AND id = #{notificationId}")
    int markNotificationAsRead(@Param("userId") Long userId, @Param("notificationId") Long notificationId);

    @Select("SELECT p.description, up.profile_image FROM products p " +
            "LEFT JOIN user_profile up ON p.user_id = up.user_id " +
            "WHERE p.product_id = #{productId}")
    NotificationFavorite getFavoriteNotification(@Param("productId") Long productId);

    @Select("SELECT product_id FROM notifications WHERE id = #{id}")
    Long getProductIdByNoId(@Param("id") Long id);

    @Delete("DELETE FROM notifications WHERE product_id = #{productId}")
    void deleteAllNotificationsByProductId(@Param("productId") Long productId);
}