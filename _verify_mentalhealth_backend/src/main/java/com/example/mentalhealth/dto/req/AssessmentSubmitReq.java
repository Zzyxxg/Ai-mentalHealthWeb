package com.example.mentalhealth.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class AssessmentSubmitReq {

    @NotBlank(message = "scaleType不能为空")
    private String scaleType;

    @NotEmpty(message = "answers不能为空")
    private List<Integer> answers;

    public String getScaleType() {
        return scaleType;
    }

    public void setScaleType(String scaleType) {
        this.scaleType = scaleType;
    }

    public List<Integer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Integer> answers) {
        this.answers = answers;
    }
}

