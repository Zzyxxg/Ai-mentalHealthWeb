package com.example.mentalhealth.service.impl;

import com.example.mentalhealth.common.BizException;
import com.example.mentalhealth.common.ErrorCode;
import com.example.mentalhealth.common.PageResp;
import com.example.mentalhealth.dto.resp.AdminUserResp;
import com.example.mentalhealth.dto.resp.AdminStatsResp;
import com.example.mentalhealth.dto.resp.AssessmentResp;
import com.example.mentalhealth.dto.resp.ConsultAppointmentResp;
import com.example.mentalhealth.dto.resp.ConsultThreadResp;
import com.example.mentalhealth.entity.Assessment;
import com.example.mentalhealth.entity.ConsultAppointment;
import com.example.mentalhealth.entity.ConsultThread;
import com.example.mentalhealth.entity.User;
import com.example.mentalhealth.enums.AppointmentStatus;
import com.example.mentalhealth.enums.AssessmentScaleType;
import com.example.mentalhealth.mapper.AssessmentMapper;
import com.example.mentalhealth.mapper.AdminStatsMapper;
import com.example.mentalhealth.mapper.ConsultAppointmentMapper;
import com.example.mentalhealth.mapper.ConsultMessageMapper;
import com.example.mentalhealth.mapper.ConsultThreadMapper;
import com.example.mentalhealth.mapper.UserMapper;
import com.example.mentalhealth.service.AdminService;
import com.example.mentalhealth.service.AuditLogService;
import com.example.mentalhealth.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.time.ZoneId;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserMapper userMapper;
    private final ConsultAppointmentMapper consultAppointmentMapper;
    private final ConsultThreadMapper consultThreadMapper;
    private final ConsultMessageMapper consultMessageMapper;
    private final AssessmentMapper assessmentMapper;
    private final AdminStatsMapper adminStatsMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public AdminServiceImpl(UserMapper userMapper,
                            ConsultAppointmentMapper consultAppointmentMapper,
                            ConsultThreadMapper consultThreadMapper,
                            ConsultMessageMapper consultMessageMapper,
                            AssessmentMapper assessmentMapper,
                            AdminStatsMapper adminStatsMapper,
                            PasswordEncoder passwordEncoder,
                            AuditLogService auditLogService) {
        this.userMapper = userMapper;
        this.consultAppointmentMapper = consultAppointmentMapper;
        this.consultThreadMapper = consultThreadMapper;
        this.consultMessageMapper = consultMessageMapper;
        this.assessmentMapper = assessmentMapper;
        this.adminStatsMapper = adminStatsMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    private UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) auth.getPrincipal();
        }
        return null;
    }

    @Override
    public PageResp<AdminUserResp> pageUsers(int pageNum, int pageSize, String role, String keyword) {
        int pn = Math.max(pageNum, 1);
        int ps = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        String r = role == null || role.isBlank() ? null : role.trim().toUpperCase();

        PageHelper.startPage(pn, ps);
        List<User> list = userMapper.selectList(r, keyword == null ? null : keyword.trim());
        List<AdminUserResp> respList = list.stream().map(this::toResp).toList();
        PageInfo<User> pageInfo = new PageInfo<>(list);
        return new PageResp<>(pageInfo.getTotal(), pn, ps, respList);
    }

    @Override
    public void updateUserStatus(Long userId, String status) {
        if (status == null || status.isBlank()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "status不能为空");
        }
        String s = status.trim().toUpperCase();
        if (!"ENABLED".equals(s) && !"DISABLED".equals(s)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "status仅支持 ENABLED/DISABLED");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        userMapper.updateStatus(userId, s);
        
        UserPrincipal currentUser = getCurrentUser();
        if (currentUser != null) {
            auditLogService.record(currentUser.getUserId(), currentUser.getUsername(), "USER_UPDATE_STATUS", "USER", userId, "Updated status to " + s);
        }
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "newPassword不能为空");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        String hash = passwordEncoder.encode(newPassword);
        userMapper.updatePasswordHash(userId, hash);
        
        UserPrincipal currentUser = getCurrentUser();
        if (currentUser != null) {
            auditLogService.record(currentUser.getUserId(), currentUser.getUsername(), "USER_RESET_PASSWORD", "USER", userId, "Reset password");
        }
    }

    @Override
    public PageResp<ConsultAppointmentResp> pageAppointments(int pageNum, int pageSize, String status) {
        int pn = Math.max(pageNum, 1);
        int ps = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        Integer code = null;
        if (status != null && !status.isBlank()) {
            String s = status.trim().toUpperCase();
            code = switch (s) {
                case "CREATED" -> AppointmentStatus.CREATED.getCode();
                case "CONFIRMED" -> AppointmentStatus.CONFIRMED.getCode();
                case "CANCELED" -> AppointmentStatus.CANCELED.getCode();
                case "COMPLETED" -> AppointmentStatus.COMPLETED.getCode();
                default -> throw new BizException(ErrorCode.PARAM_ERROR, "status仅支持 CREATED/CONFIRMED/CANCELED/COMPLETED");
            };
        }

        PageHelper.startPage(pn, ps);
        List<ConsultAppointment> list = consultAppointmentMapper.selectAll(code);
        List<ConsultAppointmentResp> respList = list.stream().map(this::toResp).toList();
        PageInfo<ConsultAppointment> pageInfo = new PageInfo<>(list);
        return new PageResp<>(pageInfo.getTotal(), pn, ps, respList);
    }

    @Override
    public PageResp<ConsultThreadResp> pageThreads(int pageNum, int pageSize) {
        int pn = Math.max(pageNum, 1);
        int ps = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);

        PageHelper.startPage(pn, ps);
        List<ConsultThread> list = consultThreadMapper.selectAll();
        List<ConsultThreadResp> respList = list.stream().map(this::toThreadResp).toList();
        PageInfo<ConsultThread> pageInfo = new PageInfo<>(list);
        return new PageResp<>(pageInfo.getTotal(), pn, ps, respList);
    }

    @Override
    public PageResp<AssessmentResp> pageAssessments(int pageNum, int pageSize, String scaleType) {
        int pn = Math.max(pageNum, 1);
        int ps = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        String t = null;
        if (scaleType != null && !scaleType.isBlank()) {
            try {
                t = AssessmentScaleType.valueOf(scaleType.trim().toUpperCase()).name();
            } catch (Exception e) {
                throw new BizException(ErrorCode.PARAM_ERROR, "scaleType仅支持 PHQ9/GAD7");
            }
        }

        PageHelper.startPage(pn, ps);
        List<Assessment> list = assessmentMapper.selectAll(t);
        List<AssessmentResp> respList = list.stream().map(this::toAssessmentResp).toList();
        PageInfo<Assessment> pageInfo = new PageInfo<>(list);
        return new PageResp<>(pageInfo.getTotal(), pn, ps, respList);
    }

    @Override
    public AdminStatsResp stats(int days) {
        int d = Math.min(Math.max(days, 1), 365);
        AdminStatsResp resp = new AdminStatsResp();
        resp.setAppointmentsDaily(adminStatsMapper.dailyAppointments(d));
        resp.setThreadsDaily(adminStatsMapper.dailyThreads(d));
        resp.setAssessmentsDaily(adminStatsMapper.dailyAssessments(d));
        resp.setAssessmentLevelDist(adminStatsMapper.assessmentLevelDist());
        return resp;
    }

    @Override
    public void hideThread(Long threadId, String reason) {
        int rows = consultThreadMapper.updateHidden(threadId, true, reason);
        if (rows == 0) {
            throw new BizException(ErrorCode.NOT_FOUND, "咨询会话不存在");
        }
        
        UserPrincipal currentUser = getCurrentUser();
        if (currentUser != null) {
            auditLogService.record(currentUser.getUserId(), currentUser.getUsername(), "CONTENT_HIDE", "THREAD", threadId, "Hidden reason: " + reason);
        }
    }

    @Override
    public void hideMessage(Long messageId, String reason) {
        int rows = consultMessageMapper.updateHidden(messageId, true, reason);
        if (rows == 0) {
            throw new BizException(ErrorCode.NOT_FOUND, "咨询消息不存在");
        }
        
        UserPrincipal currentUser = getCurrentUser();
        if (currentUser != null) {
            auditLogService.record(currentUser.getUserId(), currentUser.getUsername(), "CONTENT_HIDE", "MESSAGE", messageId, "Hidden reason: " + reason);
        }
    }

    private AdminUserResp toResp(User u) {
        AdminUserResp resp = new AdminUserResp();
        resp.setId(u.getId());
        resp.setUsername(u.getUsername());
        resp.setRole(u.getRole());
        resp.setStatus(u.getStatus());
        resp.setNickname(u.getNickname());
        resp.setAvatarUrl(u.getAvatarUrl());
        resp.setCreateTime(u.getCreateTime() == null ? null : u.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return resp;
    }

    private ConsultAppointmentResp toResp(ConsultAppointment appt) {
        return new ConsultAppointmentResp(
                appt.getId(),
                appt.getUserId(),
                appt.getCounselorUserId(),
                appt.getSlotId(),
                appt.getStartTime() == null ? null : appt.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                appt.getDurationMinutes(),
                appt.getStatus().name(),
                appt.getNote()
        );
    }

    private ConsultThreadResp toThreadResp(ConsultThread t) {
        ConsultThreadResp resp = new ConsultThreadResp();
        resp.setId(t.getId());
        resp.setStudentUserId(t.getStudentUserId());
        resp.setCounselorUserId(t.getCounselorUserId());
        resp.setStatus(t.getStatus().name());
        resp.setTopic(t.getTopic());
        resp.setHidden(t.getHidden());
        resp.setHiddenReason(t.getHiddenReason());
        resp.setCreateTime(t.getCreateTime() == null ? null : t.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        resp.setUpdateTime(t.getUpdateTime() == null ? null : t.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return resp;
    }

    private AssessmentResp toAssessmentResp(Assessment a) {
        AssessmentResp resp = new AssessmentResp();
        resp.setId(a.getId());
        resp.setUserId(a.getUserId());
        resp.setScaleType(a.getScaleType() == null ? null : a.getScaleType().name());
        resp.setTotalScore(a.getTotalScore());
        resp.setLevel(a.getLevel());
        resp.setSuggestion(a.getSuggestion());
        resp.setCreateTime(a.getCreateTime() == null ? null : a.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return resp;
    }
}

