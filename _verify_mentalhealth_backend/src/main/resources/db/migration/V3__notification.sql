CREATE TABLE IF NOT EXISTS notification (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  receiver_user_id BIGINT UNSIGNED NOT NULL COMMENT '接收者用户ID',
  type VARCHAR(32) NOT NULL COMMENT '类型',
  title VARCHAR(128) NOT NULL COMMENT '标题',
  content VARCHAR(512) NOT NULL COMMENT '内容',
  read_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读 0否1是',
  create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_receiver_read (receiver_user_id, read_flag),
  KEY idx_receiver_time (receiver_user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内通知表';

