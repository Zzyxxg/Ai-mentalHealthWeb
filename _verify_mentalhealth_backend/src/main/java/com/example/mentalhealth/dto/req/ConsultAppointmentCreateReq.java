package com.example.mentalhealth.dto.req;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ConsultAppointmentCreateReq {

    @NotNull(message = "咨询师ID不能为空")
    private Long counselorUserId;

    @NotNull(message = "开始时间不能为空")
    private Long startTime;

    @NotNull(message = "咨询时长不能为空")
    private Integer durationMinutes;

    private String note;

    public Long getCounselorUserId() {
        return counselorUserId;
    }

    public void setCounselorUserId(Long counselorUserId) {
        this.counselorUserId = counselorUserId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
