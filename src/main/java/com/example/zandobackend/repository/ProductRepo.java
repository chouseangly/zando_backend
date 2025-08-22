package com.example.zandobackend.repository;

import com.example.zandobackend.model.entity.Product;
import com.example.zandobackend.model.entity.ProductImage;
import com.example.zandobackend.model.entity.ProductVariant;
import com.example.zandobackend.model.entity.VariantSize;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ProductRepo {



    @Insert("""
        INSERT INTO product (name, description, base_price, discount_percent)
        VALUES (#{name}, #{description}, #{originalPrice}, #{discount})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "product_id")
    void insertProduct(Product product);

    @Insert("""
        INSERT INTO product_variant (product_id, color)
        VALUES (#{productId}, #{color})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "variant_id")
    void insertProductVariant(ProductVariant variant);

    @Insert("""
        INSERT INTO product_image (variant_id, image_url)
        VALUES (#{variantId}, #{imageUrl})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "image_id")
    void insertProductImage(ProductImage image);

    @Insert("""
        INSERT INTO variant_size (variant_id, size_id, is_available)
        VALUES (#{variantId}, #{sizeId}, #{isAvailable})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertVariantSize(VariantSize variantSize);


    // =================================================================
    // ==                       READ OPERATIONS                       ==
    // =================================================================

    /**
     * This is the main method to fetch all products.
     * It uses a complex @Results mapping that calls other methods in this interface
     * (e.g., findVariantsByProductId) to build the nested object structure.
     */
    @Select("SELECT * FROM product")
    @Results({
            @Result(property = "id", column = "product_id"),
            @Result(property = "name", column = "name"),
            @Result(property = "description", column = "description"),
            @Result(property = "originalPrice", column = "base_price"),
            @Result(property = "discount", column = "discount_percent"),
            @Result(property = "price", column = "final_price"),
            @Result(property = "variants", column = "product_id",
                    many = @Many(select = "findVariantsByProductId"))
    })
    List<Product> findAllProducts();


    // =================================================================
    // ==        HELPER METHODS FOR NESTED SELECTS (Sub-queries)      ==
    // =================================================================

    /**
     * Helper method to fetch variants for a specific product ID.
     * This is called by the @Many select in findAllProducts.
     */
    @Select("SELECT * FROM product_variant WHERE product_id = #{productId}")
    @Results({
            @Result(property = "id", column = "variant_id"),
            @Result(property = "color", column = "color"),
            @Result(property = "images", column = "variant_id",
                    many = @Many(select = "findImagesByVariantId")),
            @Result(property = "variantSizes", column = "variant_id",
                    many = @Many(select = "findVariantSizesByVariantId"))
    })
    List<ProductVariant> findVariantsByProductId(Integer productId);

    /**
     * Helper method to fetch images for a specific variant ID.
     */
    @Select("SELECT image_id as id, image_url as imageUrl FROM product_image WHERE variant_id = #{variantId}")
    List<ProductImage> findImagesByVariantId(Integer variantId);

    /**
     * Helper method to fetch sizes for a specific variant ID, including the size name.
     */
    @Select("""
        SELECT vs.id, vs.is_available, s.size_id, s.name
        FROM variant_size vs
        JOIN size s ON vs.size_id = s.size_id
        WHERE vs.variant_id = #{variantId}
    """)
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "isAvailable", column = "is_available"),
            @Result(property = "size.id", column = "size_id"),
            @Result(property = "size.name", column = "name")
    })
    List<VariantSize> findVariantSizesByVariantId(Integer variantId);
}