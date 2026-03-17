package com.example.mentalhealth.service;

import com.example.mentalhealth.dto.req.LoginReq;
import com.example.mentalhealth.dto.resp.LoginResp;
import com.example.mentalhealth.dto.resp.UserMeResp;

public interface AuthService {

    LoginResp login(LoginReq req);

    void registerStudent(com.example.mentalhealth.dto.req.StudentRegisterReq req);

    void registerCounselor(com.example.mentalhealth.dto.req.CounselorRegisterReq req);

    UserMeResp me(Long userId, String username);

    void logout(Long userId, String username);
}
