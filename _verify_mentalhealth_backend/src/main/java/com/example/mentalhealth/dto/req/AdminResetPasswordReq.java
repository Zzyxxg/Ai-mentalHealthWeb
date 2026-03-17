package com.example.mentalhealth.dto.req;

import jakarta.validation.constraints.NotBlank;

public class AdminResetPasswordReq {
    @NotBlank(message = "newPassword不能为空")
    private String newPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

