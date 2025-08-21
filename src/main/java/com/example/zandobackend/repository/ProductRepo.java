package com.example.zandobackend.repository;

import com.example.zandobackend.model.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductRepo {
    @Select("""
select * from product
""")
    List<Product> getProducts(Product product);
}
