package com.example.mentalhealth.mapper;

import com.example.mentalhealth.entity.ConsultMessage;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ConsultMessageMapper {

    List<ConsultMessage> selectByThreadId(@Param("threadId") Long threadId);

    int insert(ConsultMessage message);

    int updateHidden(@Param("id") Long id, @Param("hidden") Boolean hidden, @Param("hiddenReason") String hiddenReason);
}
