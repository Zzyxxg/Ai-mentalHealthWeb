package com.example.mentalhealth.mapper;

import com.example.mentalhealth.entity.ScheduleSlot;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ScheduleSlotMapper {

    ScheduleSlot selectById(@Param("id") Long id);

    List<ScheduleSlot> selectByCounselorAndDateRange(@Param("counselorUserId") Long counselorUserId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    ScheduleSlot selectByCounselorAndExactTime(@Param("counselorUserId") Long counselorUserId,
                                               @Param("date") LocalDate date,
                                               @Param("startTime") java.time.LocalTime startTime,
                                               @Param("endTime") java.time.LocalTime endTime);

    List<ScheduleSlot> selectAvailableByCounselorAndTime(@Param("counselorUserId") Long counselorUserId,
                                                         @Param("date") LocalDate date,
                                                         @Param("startTime") java.time.LocalTime startTime,
                                                         @Param("endTime") java.time.LocalTime endTime);

    List<Long> selectCounselorIdsWithAvailableSlots(@Param("counselorUserIds") List<Long> counselorUserIds,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate,
                                                    @Param("today") LocalDate today,
                                                    @Param("nowTime") LocalTime nowTime);

    int batchInsert(@Param("slots") List<ScheduleSlot> slots);

    int updateStatusIfCurrent(@Param("id") Long id, @Param("currentStatus") String currentStatus, @Param("targetStatus") String targetStatus);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int updateTimeAndStatus(@Param("id") Long id, 
                            @Param("startTime") java.time.LocalTime startTime, 
                            @Param("endTime") java.time.LocalTime endTime, 
                            @Param("status") String status);

    int deleteById(@Param("id") Long id);
}
