package com.example.zandobackend.repository;

import com.example.zandobackend.model.dto.VariantInsertDTO;
import com.example.zandobackend.model.entity.Category;
import com.example.zandobackend.model.entity.Product;
import com.example.zandobackend.model.entity.ProductVariant;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface ProductRepo {

    // ------------------ Product Category ------------------

    @Insert("INSERT INTO product_category(product_id, category_id) VALUES(#{productId}, #{categoryId})")
    void insertProductCategory(@Param("productId") Long productId, @Param("categoryId") Integer categoryId);

    /**
     * FIX: The @Results block from CategoryRepo is copied here to define the recursive mapping.
     * This makes the 'categoryWithChildrenResult' map available locally.
     */
    @Select("SELECT c.* FROM category c " +
            "JOIN product_category pc ON c.category_id = pc.category_id " +
            "WHERE pc.product_id = #{productId}")
    @Results(id = "categoryWithChildrenResult", value = {
            @Result(property = "categoryId", column = "category_id"),
            @Result(property = "name", column = "name"),
            @Result(
                    property = "children",
                    column = "category_id",
                    javaType = Set.class,
                    // IMPORTANT: Use the fully qualified path to the method in CategoryRepo
                    many = @Many(select = "com.example.zandobackend.repository.CategoryRepo.findByParentId")
            )
    })
    List<Category> selectCategoriesByProductId(@Param("productId") Long productId);


    @Delete("DELETE FROM product_category WHERE product_id = #{productId}")
    void deleteProductCategoriesByProductId(@Param("productId") Long productId);

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

    @Select("SELECT p.* FROM product p " +
            "JOIN product_category pc ON p.product_id = pc.product_id " +
            "WHERE pc.category_id = #{categoryId}")
    @Results({
            @Result(property = "productId",       column = "product_id"),
            @Result(property = "basePrice",       column = "base_price"),
            @Result(property = "discountPercent", column = "discount_percent")
    })
    List<Product> selectProductsByCategoryId(@Param("categoryId") Integer categoryId);

}