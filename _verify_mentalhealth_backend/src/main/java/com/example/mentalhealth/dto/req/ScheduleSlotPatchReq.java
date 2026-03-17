package com.example.mentalhealth.dto.req;

import jakarta.validation.constraints.NotBlank;

public class ScheduleSlotPatchReq {

    @NotBlank(message = "status不能为空")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

