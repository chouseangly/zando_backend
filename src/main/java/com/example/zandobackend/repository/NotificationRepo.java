package com.example.zandobackend.repository;

import com.example.zandobackend.model.dto.NotificationFavorite;
import com.example.zandobackend.model.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationRepo {

    /**
     * ✅ FIX: Added the 'product_id' column to the INSERT statement.
     * This allows the notification to be created with its product link in one step.
     */
    @Insert("INSERT INTO notifications (user_id, product_id, title, content, icon_url) " +
            "VALUES (#{userId}, #{productId}, #{title}, #{content}, #{iconUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void createNotificationWithType(Notification notification);


    @Select("SELECT * FROM notifications WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Notification> getNotificationsByUserId(@Param("userId") Long userId);

    @Select("SELECT n.* FROM notifications n WHERE n.user_id = #{userId} ORDER BY n.created_at DESC")
    List<Notification> getAllNotificationsByUserId(@Param("userId") Long userId);

    @Update("UPDATE notifications SET is_read = true WHERE user_id = #{userId} AND id = #{id}")
    void markNotificationAsRead(@Param("userId") Long userId, @Param("id") Long id);

    @Select("SELECT p.description, up.profile_image FROM products p " +
            "LEFT JOIN user_profile up ON p.user_id = up.user_id " +
            "WHERE p.product_id = #{productId}")
    NotificationFavorite getFavoriteNotification(@Param("productId") Long productId);

    @Select("SELECT product_id FROM notifications WHERE id = #{id}")
    Long getProductIdByNoId(@Param("id") Long id);

    @Delete("DELETE FROM notifications WHERE product_id = #{productId}")
    void deleteAllNotificationsByProductId(@Param("productId") Long productId);

}