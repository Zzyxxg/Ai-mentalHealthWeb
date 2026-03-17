package com.example.mentalhealth.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class AdminHideReq {
    @Schema(description = "下架原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "下架原因不能为空")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
