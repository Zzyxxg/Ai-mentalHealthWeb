package com.example.mentalhealth.entity;

import com.example.mentalhealth.enums.AssessmentScaleType;
import java.time.LocalDateTime;

public class Assessment {
    private Long id;
    private Long userId;
    private AssessmentScaleType scaleType;
    private Integer totalScore;
    private String level;
    private String suggestion;
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public AssessmentScaleType getScaleType() {
        return scaleType;
    }

    public void setScaleType(AssessmentScaleType scaleType) {
        this.scaleType = scaleType;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}

