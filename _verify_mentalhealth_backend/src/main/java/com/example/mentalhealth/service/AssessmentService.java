package com.example.mentalhealth.service;

import com.example.mentalhealth.common.PageResp;
import com.example.mentalhealth.dto.req.AssessmentSubmitReq;
import com.example.mentalhealth.dto.resp.AssessmentResp;
import com.example.mentalhealth.dto.resp.AssessmentScaleResp;
import java.util.List;

public interface AssessmentService {

    List<AssessmentScaleResp> listScales();

    AssessmentScaleResp getScale(String type);

    AssessmentResp submit(Long userId, AssessmentSubmitReq req);

    PageResp<AssessmentResp> pageMy(Long userId, int pageNum, int pageSize, String scaleType);
}

