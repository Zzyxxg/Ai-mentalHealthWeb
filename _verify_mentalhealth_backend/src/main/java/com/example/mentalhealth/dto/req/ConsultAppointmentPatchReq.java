package com.example.mentalhealth.dto.req;

import jakarta.validation.constraints.NotBlank;

public class ConsultAppointmentPatchReq {

    @NotBlank(message = "action 不能为空")
    private String action;

    private String note;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
