package com.example.mentalhealth.mapper;

import com.example.mentalhealth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User selectByUsername(@Param("username") String username);

    User selectById(@Param("id") Long id);

    java.util.List<User> selectList(@Param("role") String role, @Param("keyword") String keyword);

    int insert(User user);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int updatePasswordHash(@Param("id") Long id, @Param("passwordHash") String passwordHash);
}
