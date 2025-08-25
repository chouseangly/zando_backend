package com.example.zandobackend.repository;

import com.example.zandobackend.model.dto.VariantInsertDTO;
import com.example.zandobackend.model.entity.Product;
import com.example.zandobackend.model.entity.ProductVariant;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProductRepo {

    // ------------------ Product ------------------

    @Insert("INSERT INTO product(name, description, base_price, discount_percent) " +
            "VALUES(#{name}, #{description}, #{basePrice}, #{discountPercent})")
    @Options(useGeneratedKeys = true, keyProperty = "productId")
    void insertProduct(Product product);

    @Select("SELECT * FROM product WHERE product_id = #{productId}")
    Product selectProductById(@Param("productId") Long productId);

    @Select("SELECT * FROM product")
    @Results({
            @Result(property = "productId",       column = "product_id"),
            @Result(property = "basePrice",       column = "base_price"),
            @Result(property = "discountPercent", column = "discount_percent")
    })
    List<Product> selectAllProducts();

    @Update("UPDATE product SET " +
            "name = #{name}, " +
            "description = #{description}, " +
            "base_price = #{basePrice}, " +
            "discount_percent = #{discountPercent} " +
            "WHERE product_id = #{productId}")
    void updateProduct(Product product);

    @Delete("DELETE FROM product WHERE product_id = #{productId}")
    void deleteProductById(@Param("productId") Long productId);


    // ------------------ Variant ------------------

    @Insert("INSERT INTO product_variant(product_id, color) VALUES(#{productId}, #{color})")
    @Options(useGeneratedKeys = true, keyProperty = "variantId")
    void insertVariant(VariantInsertDTO variant);

    @Select("SELECT variant_id, product_id, color, uuid FROM product_variant WHERE product_id = #{productId}")
    List<ProductVariant> selectVariantsByProductId(@Param("productId") Long productId);

    @Delete("DELETE FROM product_variant WHERE product_id = #{productId}")
    void deleteVariantsByProductId(@Param("productId") Long productId);

    // ------------------ Size ------------------

    @Select("SELECT size_id FROM size WHERE name = #{name}")
    Long getSizeIdByName(@Param("name") String name);

    @Insert("INSERT INTO size(name) VALUES(#{name})")
    @Options(useGeneratedKeys = true, keyProperty = "size_id", keyColumn = "size_id")
    void insertSize(Map<String, Object> params);

    // ------------------ Variant Size ------------------

    @Insert("INSERT INTO variant_size(variant_id, size_id, is_available) VALUES(#{variantId}, #{sizeId}, TRUE)")
    void insertVariantSize(@Param("variantId") Long variantId, @Param("sizeId") Long sizeId);

    @Select("SELECT s.name FROM size s " +
            "JOIN variant_size vs ON s.size_id = vs.size_id " +
            "WHERE vs.variant_id = #{variantId} AND vs.is_available = TRUE")
    List<String> selectSizesByVariantId(@Param("variantId") Long variantId);

    // ------------------ Images ------------------

    @Insert("INSERT INTO product_image(variant_id, image_url) VALUES(#{variantId}, #{imageUrl})")
    void insertImage(@Param("variantId") Long variantId, @Param("imageUrl") String imageUrl);

    @Select("SELECT image_url FROM product_image WHERE variant_id = #{variantId}")
    List<String> selectImagesByVariantId(@Param("variantId") Long variantId);

}