package com.example.mentalhealth.service.impl;

import com.example.mentalhealth.common.BizException;
import com.example.mentalhealth.common.ErrorCode;
import com.example.mentalhealth.dto.req.CounselorRegisterReq;
import com.example.mentalhealth.dto.req.LoginReq;
import com.example.mentalhealth.dto.req.StudentRegisterReq;
import com.example.mentalhealth.dto.resp.LoginResp;
import com.example.mentalhealth.dto.resp.UserMeResp;
import com.example.mentalhealth.entity.CounselorProfile;
import com.example.mentalhealth.entity.StudentProfile;
import com.example.mentalhealth.entity.User;
import com.example.mentalhealth.mapper.CounselorProfileMapper;
import com.example.mentalhealth.mapper.StudentProfileMapper;
import com.example.mentalhealth.mapper.UserMapper;
import com.example.mentalhealth.security.JwtTokenProvider;
import com.example.mentalhealth.service.AuthService;
import com.example.mentalhealth.service.AuditLogService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final StudentProfileMapper studentProfileMapper;
    private final CounselorProfileMapper counselorProfileMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditLogService auditLogService;

    public AuthServiceImpl(UserMapper userMapper,
                           StudentProfileMapper studentProfileMapper,
                           CounselorProfileMapper counselorProfileMapper,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           AuditLogService auditLogService) {
        this.userMapper = userMapper;
        this.studentProfileMapper = studentProfileMapper;
        this.counselorProfileMapper = counselorProfileMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public void registerStudent(StudentRegisterReq req) {
        if (userMapper.selectByUsername(req.getUsername()) != null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "用户名已存在");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole("STUDENT");
        user.setStatus("ENABLED");
        user.setNickname(req.getNickname());
        user.setDeleted(0);
        userMapper.insert(user);

        StudentProfile profile = new StudentProfile();
        profile.setUserId(user.getId());
        profile.setRealName(req.getRealName());
        profile.setStudentNo(req.getStudentNo());
        profile.setDeleted(0);
        studentProfileMapper.insert(profile);
    }

    @Override
    @Transactional
    public void registerCounselor(CounselorRegisterReq req) {
        if (userMapper.selectByUsername(req.getUsername()) != null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "用户名已存在");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole("CONSULTANT");
        user.setStatus("ENABLED");
        user.setNickname(req.getRealName()); // 咨询师昵称默认使用真实姓名
        user.setDeleted(0);
        userMapper.insert(user);

        CounselorProfile profile = new CounselorProfile();
        profile.setUserId(user.getId());
        profile.setRealName(req.getRealName());
        profile.setTitle(req.getTitle());
        profile.setExpertise(req.getExpertise());
        profile.setIntro(req.getIntro());
        profile.setDeleted(0);
        counselorProfileMapper.insert(profile);
    }

    @Override
    public LoginResp login(LoginReq req) {
        User user = userMapper.selectByUsername(req.getUsername());
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if ("DISABLED".equalsIgnoreCase(user.getStatus())) {
            throw new BizException(ErrorCode.FORBIDDEN, "账号已被禁用");
        }

        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), user.getRole());
        
        auditLogService.record(user.getId(), user.getUsername(), "LOGIN", "USER", user.getId(), "User logged in");
        
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

    @Override
    public void logout(Long userId, String username) {
        auditLogService.record(userId, username, "LOGOUT", "USER", userId, "User logged out");
    }
}
