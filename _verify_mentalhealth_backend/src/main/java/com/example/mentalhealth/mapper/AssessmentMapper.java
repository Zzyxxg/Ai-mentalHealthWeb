package com.example.mentalhealth.mapper;

import com.example.mentalhealth.entity.Assessment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AssessmentMapper {

    int insert(Assessment assessment);

    List<Assessment> selectByUser(@Param("userId") Long userId);

    List<Assessment> selectByUserAndType(@Param("userId") Long userId, @Param("scaleType") String scaleType);

    List<Assessment> selectAll(@Param("scaleType") String scaleType);
}

