package com.example.zandobackend.repository;

import com.example.zandobackend.model.entity.CartItemDto;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface CartRepo {

    @Select("SELECT cart_id FROM shopping_cart WHERE user_id = #{userId}")
    Long findCartIdByUserId(Long userId);

    @Insert("INSERT INTO shopping_cart (user_id) VALUES (#{userId})")
    @Options(useGeneratedKeys = true, keyProperty = "cart_id", keyColumn = "cart_id")
    void createCart(Map<String, Object> params); // Use Map to get back the generated ID

    @Select("SELECT * FROM cart_item WHERE cart_id = #{cartId} AND product_id = #{productId} AND variant_id = #{variantId} AND size_id = #{sizeId}")
    @Results({
            @Result(property = "cartItemId", column = "cart_item_id")
    })
    CartItemDto findCartItem(@Param("cartId") Long cartId, @Param("productId") Long productId, @Param("variantId") Long variantId, @Param("sizeId") Long sizeId);


    @Insert("INSERT INTO cart_item (cart_id, product_id, variant_id, size_id, quantity) VALUES (#{cartId}, #{productId}, #{variantId}, #{sizeId}, #{quantity})")
    void addCartItem(@Param("cartId") Long cartId, @Param("productId") Long productId, @Param("variantId") Long variantId, @Param("sizeId") Long sizeId, @Param("quantity") int quantity);

    @Update("UPDATE cart_item SET quantity = #{quantity} WHERE cart_item_id = #{cartItemId}")
    void updateCartItemQuantity(@Param("cartItemId") Long cartItemId, @Param("quantity") int quantity);

    @Select("SELECT ci.*, p.*, pv.color FROM cart_item ci " +
            "JOIN product p ON ci.product_id = p.product_id " +
            "JOIN product_variant pv ON ci.variant_id = pv.variant_id " +
            "WHERE ci.cart_id = #{cartId}")
    @Results({
            @Result(property = "cartItemId", column = "cart_item_id"),
            @Result(property = "cartId", column = "cart_id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "variantId", column = "variant_id"),
            @Result(property = "sizeId", column = "size_id"),
            @Result(property = "quantity", column = "quantity"),
            @Result(property = "product", column = "product_id", one = @One(select = "com.example.zandobackend.repository.ProductRepo.selectProductById"))
    })
    List<CartItemDto> findCartItemsByCartId(Long cartId);


    @Delete("DELETE FROM cart_item WHERE cart_item_id = #{cartItemId}")
    void removeCartItem(Long cartItemId);
}
