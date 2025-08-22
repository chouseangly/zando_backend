package com.example.zandobackend.repository;



import com.example.zandobackend.model.entity.Size;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import java.util.Optional;

@Mapper
public interface SizeRepo {

    @Select("SELECT size_id as id, name FROM size WHERE name = #{name}")
    Optional<Size> findByName(String name);

    @Insert("INSERT INTO size (name) VALUES (#{name})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "size_id")
    void insertSize(Size size);
}