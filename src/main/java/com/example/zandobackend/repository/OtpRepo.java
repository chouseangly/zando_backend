package com.example.zandobackend.repository;


import com.example.zandobackend.model.entity.Otp;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OtpRepo {

    @Insert("INSERT INTO otp_number (email, otp, created_at, verified) VALUES (#{email}, #{otp}, #{createdAt}, #{verified})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @ResultMap("OtpMapper")
    void saveOtp(Otp otp);


    @Select("SELECT * FROM otp_number WHERE email = #{email} ORDER BY created_at DESC LIMIT 1")
    @Results(id = "OtpMapper" , value = {
            @Result(property = "id", column = "id"),
            @Result(property = "email", column = "email"),
            @Result(property = "otp", column = "otp"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "verified", column = "verified")
    })
    Otp findLatestByEmail(String email);


    @Update("UPDATE otp_number SET verified = true WHERE id = #{id}")
    @ResultMap("OtpMapper")
    void markOtpAsVerified(Integer id);
}




