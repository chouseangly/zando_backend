package com.example.zandobackend.repository;

import com.example.zandobackend.model.entity.*;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mapper
public interface ProductRepo {

    // Main Result Map to build a complete Product object with all its nested children
    @Results(id = "productResultMap", value = {
            @Result(property = "productId", column = "product_id"),
            @Result(property = "name", column = "name"),
            @Result(property = "description", column = "description"),
            @Result(property = "basePrice", column = "base_price"),
            @Result(property = "discountPercent", column = "discount_percent"),
            @Result(property = "finalPrice", column = "final_price"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "variants", column = "product_id",
                    javaType = List.class,
                    many = @Many(select = "findVariantsByProductId"))
    })
    @Select("SELECT * FROM product")
    List<Product> findAll();

    @Select("SELECT * FROM product WHERE product_id = #{productId}")
    @ResultMap("productResultMap")
    Optional<Product> findById(Integer productId);

    // Sub-query to find variants for a given product
    @Results(id = "variantResultMap", value = {
            @Result(property = "variantId", column = "variant_id"),
            @Result(property = "color", column = "color"),
            @Result(property = "images", column = "variant_id",
                    javaType = List.class,
                    many = @Many(select = "findImagesByVariantId")),
            @Result(property = "availableSizes", column = "variant_id",
                    javaType = List.class,
                    many = @Many(select = "findVariantSizesByVariantId"))
    })
    @Select("SELECT * FROM product_variant WHERE product_id = #{productId}")
    List<ProductVariant> findVariantsByProductId(Integer productId);

    // Sub-query to find images for a given variant
    @Results(id = "imageResultMap", value = {
            @Result(property = "imageId", column = "image_id"),
            @Result(property = "imageUrl", column = "image_url")
    })
    @Select("SELECT * FROM product_image WHERE variant_id = #{variantId}")
    List<ProductImage> findImagesByVariantId(Integer variantId);

    // Sub-query to find sizes for a given variant
    @Select("SELECT * FROM variant_size WHERE variant_id = #{variantId}")
    List<VariantSize> findVariantSizesByVariantId(Integer variantId);

    // INSERT Statements
    @Insert("INSERT INTO product (name, description, base_price, discount_percent) VALUES (#{name}, #{description}, #{basePrice}, #{discountPercent})")
    @Options(useGeneratedKeys = true, keyProperty = "productId", keyColumn = "product_id")
    void insertProduct(Product product);

    @Insert("INSERT INTO product_variant (product_id, color) VALUES (#{productId}, #{color})")
    @Options(useGeneratedKeys = true, keyProperty = "variantId", keyColumn = "variant_id")
    void insertVariant(ProductVariant variant);

    @Insert("INSERT INTO product_image (variant_id, image_url) VALUES (#{variantId}, #{imageUrl})")
    void insertImage(ProductImage image);

    @Insert("INSERT INTO variant_size (variant_id, size_id, is_available) VALUES (#{variantId}, #{sizeId}, #{isAvailable})")
    void insertVariantSize(VariantSize variantSize);

    // UPDATE Statements
    @Update("UPDATE product SET name=#{name}, description=#{description}, base_price=#{basePrice}, discount_percent=#{discountPercent} WHERE product_id=#{productId}")
    void updateProduct(Product product);

    // DELETE Statements
    @Delete("DELETE FROM product WHERE product_id = #{productId}")
    void deleteById(Integer productId);

    // You can add more specific variant/image deletion queries here if needed
    @Delete("DELETE FROM product_variant WHERE product_id = #{productId}")
    void deleteVariantsByProductId(Integer productId);

}