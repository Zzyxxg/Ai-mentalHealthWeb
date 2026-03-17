package com.example.mentalhealth.controller;

import com.example.mentalhealth.common.PageResp;
import com.example.mentalhealth.common.Result;
import com.example.mentalhealth.config.TraceIdFilter;
import com.example.mentalhealth.dto.req.AdminResetPasswordReq;
import com.example.mentalhealth.dto.req.AdminUserStatusPatchReq;
import com.example.mentalhealth.dto.resp.AdminUserResp;
import com.example.mentalhealth.dto.resp.AssessmentResp;
import com.example.mentalhealth.dto.resp.AdminStatsResp;
import com.example.mentalhealth.dto.resp.ConsultAppointmentResp;
import com.example.mentalhealth.dto.resp.ConsultThreadResp;
import com.example.mentalhealth.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin", description = "管理员后台")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "用户列表（分页）")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/users")
    public ResponseEntity<Result<PageResp<AdminUserResp>>> pageUsers(
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) @Max(100) int pageSize,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "keyword", required = false) String keyword) {
        PageResp<AdminUserResp> page = adminService.pageUsers(pageNum, pageSize, role, keyword);
        return ResponseEntity.ok(Result.success(page, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "启用/禁用用户")
    @ApiResponse(responseCode = "200", description = "成功")
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<Result<Void>> patchUserStatus(@PathVariable("id") Long id, @Valid @RequestBody AdminUserStatusPatchReq req) {
        adminService.updateUserStatus(id, req.getStatus());
        return ResponseEntity.ok(Result.success(null, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "重置用户密码")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<Result<Void>> resetPassword(@PathVariable("id") Long id, @Valid @RequestBody AdminResetPasswordReq req) {
        adminService.resetPassword(id, req.getNewPassword());
        return ResponseEntity.ok(Result.success(null, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "预约记录（分页）")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/appointments")
    public ResponseEntity<Result<PageResp<ConsultAppointmentResp>>> pageAppointments(
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) @Max(100) int pageSize,
            @RequestParam(value = "status", required = false) String status) {
        return ResponseEntity.ok(Result.success(adminService.pageAppointments(pageNum, pageSize, status), TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "咨询记录（分页）")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/consult-threads")
    public ResponseEntity<Result<PageResp<ConsultThreadResp>>> pageThreads(
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) @Max(100) int pageSize) {
        return ResponseEntity.ok(Result.success(adminService.pageThreads(pageNum, pageSize), TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "测评记录（分页）")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/assessments")
    public ResponseEntity<Result<PageResp<AssessmentResp>>> pageAssessments(
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) @Max(100) int pageSize,
            @RequestParam(value = "scaleType", required = false) String scaleType) {
        return ResponseEntity.ok(Result.success(adminService.pageAssessments(pageNum, pageSize, scaleType), TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "统计概览（基础）")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/stats")
    public ResponseEntity<Result<AdminStatsResp>> stats(@RequestParam(value = "days", defaultValue = "30") @Min(1) @Max(365) int days) {
        return ResponseEntity.ok(Result.success(adminService.stats(days), TraceIdFilter.currentTraceId()));
    }
}

