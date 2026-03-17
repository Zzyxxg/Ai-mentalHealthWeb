package com.example.mentalhealth.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminStatsMapper {

    List<Map<String, Object>> dailyAppointments(@Param("days") int days);

    List<Map<String, Object>> dailyThreads(@Param("days") int days);

    List<Map<String, Object>> dailyAssessments(@Param("days") int days);

    List<Map<String, Object>> assessmentLevelDist();
}

