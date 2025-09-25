package com.example.zandobackend.repository;

import com.example.zandobackend.model.dto.FavoriteRequest;
import com.example.zandobackend.model.entity.Favorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FavoriteRepo {

    @Insert("INSERT INTO favorite (user_id, product_id, created_at) VALUES (#{userId}, #{productId}, NOW())")
    void addFavorite(FavoriteRequest favoriteRequest);

    @Delete("DELETE FROM favorite WHERE user_id = #{userId} AND product_id = #{productId}")
    void removeFavorite(@Param("userId") Long userId, @Param("productId") Long productId);

    @Select("SELECT * FROM favorite WHERE user_id = #{userId}")
    @Results({
            @Result(property = "favoriteId", column = "favorite_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "product", column = "product_id",
                    one = @One(select = "com.example.zandobackend.repository.ProductRepo.selectProductById"))
    })
    List<Favorite> findFavoritesByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM favorite WHERE user_id = #{userId} AND product_id = #{productId}")
    int isFavorite(@Param("userId") Long userId, @Param("productId") Long productId);

    // ✅ ADDED: New method to find a single favorite after it's created
    @Select("SELECT * FROM favorite WHERE user_id = #{userId} AND product_id = #{productId}")
    @Results({
            @Result(property = "favoriteId", column = "favorite_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "product", column = "product_id",
                    one = @One(select = "com.example.zandobackend.repository.ProductRepo.selectProductById"))
    })
    Favorite findFavoriteByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);
}