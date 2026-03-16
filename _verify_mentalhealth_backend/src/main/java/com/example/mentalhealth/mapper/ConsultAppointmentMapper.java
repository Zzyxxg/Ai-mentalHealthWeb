package com.example.mentalhealth.mapper;

import com.example.mentalhealth.entity.ConsultAppointment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ConsultAppointmentMapper {

    ConsultAppointment selectById(@Param("id") Long id);

    ConsultAppointment selectByUserAndIdempotencyKey(@Param("userId") Long userId, @Param("idempotencyKey") String idempotencyKey);

    List<ConsultAppointment> selectByUser(@Param("userId") Long userId, @Param("status") Integer status);

    int insert(ConsultAppointment appointment);

    int updateStatus(@Param("id") Long id, @Param("userId") Long userId, @Param("status") Integer status);
}
