package com.example.mentalhealth.controller;

import com.example.mentalhealth.common.ErrorCode;
import com.example.mentalhealth.common.PageResp;
import com.example.mentalhealth.common.Result;
import com.example.mentalhealth.config.TraceIdFilter;
import com.example.mentalhealth.dto.req.ConsultAppointmentCreateReq;
import com.example.mentalhealth.dto.req.ConsultAppointmentPatchReq;
import com.example.mentalhealth.dto.req.ConsultMessageCreateReq;
import com.example.mentalhealth.dto.req.ConsultThreadCreateReq;
import com.example.mentalhealth.dto.resp.ConsultAppointmentResp;
import com.example.mentalhealth.dto.resp.ConsultMessageResp;
import com.example.mentalhealth.dto.resp.ConsultThreadResp;
import com.example.mentalhealth.dto.resp.CounselorResp;
import com.example.mentalhealth.security.UserPrincipal;
import com.example.mentalhealth.service.ConsultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Consult", description = "咨询与预约（mock）")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/v1")
@Validated
public class ConsultController {

    private final ConsultService consultService;

    public ConsultController(ConsultService consultService) {
        this.consultService = consultService;
    }

    @Operation(summary = "咨询师列表")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/consultants")
    public ResponseEntity<Result<List<CounselorResp>>> listConsultants(
            @Parameter(description = "搜索关键词（姓名/擅长/职称）") @RequestParam(value = "keyword", required = false) String keyword) {
        List<CounselorResp> list = consultService.listConsultants(keyword);
        return ResponseEntity.ok(Result.success(list, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "咨询师详情")
    @ApiResponse(responseCode = "200", description = "成功")
    @ApiResponse(responseCode = "404", description = "不存在")
    @GetMapping("/consultants/{consultantId}")
    public ResponseEntity<Result<CounselorResp>> getConsultant(@PathVariable("consultantId") Long consultantId) {
        CounselorResp resp = consultService.getConsultant(consultantId);
        return ResponseEntity.ok(Result.success(resp, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "创建预约", description = "支持 Idempotency-Key 幂等键（同一用户同一 key 重复提交返回同一预约）")
    @ApiResponse(responseCode = "201", description = "创建成功")
    @PostMapping("/consult-appointments")
    public ResponseEntity<Result<ConsultAppointmentResp>> createAppointment(
            Authentication authentication,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody ConsultAppointmentCreateReq req) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        ConsultAppointmentResp resp = consultService.createAppointment(principal.getUserId(), req, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(resp, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "预约详情")
    @ApiResponse(responseCode = "200", description = "成功")
    @ApiResponse(responseCode = "404", description = "不存在")
    @GetMapping("/consult-appointments/{appointmentId}")
    public ResponseEntity<Result<ConsultAppointmentResp>> getAppointment(Authentication authentication, @PathVariable("appointmentId") Long appointmentId) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        ConsultAppointmentResp resp = consultService.getAppointment(principal.getUserId(), appointmentId);
        return ResponseEntity.ok(Result.success(resp, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "预约分页", description = "mock 版本仅支持 pageNum/pageSize/status 过滤")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/consult-appointments")
    public ResponseEntity<Result<PageResp<ConsultAppointmentResp>>> pageAppointments(
            Authentication authentication,
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) @Max(100) int pageSize,
            @RequestParam(value = "status", required = false) String status) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        PageResp<ConsultAppointmentResp> page = consultService.pageAppointments(principal.getUserId(), pageNum, pageSize, status);
        return ResponseEntity.ok(Result.success(page, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "预约操作", description = "mock 版本仅支持 action=CANCEL")
    @ApiResponse(responseCode = "200", description = "成功")
    @PatchMapping("/consult-appointments/{appointmentId}")
    public ResponseEntity<Result<ConsultAppointmentResp>> patchAppointment(Authentication authentication,
                                                                          @PathVariable("appointmentId") Long appointmentId,
                                                                          @Valid @RequestBody ConsultAppointmentPatchReq req) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        if (!"CANCEL".equalsIgnoreCase(req.getAction())) {
            return ResponseEntity.badRequest().body(Result.fail(ErrorCode.PARAM_ERROR, "仅支持 action=CANCEL", TraceIdFilter.currentTraceId()));
        }
        ConsultAppointmentResp resp = consultService.cancelAppointment(principal.getUserId(), appointmentId);
        return ResponseEntity.ok(Result.success(resp, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "发起咨询会话")
    @ApiResponse(responseCode = "201", description = "创建成功")
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/consult-threads")
    public ResponseEntity<Result<ConsultThreadResp>> createThread(Authentication authentication, @Valid @RequestBody ConsultThreadCreateReq req) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        ConsultThreadResp resp = consultService.createThread(principal.getUserId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(resp, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "获取咨询会话列表")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/consult-threads")
    public ResponseEntity<Result<PageResp<ConsultThreadResp>>> listThreads(
            Authentication authentication,
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) @Max(100) int pageSize,
            @RequestParam(value = "status", required = false) String status) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        PageResp<ConsultThreadResp> page = consultService.listThreads(principal.getUserId(), principal.getRole(), pageNum, pageSize, status);
        return ResponseEntity.ok(Result.success(page, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "获取咨询会话详情")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/consult-threads/{threadId}")
    public ResponseEntity<Result<ConsultThreadResp>> getThread(Authentication authentication, @PathVariable("threadId") Long threadId) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        ConsultThreadResp resp = consultService.getThread(principal.getUserId(), threadId);
        return ResponseEntity.ok(Result.success(resp, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "发送咨询消息")
    @ApiResponse(responseCode = "201", description = "发送成功")
    @PostMapping("/consult-messages")
    public ResponseEntity<Result<ConsultMessageResp>> sendMessage(Authentication authentication, @Valid @RequestBody ConsultMessageCreateReq req) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        ConsultMessageResp resp = consultService.sendMessage(principal.getUserId(), principal.getRole(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(resp, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "下架咨询会话")
    @ApiResponse(responseCode = "200", description = "成功")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/consult-threads/{threadId}/hide")
    public ResponseEntity<Result<Void>> hideThread(@PathVariable("threadId") Long threadId, @RequestBody Map<String, String> body) {
        consultService.hideThread(threadId, body.get("reason"));
        return ResponseEntity.ok(Result.success(null, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "下架咨询消息")
    @ApiResponse(responseCode = "200", description = "成功")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/consult-messages/{messageId}/hide")
    public ResponseEntity<Result<Void>> hideMessage(@PathVariable("messageId") Long messageId, @RequestBody Map<String, String> body) {
        consultService.hideMessage(messageId, body.get("reason"));
        return ResponseEntity.ok(Result.success(null, TraceIdFilter.currentTraceId()));
    }
}
