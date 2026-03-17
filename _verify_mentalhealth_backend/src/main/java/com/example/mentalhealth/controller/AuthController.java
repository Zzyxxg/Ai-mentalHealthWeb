package com.example.mentalhealth.controller;

import com.example.mentalhealth.common.Result;
import com.example.mentalhealth.config.TraceIdFilter;
import com.example.mentalhealth.dto.req.CounselorRegisterReq;
import com.example.mentalhealth.dto.req.LoginReq;
import com.example.mentalhealth.dto.req.StudentRegisterReq;
import com.example.mentalhealth.dto.resp.LoginResp;
import com.example.mentalhealth.dto.resp.UserMeResp;
import com.example.mentalhealth.security.UserPrincipal;
import com.example.mentalhealth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "登录与鉴权")
@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "登录", description = "返回 JWT，后续请求使用 Authorization: Bearer <token>")
    @ApiResponse(responseCode = "200", description = "成功", content = @Content(schema = @Schema(implementation = Result.class)))
    @ApiResponse(responseCode = "401", description = "用户名或密码错误")
    @PostMapping("/auth/login")
    public ResponseEntity<Result<LoginResp>> login(@Valid @RequestBody LoginReq req) {
        LoginResp resp = authService.login(req);
        return ResponseEntity.ok(Result.success(resp, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "学生注册")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping("/auth/register/student")
    public ResponseEntity<Result<Void>> registerStudent(@Valid @RequestBody StudentRegisterReq req) {
        authService.registerStudent(req);
        return ResponseEntity.ok(Result.success(null, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "咨询师注册")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping("/auth/register/counselor")
    public ResponseEntity<Result<Void>> registerCounselor(@Valid @RequestBody CounselorRegisterReq req) {
        authService.registerCounselor(req);
        return ResponseEntity.ok(Result.success(null, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "当前用户信息")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/auth/me")
    public ResponseEntity<Result<UserMeResp>> me(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UserMeResp resp = authService.me(principal.getUserId(), principal.getUsername());
        return ResponseEntity.ok(Result.success(resp, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "退出登录")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping("/auth/logout")
    public ResponseEntity<Result<Void>> logout(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            authService.logout(principal.getUserId(), principal.getUsername());
        }
        return ResponseEntity.ok(Result.success(null, TraceIdFilter.currentTraceId()));
    }
}
