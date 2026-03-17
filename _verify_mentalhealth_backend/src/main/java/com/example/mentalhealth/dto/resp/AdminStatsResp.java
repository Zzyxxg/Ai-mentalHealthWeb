package com.example.mentalhealth.dto.resp;

import java.util.List;
import java.util.Map;

public class AdminStatsResp {
    private List<Map<String, Object>> appointmentsDaily;
    private List<Map<String, Object>> threadsDaily;
    private List<Map<String, Object>> assessmentsDaily;
    private List<Map<String, Object>> assessmentLevelDist;

    public List<Map<String, Object>> getAppointmentsDaily() {
        return appointmentsDaily;
    }

    public void setAppointmentsDaily(List<Map<String, Object>> appointmentsDaily) {
        this.appointmentsDaily = appointmentsDaily;
    }

    public List<Map<String, Object>> getThreadsDaily() {
        return threadsDaily;
    }

    public void setThreadsDaily(List<Map<String, Object>> threadsDaily) {
        this.threadsDaily = threadsDaily;
    }

    public List<Map<String, Object>> getAssessmentsDaily() {
        return assessmentsDaily;
    }

    public void setAssessmentsDaily(List<Map<String, Object>> assessmentsDaily) {
        this.assessmentsDaily = assessmentsDaily;
    }

    public List<Map<String, Object>> getAssessmentLevelDist() {
        return assessmentLevelDist;
    }

    public void setAssessmentLevelDist(List<Map<String, Object>> assessmentLevelDist) {
        this.assessmentLevelDist = assessmentLevelDist;
    }
}

