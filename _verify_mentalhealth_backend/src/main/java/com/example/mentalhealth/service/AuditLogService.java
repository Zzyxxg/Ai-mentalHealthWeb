package com.example.mentalhealth.service;

import com.example.mentalhealth.common.PageResp;
import com.example.mentalhealth.dto.resp.AdminAuditLogResp;

public interface AuditLogService {
    void record(Long userId, String username, String action, String targetType, Long targetId, String detail);
    PageResp<AdminAuditLogResp> pageLogs(int pageNum, int pageSize, Long userId, String action);
}
