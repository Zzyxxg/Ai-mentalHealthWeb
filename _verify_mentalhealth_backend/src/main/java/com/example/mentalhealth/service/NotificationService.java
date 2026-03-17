package com.example.mentalhealth.service;

import com.example.mentalhealth.common.PageResp;
import com.example.mentalhealth.dto.resp.NotificationResp;

public interface NotificationService {

    PageResp<NotificationResp> pageMy(Long userId, int pageNum, int pageSize, Integer readFlag);

    void readOne(Long userId, Long notificationId);

    void readAll(Long userId);
}

