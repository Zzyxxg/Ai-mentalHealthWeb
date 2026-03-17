package com.example.mentalhealth.mapper;

import com.example.mentalhealth.entity.Notification;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NotificationMapper {

    int insert(Notification notification);

    List<Notification> selectByReceiver(@Param("receiverUserId") Long receiverUserId, @Param("readFlag") Integer readFlag);

    int markRead(@Param("id") Long id, @Param("receiverUserId") Long receiverUserId);

    int markReadAll(@Param("receiverUserId") Long receiverUserId);
}

