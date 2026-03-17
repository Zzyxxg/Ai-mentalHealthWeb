package com.example.mentalhealth.service.impl;

import com.example.mentalhealth.common.PageResp;
import com.example.mentalhealth.dto.resp.NotificationResp;
import com.example.mentalhealth.entity.Notification;
import com.example.mentalhealth.mapper.NotificationMapper;
import com.example.mentalhealth.service.NotificationService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final int MAX_PAGE_SIZE = 100;

    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @Override
    public PageResp<NotificationResp> pageMy(Long userId, int pageNum, int pageSize, Integer readFlag) {
        int pn = Math.max(pageNum, 1);
        int ps = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        Integer rf = readFlag == null ? null : (readFlag == 0 ? 0 : 1);

        PageHelper.startPage(pn, ps);
        List<Notification> list = notificationMapper.selectByReceiver(userId, rf);
        List<NotificationResp> respList = list.stream().map(this::toResp).toList();
        PageInfo<Notification> pageInfo = new PageInfo<>(list);
        return new PageResp<>(pageInfo.getTotal(), pn, ps, respList);
    }

    @Override
    public void readOne(Long userId, Long notificationId) {
        notificationMapper.markRead(notificationId, userId);
    }

    @Override
    public void readAll(Long userId) {
        notificationMapper.markReadAll(userId);
    }

    private NotificationResp toResp(Notification n) {
        NotificationResp resp = new NotificationResp();
        resp.setId(n.getId());
        resp.setReceiverUserId(n.getReceiverUserId());
        resp.setType(n.getType() == null ? null : n.getType().name());
        resp.setTitle(n.getTitle());
        resp.setContent(n.getContent());
        resp.setReadFlag(Boolean.TRUE.equals(n.getReadFlag()));
        resp.setCreateTime(n.getCreateTime() == null ? null : n.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return resp;
    }
}

