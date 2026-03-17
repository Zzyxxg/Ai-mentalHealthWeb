package com.example.mentalhealth.controller;

import com.example.mentalhealth.common.ErrorCode;
import com.example.mentalhealth.common.PageResp;
import com.example.mentalhealth.common.Result;
import com.example.mentalhealth.config.TraceIdFilter;
import com.example.mentalhealth.dto.req.ConsultAppointmentCreateReq;
import com.example.mentalhealth.dto.req.ConsultAppointmentPatchReq;
import com.example.mentalhealth.dto.req.ConsultMessageCreateReq;
import com.example.mentalhealth.dto.req.ConsultThreadCreateReq;
import com.example.mentalhealth.dto.req.ScheduleSlotCreateReq;
import com.example.mentalhealth.dto.req.ScheduleSlotPatchReq;
import com.example.mentalhealth.dto.resp.ConsultAppointmentResp;
import com.example.mentalhealth.dto.resp.ConsultMessageResp;
import com.example.mentalhealth.dto.resp.ConsultThreadResp;
import com.example.mentalhealth.dto.resp.CounselorResp;
import com.example.mentalhealth.dto.resp.ScheduleSlotResp;
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
        PageResp<ConsultAppointmentResp> page = consultService.pageAppointments(principal.getUserId(), principal.getRole(), pageNum, pageSize, status);
        return ResponseEntity.ok(Result.success(page, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "预约操作", description = "支持 action=CANCEL / COMPLETE")
    @ApiResponse(responseCode = "200", description = "成功")
    @PatchMapping("/consult-appointments/{appointmentId}")
    public ResponseEntity<Result<ConsultAppointmentResp>> patchAppointment(Authentication authentication,
                                                                          @PathVariable("appointmentId") Long appointmentId,
                                                                          @Valid @RequestBody ConsultAppointmentPatchReq req) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        if ("CANCEL".equalsIgnoreCase(req.getAction())) {
            ConsultAppointmentResp resp = consultService.cancelAppointment(principal.getUserId(), appointmentId);
            return ResponseEntity.ok(Result.success(resp, TraceIdFilter.currentTraceId()));
        } else if ("COMPLETE".equalsIgnoreCase(req.getAction())) {
            ConsultAppointmentResp resp = consultService.completeAppointment(principal.getUserId(), appointmentId, req.getNote());
            return ResponseEntity.ok(Result.success(resp, TraceIdFilter.currentTraceId()));
        } else {
            return ResponseEntity.badRequest().body(Result.fail(ErrorCode.PARAM_ERROR, "action 仅支持 CANCEL/COMPLETE", TraceIdFilter.currentTraceId()));
        }
    }

    @Operation(summary = "配置排班")
    @ApiResponse(responseCode = "200", description = "成功")
    @PreAuthorize("hasRole('CONSULTANT')")
    @PostMapping("/schedule-slots")
    public ResponseEntity<Result<Void>> createScheduleSlots(Authentication authentication, @Valid @RequestBody List<ScheduleSlotCreateReq> reqs) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        consultService.createScheduleSlots(principal.getUserId(), reqs);
        return ResponseEntity.ok(Result.success(null, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "查询排班")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/schedule-slots")
    public ResponseEntity<Result<List<ScheduleSlotResp>>> listScheduleSlots(
            Authentication authentication,
            @RequestParam(value = "counselorUserId", required = false) Long counselorUserId,
            @RequestParam("startDate") Long startDate,
            @RequestParam("endDate") Long endDate) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        // 如果是咨询师查自己，或者用户查咨询师
        Long targetId = counselorUserId;
        if (targetId == null) {
            if ("CONSULTANT".equals(principal.getRole())) {
                targetId = principal.getUserId();
            } else {
                return ResponseEntity.badRequest().body(Result.fail(ErrorCode.PARAM_ERROR, "counselorUserId 不能为空", TraceIdFilter.currentTraceId()));
            }
        }
        
        List<ScheduleSlotResp> list = consultService.listScheduleSlots(targetId, startDate, endDate);
        return ResponseEntity.ok(Result.success(list, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "更新排班时段状态", description = "咨询师可将未来时段设为 UNAVAILABLE/AVAILABLE（OCCUPIED 不允许手动设置）")
    @ApiResponse(responseCode = "200", description = "成功")
    @PreAuthorize("hasRole('CONSULTANT')")
    @PatchMapping("/schedule-slots/{slotId}")
    public ResponseEntity<Result<Void>> patchScheduleSlotStatus(Authentication authentication,
                                                                @PathVariable("slotId") Long slotId,
                                                                @Valid @RequestBody ScheduleSlotPatchReq req) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        consultService.updateScheduleSlotStatus(principal.getUserId(), slotId, req.getStatus());
        return ResponseEntity.ok(Result.success(null, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "删除排班时段（软删）", description = "仅允许删除非 OCCUPIED 的未来时段")
    @ApiResponse(responseCode = "200", description = "成功")
    @PreAuthorize("hasRole('CONSULTANT')")
    @PatchMapping("/schedule-slots/{slotId}/delete")
    public ResponseEntity<Result<Void>> deleteScheduleSlot(Authentication authentication,
                                                           @PathVariable("slotId") Long slotId) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        consultService.deleteScheduleSlot(principal.getUserId(), slotId);
        return ResponseEntity.ok(Result.success(null, TraceIdFilter.currentTraceId()));
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

    @Operation(summary = "结束咨询会话")
    @ApiResponse(responseCode = "200", description = "成功")
    @PreAuthorize("hasRole('CONSULTANT')")
    @PatchMapping("/consult-threads/{threadId}/close")
    public ResponseEntity<Result<Void>> closeThread(Authentication authentication, @PathVariable("threadId") Long threadId) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        consultService.closeThread(principal.getUserId(), threadId);
        return ResponseEntity.ok(Result.success(null, TraceIdFilter.currentTraceId()));
    }
}
