package com.example.mentalhealth.service;

import com.example.mentalhealth.common.PageResp;
import com.example.mentalhealth.dto.req.ConsultAppointmentCreateReq;
import com.example.mentalhealth.dto.req.ConsultMessageCreateReq;
import com.example.mentalhealth.dto.req.ConsultThreadCreateReq;
import com.example.mentalhealth.dto.resp.ConsultAppointmentResp;
import com.example.mentalhealth.dto.resp.ConsultMessageResp;
import com.example.mentalhealth.dto.resp.ConsultThreadResp;
import com.example.mentalhealth.dto.resp.CounselorResp;
import java.util.List;

public interface ConsultService {

    List<CounselorResp> listConsultants(String keyword);

    CounselorResp getConsultant(Long consultantId);

    ConsultAppointmentResp createAppointment(Long userId, ConsultAppointmentCreateReq req, String idempotencyKey);

    ConsultAppointmentResp getAppointment(Long userId, Long appointmentId);

    PageResp<ConsultAppointmentResp> pageAppointments(Long userId, int pageNum, int pageSize, String status);

    ConsultAppointmentResp cancelAppointment(Long userId, Long appointmentId);

    ConsultThreadResp createThread(Long studentUserId, ConsultThreadCreateReq req);

    ConsultThreadResp getThread(Long userId, Long threadId);

    PageResp<ConsultThreadResp> listThreads(Long userId, String role, int pageNum, int pageSize, String status);

    ConsultMessageResp sendMessage(Long userId, String role, ConsultMessageCreateReq req);

    void hideThread(Long threadId, String reason);

    void hideMessage(Long messageId, String reason);
}
