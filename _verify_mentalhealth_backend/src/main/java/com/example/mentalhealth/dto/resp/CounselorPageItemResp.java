package com.example.mentalhealth.dto.resp;

public class CounselorPageItemResp {

    private Long id;
    private Long userId;
    private String realName;
    private String title;
    private String expertise;
    private String intro;
    private Boolean hasAvailableSlots;

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

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExpertise() {
        return expertise;
    }

    public void setExpertise(String expertise) {
        this.expertise = expertise;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public Boolean getHasAvailableSlots() {
        return hasAvailableSlots;
    }

    public void setHasAvailableSlots(Boolean hasAvailableSlots) {
        this.hasAvailableSlots = hasAvailableSlots;
    }
}

