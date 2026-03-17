package com.example.mentalhealth.dto.resp;

public class ConsultAppointmentResp {

    private Long id;
    private Long userId;
    private Long counselorUserId;
    private Long slotId;
    private Long startTime;
    private Integer durationMinutes;
    private String status;
    private String note;

    public ConsultAppointmentResp(Long id, Long userId, Long counselorUserId, Long slotId, Long startTime, Integer durationMinutes, String status, String note) {
        this.id = id;
        this.userId = userId;
        this.counselorUserId = counselorUserId;
        this.slotId = slotId;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCounselorUserId() {
        return counselorUserId;
    }

    public void setCounselorUserId(Long counselorUserId) {
        this.counselorUserId = counselorUserId;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
