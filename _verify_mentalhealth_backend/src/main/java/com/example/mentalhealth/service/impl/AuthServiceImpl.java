package com.example.mentalhealth.service.impl;

import com.example.mentalhealth.common.BizException;
import com.example.mentalhealth.common.ErrorCode;
import com.example.mentalhealth.dto.req.LoginReq;
import com.example.mentalhealth.dto.resp.LoginResp;
import com.example.mentalhealth.dto.resp.UserMeResp;
import com.example.mentalhealth.entity.User;
import com.example.mentalhealth.mapper.UserMapper;
import com.example.mentalhealth.security.JwtTokenProvider;
import com.example.mentalhealth.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public LoginResp login(LoginReq req) {
        User user = userMapper.selectByUsername(req.getUsername());
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResp(token, "Bearer", 86400L); // 假设 24h
    }

    @Override
    public UserMeResp me(Long userId, String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return new UserMeResp(user.getId(), user.getUsername(), user.getRole(), user.getNickname(), user.getAvatarUrl());
    }
}
