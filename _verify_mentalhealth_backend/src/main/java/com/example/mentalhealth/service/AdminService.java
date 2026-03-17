package com.example.mentalhealth.service;

import com.example.mentalhealth.common.PageResp;
import com.example.mentalhealth.dto.resp.AdminUserResp;
import com.example.mentalhealth.dto.resp.AssessmentResp;
import com.example.mentalhealth.dto.resp.ConsultAppointmentResp;
import com.example.mentalhealth.dto.resp.ConsultThreadResp;
import com.example.mentalhealth.dto.resp.AdminStatsResp;

public interface AdminService {

    PageResp<AdminUserResp> pageUsers(int pageNum, int pageSize, String role, String keyword);

    void updateUserStatus(Long userId, String status);

    void resetPassword(Long userId, String newPassword);

    PageResp<ConsultAppointmentResp> pageAppointments(int pageNum, int pageSize, String status);

    PageResp<ConsultThreadResp> pageThreads(int pageNum, int pageSize);

    PageResp<AssessmentResp> pageAssessments(int pageNum, int pageSize, String scaleType);

    AdminStatsResp stats(int days);
}

