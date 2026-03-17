CREATE TABLE IF NOT EXISTS assessment (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  scale_type VARCHAR(16) NOT NULL COMMENT '量表类型（PHQ9/GAD7）',
  total_score INT NOT NULL COMMENT '总分',
  level VARCHAR(32) NOT NULL COMMENT '等级',
  suggestion VARCHAR(512) NOT NULL COMMENT '建议',
  create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_user_time (user_id, create_time),
  KEY idx_scale_time (scale_type, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测评记录表';

