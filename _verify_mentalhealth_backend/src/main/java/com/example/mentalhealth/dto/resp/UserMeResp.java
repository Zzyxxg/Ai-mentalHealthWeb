package com.example.mentalhealth.dto.resp;

public class UserMeResp {

    private Long id;
    private String username;
    private String role;
    private String nickname;
    private String avatarUrl;

    public UserMeResp() {
    }

    public UserMeResp(Long id, String username, String role, String nickname, String avatarUrl) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
