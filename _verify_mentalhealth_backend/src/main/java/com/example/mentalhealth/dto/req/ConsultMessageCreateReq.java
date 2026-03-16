package com.example.mentalhealth.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ConsultMessageCreateReq {

    @NotNull(message = "线程ID不能为空")
    private Long threadId;

    @NotBlank(message = "内容不能为空")
    private String content;

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
