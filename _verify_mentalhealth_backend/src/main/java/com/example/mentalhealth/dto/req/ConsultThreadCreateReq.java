package com.example.mentalhealth.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ConsultThreadCreateReq {

    @NotNull(message = "咨询师ID不能为空")
    private Long counselorUserId;

    @NotBlank(message = "咨询主题不能为空")
    private String topic;

    @NotBlank(message = "内容不能为空")
    private String content;

    public Long getCounselorUserId() {
        return counselorUserId;
    }

    public void setCounselorUserId(Long counselorUserId) {
        this.counselorUserId = counselorUserId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
