package com.example.mentalhealth.service.impl;

import com.example.mentalhealth.common.PageResp;
import com.example.mentalhealth.dto.resp.AdminAuditLogResp;
import com.example.mentalhealth.entity.AuditLog;
import com.example.mentalhealth.mapper.AuditLogMapper;
import com.example.mentalhealth.service.AuditLogService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.ZoneId;
import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogMapper auditLogMapper;

    public AuditLogServiceImpl(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    public void record(Long userId, String username, String action, String targetType, Long targetId, String detail) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                log.setIp(ip);
            }
        } catch (Exception e) {
            log.setIp("unknown");
        }

        auditLogMapper.insert(log);
    }

    @Override
    public PageResp<AdminAuditLogResp> pageLogs(int pageNum, int pageSize, Long userId, String action) {
        int pn = Math.max(pageNum, 1);
        int ps = Math.min(Math.max(pageSize, 1), 100);

        PageHelper.startPage(pn, ps);
        List<AuditLog> list = auditLogMapper.selectList(userId, action);
        List<AdminAuditLogResp> respList = list.stream().map(this::toResp).toList();
        PageInfo<AuditLog> pageInfo = new PageInfo<>(list);

        return new PageResp<>(pageInfo.getTotal(), pn, ps, respList);
    }

    private AdminAuditLogResp toResp(AuditLog log) {
        AdminAuditLogResp resp = new AdminAuditLogResp();
        resp.setId(log.getId());
        resp.setUserId(log.getUserId());
        resp.setUsername(log.getUsername());
        resp.setAction(log.getAction());
        resp.setTargetType(log.getTargetType());
        resp.setTargetId(log.getTargetId());
        resp.setDetail(log.getDetail());
        resp.setIp(log.getIp());
        if (log.getCreateTime() != null) {
            resp.setCreateTime(log.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        return resp;
    }
}
