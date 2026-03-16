package com.example.mentalhealth.service;

import com.example.mentalhealth.dto.req.LoginReq;
import com.example.mentalhealth.dto.resp.LoginResp;
import com.example.mentalhealth.dto.resp.UserMeResp;

public interface AuthService {

    LoginResp login(LoginReq req);

    UserMeResp me(Long userId, String username);
}
