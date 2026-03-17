package com.example.mentalhealth.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class StudentRegisterReq {
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{4,32}$", message = "用户名必须是4-32位字母、数字、下划线或减号")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^.{6,32}$", message = "密码长度必须在6-32位之间")
    private String password;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    private String realName;

    private String studentNo;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }
}
