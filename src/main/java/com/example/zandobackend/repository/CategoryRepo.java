package com.example.zandobackend.repository;

import com.example.zandobackend.model.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryRepo {

    @Select("SELECT * FROM category WHERE parent_id IS NULL")
    @Results({
            @Result(property = "categoryId", column = "category_id"),
            // MyBatis automatically maps 'name' to 'name'
    })
    List<Category> findByParentIsNull();

    // IMPORTANT: See note below about fetching children
}