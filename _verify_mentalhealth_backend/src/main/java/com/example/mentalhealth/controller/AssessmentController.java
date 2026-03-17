package com.example.mentalhealth.controller;

import com.example.mentalhealth.common.PageResp;
import com.example.mentalhealth.common.Result;
import com.example.mentalhealth.config.TraceIdFilter;
import com.example.mentalhealth.dto.req.AssessmentSubmitReq;
import com.example.mentalhealth.dto.resp.AssessmentResp;
import com.example.mentalhealth.dto.resp.AssessmentScaleResp;
import com.example.mentalhealth.security.UserPrincipal;
import com.example.mentalhealth.service.AssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Assessment", description = "心理测评（PHQ-9 / GAD-7）")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/v1")
@Validated
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @Operation(summary = "量表列表")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/assessments/scales")
    public ResponseEntity<Result<List<AssessmentScaleResp>>> listScales() {
        return ResponseEntity.ok(Result.success(assessmentService.listScales(), TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "量表结构")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/assessments/scales/{type}")
    public ResponseEntity<Result<AssessmentScaleResp>> getScale(@PathVariable("type") String type) {
        return ResponseEntity.ok(Result.success(assessmentService.getScale(type), TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "提交答卷并计算结果")
    @ApiResponse(responseCode = "201", description = "创建成功")
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/assessments")
    public ResponseEntity<Result<AssessmentResp>> submit(Authentication authentication, @Valid @RequestBody AssessmentSubmitReq req) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        AssessmentResp resp = assessmentService.submit(principal.getUserId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(resp, TraceIdFilter.currentTraceId()));
    }

    @Operation(summary = "我的测评历史（分页）")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/assessments/my")
    public ResponseEntity<Result<PageResp<AssessmentResp>>> my(Authentication authentication,
                                                               @RequestParam(value = "pageNum", defaultValue = "1") @Min(1) int pageNum,
                                                               @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) @Max(100) int pageSize,
                                                               @RequestParam(value = "scaleType", required = false) String scaleType) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        PageResp<AssessmentResp> page = assessmentService.pageMy(principal.getUserId(), pageNum, pageSize, scaleType);
        return ResponseEntity.ok(Result.success(page, TraceIdFilter.currentTraceId()));
    }
}

