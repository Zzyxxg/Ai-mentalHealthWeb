package com.example.mentalhealth.mapper;

import com.example.mentalhealth.entity.ConsultThread;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ConsultThreadMapper {

    ConsultThread selectById(@Param("id") Long id);

    List<ConsultThread> selectByStudent(@Param("studentUserId") Long studentUserId);

    List<ConsultThread> selectByCounselor(@Param("counselorUserId") Long counselorUserId, @Param("status") String status);

    List<ConsultThread> selectAll();

    int insert(ConsultThread thread);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int updateHidden(@Param("id") Long id, @Param("hidden") Boolean hidden, @Param("hiddenReason") String hiddenReason);
}
