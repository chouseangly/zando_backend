package com.example.zandobackend.repository;

import com.example.zandobackend.model.dto.VariantInsertDTO;
import com.example.zandobackend.model.entity.Category;
import com.example.zandobackend.model.entity.Product;
import com.example.zandobackend.model.entity.ProductVariant;
import com.example.zandobackend.model.entity.Size;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface ProductRepo {

    // ... (insertProductCategory, selectCategoriesByProductId, deleteProductCategoriesByProductId are unchanged)
    @Insert("INSERT INTO product_category(product_id, category_id) VALUES(#{productId}, #{categoryId})")
    void insertProductCategory(@Param("productId") Long productId, @Param("categoryId") Integer categoryId);
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
    @Results(id = "ProductResultMap", value = {
            @Result(property = "productId",       column = "product_id"),
            @Result(property = "basePrice",       column = "base_price"),
            @Result(property = "discountPercent", column = "discount_percent"),
            @Result(property = "isAvailable",     column = "is_available"),
            @Result(property = "views",           column = "views"), // ✅ ADD THIS MAPPING
            @Result(property = "variants",        column = "product_id",
                    many = @Many(select = "selectVariantsByProductId")),
            @Result(property = "categories",      column = "product_id",
                    many = @Many(select = "selectCategoriesByProductId"))
    })
    Product selectProductById(@Param("productId") Long productId);

    @Update("UPDATE product SET views = views + 1 WHERE product_id = #{productId}")
    void incrementViewCount(@Param("productId") Long productId);

    @Select("SELECT * FROM product")
    @ResultMap("ProductResultMap")
    List<Product> selectAllProducts();

    // ✅ MODIFIED: Switched to a dynamic SQL builder for partial updates
    @UpdateProvider(type = ProductSqlBuilder.class, method = "buildUpdateProductSql")
    void updateProduct(Product product);

    @Delete("DELETE FROM product WHERE product_id = #{productId}")
    void deleteProductById(@Param("productId") Long productId);

    // ... (rest of the file is unchanged)
    @Insert("INSERT INTO product_variant(product_id, color, quantity) VALUES(#{productId}, #{color}, #{quantity})")
    @Options(useGeneratedKeys = true, keyProperty = "variantId")
    void insertVariant(VariantInsertDTO variant);

    @Select("SELECT variant_id, product_id, color, uuid, quantity FROM product_variant WHERE product_id = #{productId}")
    @Results({
            @Result(property = "variantId", column = "variant_id"),
            @Result(property = "quantity",  column = "quantity"),
            @Result(property = "images",    column = "variant_id",
                    many = @Many(select = "selectImagesByVariantId")),
            @Result(property = "sizes",     column = "variant_id",
                    many = @Many(select = "selectSizesByVariantId"))
    })
    List<ProductVariant> selectVariantsByProductId(@Param("productId") Long productId);

    @Delete("DELETE FROM product_variant WHERE product_id = #{productId}")
    void deleteVariantsByProductId(@Param("productId") Long productId);

    @Select("SELECT size_id FROM size WHERE name = #{name}")
    Long getSizeIdByName(@Param("name") String name);

    @Insert("INSERT INTO size(name) VALUES(#{name})")
    @Options(useGeneratedKeys = true, keyProperty = "size_id", keyColumn = "size_id")
    void insertSize(Map<String, Object> params);

    @Insert("INSERT INTO variant_size(variant_id, size_id, is_available) VALUES(#{variantId}, #{sizeId}, TRUE)")
    void insertVariantSize(@Param("variantId") Long variantId, @Param("sizeId") Long sizeId);

    // ... other methods
    @Select("SELECT s.size_id, s.name FROM size s " +
            "JOIN variant_size vs ON s.size_id = vs.size_id " +
            "WHERE vs.variant_id = #{variantId} AND vs.is_available = TRUE")
    List<Size> selectSizesByVariantId(@Param("variantId") Long variantId);
// ...

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


    // ✅ ADDED: A dedicated class to build the dynamic SQL
    class ProductSqlBuilder {
        public String buildUpdateProductSql(final Product product) {
            return new org.apache.ibatis.jdbc.SQL() {{
                UPDATE("product");
                if (product.getName() != null) {
                    SET("name = #{name}");
                }
                if (product.getDescription() != null) {
                    SET("description = #{description}");
                }
                if (product.getBasePrice() != null) {
                    SET("base_price = #{basePrice}");
                }
                if (product.getDiscountPercent() != null) {
                    SET("discount_percent = #{discountPercent}");
                }
                if (product.getIsAvailable() != null) {
                    SET("is_available = #{isAvailable}");
                }
                WHERE("product_id = #{productId}");
            }}.toString();
        }
    }

    // ✅ ADDED: Query to calculate total sales count for a product
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM transaction_items WHERE product_id = #{productId}")
    Long selectTotalSalesForProduct(@Param("productId") Long productId);

    // ✅ ADDED: Query to calculate total earnings for a product
    @Select("SELECT COALESCE(SUM(price_at_purchase * quantity), 0) FROM transaction_items WHERE product_id = #{productId}")
    Double selectTotalEarningsForProduct(@Param("productId") Long productId);

}