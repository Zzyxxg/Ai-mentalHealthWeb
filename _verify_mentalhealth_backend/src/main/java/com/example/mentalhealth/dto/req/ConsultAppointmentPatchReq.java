package com.example.mentalhealth.dto.req;

import jakarta.validation.constraints.NotBlank;

public class ConsultAppointmentPatchReq {

    @NotBlank(message = "action 不能为空")
    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
