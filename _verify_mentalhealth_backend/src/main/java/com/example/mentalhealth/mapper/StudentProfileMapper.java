package com.example.mentalhealth.mapper;

import com.example.mentalhealth.entity.StudentProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StudentProfileMapper {
    int insert(StudentProfile studentProfile);
    StudentProfile selectByUserId(@Param("userId") Long userId);
}
