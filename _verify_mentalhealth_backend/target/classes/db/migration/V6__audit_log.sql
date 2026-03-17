CREATE TABLE `audit_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT COMMENT '操作人ID',
    `username` VARCHAR(64) COMMENT '操作人用户名',
    `action` VARCHAR(64) NOT NULL COMMENT '操作类型 (如 LOGIN, LOGOUT, USER_UPDATE_STATUS, USER_RESET_PASSWORD, APPOINTMENT_CANCEL, THREAD_CLOSE, CONTENT_HIDE)',
    `target_type` VARCHAR(64) COMMENT '操作目标类型 (如 THREAD, MESSAGE, USER, APPOINTMENT)',
    `target_id` BIGINT COMMENT '操作目标ID',
    `detail` TEXT COMMENT '操作详情 JSON',
    `ip` VARCHAR(64) COMMENT '操作IP地址',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_action` (`action`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';
