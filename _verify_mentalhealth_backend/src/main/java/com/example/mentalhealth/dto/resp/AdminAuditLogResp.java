package com.example.mentalhealth.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;

public class AdminAuditLogResp {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "操作人ID")
    private Long userId;
    @Schema(description = "操作人用户名")
    private String username;
    @Schema(description = "操作类型")
    private String action;
    @Schema(description = "目标类型")
    private String targetType;
    @Schema(description = "目标ID")
    private Long targetId;
    @Schema(description = "详情")
    private String detail;
    @Schema(description = "IP")
    private String ip;
    @Schema(description = "创建时间(毫秒戳)")
    private Long createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public Long getCreateTime() { return createTime; }
    public void setCreateTime(Long createTime) { this.createTime = createTime; }
}
