package com.example.zandobackend.repository;

import com.example.zandobackend.model.entity.Category;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Set;

@Mapper
public interface CategoryRepo {

    /**
     * Finds all top-level categories and uses the "categoryWithChildrenResult" resultMap
     * to recursively fetch all their children.
     */
    @Select("SELECT * FROM category WHERE parent_id IS NULL")
    @ResultMap("categoryWithChildrenResult") // Reference the map by its ID
    List<Category> findByParentIsNull();

    /**
     * Finds all direct children for a given parent ID. This method defines the complete
     * mapping for a Category object, including the recursive call to fetch its own children.
     */
    @Select("SELECT * FROM category WHERE parent_id = #{parentId}")
    @Results(id = "categoryWithChildrenResult", value = {
            @Result(property = "categoryId", column = "category_id"),
            @Result(property = "name", column = "name"),
            // This @Many annotation is the key. It tells MyBatis to call the
            // 'findByParentId' method for each category it finds, using the
            // 'category_id' as the parameter to fetch the children.
            @Result(
                    property = "children",
                    column = "category_id",
                    javaType = Set.class,
                    many = @Many(select = "findByParentId")
            )
    })
    Set<Category> findByParentId(Integer parentId);
}