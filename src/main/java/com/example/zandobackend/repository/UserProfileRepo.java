package com.example.zandobackend.repository;

import com.example.zandobackend.model.dto.UserProfileRequest;
import com.example.zandobackend.model.entity.UserProfile;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserProfileRepo {

    @Insert("""
        INSERT INTO user_profile (user_id, gender, phone_number, profile_image, birthday, address, user_name, first_name, last_name)
        VALUES (#{userId}, #{gender}, #{phoneNumber}, #{profileImage}, #{birthday}, #{address}, #{userName}, #{firstName}, #{lastName})
    """)
    void createUserProfileAfterVerify(UserProfile userProfile);

    @Insert("""
        INSERT INTO user_profile (user_id, gender, phone_number, profile_image,  birthday, address)
        VALUES (#{userId}, #{gender}, #{phoneNumber}, #{profileImage}, #{birthday}, #{address})
    """)
    void createUserProfile(UserProfileRequest request);

    @Update("""
        UPDATE user_profile
        SET gender = #{gender}, phone_number = #{phoneNumber}, profile_image = #{profileImage}, birthday = #{birthday}, address = #{address},
            user_name = #{userName}, first_name = #{firstName}, last_name = #{lastName}
        WHERE user_id = #{userId}
    """)
    void updateUserProfile(UserProfileRequest request);

    @Select("SELECT * FROM user_profile WHERE user_id = #{userId}")
    @Results({
            @Result(property = "profileId", column = "profile_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "phoneNumber", column = "phone_number"),
            @Result(property = "profileImage", column = "profile_image"),
            @Result(property = "userName", column = "user_name"),
            @Result(property = "firstName", column = "first_name"),
            @Result(property = "lastName", column = "last_name")
    })
    UserProfile getProfileByUserId(Long userId);

    @Delete("DELETE FROM user_profile WHERE user_id = #{userId}")
    int deleteProfile(Long userId);

    @Select("SELECT * FROM user_profile")
    @Results({
            @Result(property = "profileId", column = "profile_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "phoneNumber", column = "phone_number"),
            @Result(property = "profileImage", column = "profile_image"),
            @Result(property = "userName", column = "user_name"),
            @Result(property = "firstName", column = "first_name"),
            @Result(property = "lastName", column = "last_name")
    })
    List<UserProfile> getUserProfiles();

    @Select("SELECT COUNT(*) > 0 FROM user_profile WHERE user_id = #{userId}")
    boolean existsByUserId(@Param("userId") Long userId);
}