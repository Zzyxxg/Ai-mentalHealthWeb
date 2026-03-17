package com.example.mentalhealth.mapper;

import com.example.mentalhealth.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuditLogMapper {
    int insert(AuditLog log);

    List<AuditLog> selectList(@Param("userId") Long userId, @Param("action") String action);
}
