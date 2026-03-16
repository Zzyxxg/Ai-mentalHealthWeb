package com.example.mentalhealth.dto.resp;

public class CounselorResp {

    private Long id;
    private Long userId;
    private String realName;
    private String title;
    private String expertise;
    private String intro;

    public CounselorResp() {
    }

    public CounselorResp(Long id, Long userId, String realName, String title, String expertise, String intro) {
        this.id = id;
        this.userId = userId;
        this.realName = realName;
        this.title = title;
        this.expertise = expertise;
        this.intro = intro;
    }

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
}
