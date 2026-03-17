package com.example.mentalhealth.controller;

import com.example.mentalhealth.common.PageResp;
import com.example.mentalhealth.common.Result;
import com.example.mentalhealth.config.TraceIdFilter;
import com.example.mentalhealth.dto.resp.NotificationResp;
import com.example.mentalhealth.security.UserPrincipal;
import com.example.mentalhealth.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification", description = "站内通知")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/v1")
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "通知列表（分页）")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/notifications")
    public ResponseEntity<Result<PageResp<NotificationResp>>> page(Authentication authentication,
                                                                   @RequestParam(value = "pageNum", defaultValue = "1") @Min(1) int pageNum,
                                                                   @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) @Max(100) int pageSize,
                                                                   @RequestParam(value = "readFlag", required = false) Integer readFlag) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        PageResp<NotificationResp> page = notificationService.pageMy(principal.getUserId(), pageNum, pageSize, readFlag);
        return ResponseEntity.ok(Result.success(page, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "标记单条通知已读")
    @ApiResponse(responseCode = "200", description = "成功")
    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<Result<Void>> readOne(Authentication authentication, @PathVariable("id") Long id) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        notificationService.readOne(principal.getUserId(), id);
        return ResponseEntity.ok(Result.success(null, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "全部标记已读")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping("/notifications/read-all")
    public ResponseEntity<Result<Void>> readAll(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        notificationService.readAll(principal.getUserId());
        return ResponseEntity.ok(Result.success(null, TraceIdFilter.currentTraceId()));
    }
}

