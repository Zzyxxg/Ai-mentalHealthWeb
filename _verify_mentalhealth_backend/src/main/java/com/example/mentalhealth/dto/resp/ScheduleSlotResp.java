package com.example.mentalhealth.dto.resp;

import com.example.mentalhealth.enums.ScheduleSlotStatus;
import java.time.LocalDate;
import java.time.LocalTime;

public class ScheduleSlotResp {
    private Long id;
    private Long counselorUserId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private ScheduleSlotStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCounselorUserId() {
        return counselorUserId;
    }

    public void setCounselorUserId(Long counselorUserId) {
        this.counselorUserId = counselorUserId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public ScheduleSlotStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleSlotStatus status) {
        this.status = status;
    }
}
