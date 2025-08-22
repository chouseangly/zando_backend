package com.example.zandobackend.repository;

import com.example.zandobackend.model.entity.Size;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.Optional;

@Mapper
public interface SizeRepo {
    @Select("SELECT * FROM size WHERE name = #{name}")
    Optional<Size> findByName(String name);
}