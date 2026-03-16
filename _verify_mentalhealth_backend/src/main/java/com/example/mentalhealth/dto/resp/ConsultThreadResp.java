package com.example.mentalhealth.dto.resp;

import java.util.List;

public class ConsultThreadResp {
    private Long id;
    private Long studentUserId;
    private Long counselorUserId;
    private String status;
    private String topic;
    private String content;
    private Boolean hidden;
    private String hiddenReason;
    private Long createTime;
    private Long updateTime;
    private List<ConsultMessageResp> messages;

    // 为了展示，可能需要咨询师姓名
    private String counselorName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentUserId() {
        return studentUserId;
    }

    public void setStudentUserId(Long studentUserId) {
        this.studentUserId = studentUserId;
    }

    public Long getCounselorUserId() {
        return counselorUserId;
    }

    public void setCounselorUserId(Long counselorUserId) {
        this.counselorUserId = counselorUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public String getHiddenReason() {
        return hiddenReason;
    }

    public void setHiddenReason(String hiddenReason) {
        this.hiddenReason = hiddenReason;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public List<ConsultMessageResp> getMessages() {
        return messages;
    }

    public void setMessages(List<ConsultMessageResp> messages) {
        this.messages = messages;
    }

    public String getCounselorName() {
        return counselorName;
    }

    public void setCounselorName(String counselorName) {
        this.counselorName = counselorName;
    }
}
