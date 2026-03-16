package com.example.mentalhealth.service;

import com.example.mentalhealth.common.PageResp;
import com.example.mentalhealth.dto.req.ConsultAppointmentCreateReq;
import com.example.mentalhealth.dto.resp.ConsultAppointmentResp;
import com.example.mentalhealth.dto.resp.CounselorResp;
import java.util.List;

public interface ConsultService {

    List<CounselorResp> listConsultants(String keyword);

    CounselorResp getConsultant(Long consultantId);

    ConsultAppointmentResp createAppointment(Long userId, ConsultAppointmentCreateReq req, String idempotencyKey);

    ConsultAppointmentResp getAppointment(Long userId, Long appointmentId);

    PageResp<ConsultAppointmentResp> pageAppointments(Long userId, int pageNum, int pageSize, String status);

    ConsultAppointmentResp cancelAppointment(Long userId, Long appointmentId);
}
