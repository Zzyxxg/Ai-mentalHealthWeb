ALTER TABLE notification
  ADD COLUMN student_name VARCHAR(64) DEFAULT NULL COMMENT '学生展示名' AFTER receiver_user_id,
  ADD COLUMN counselor_name VARCHAR(64) DEFAULT NULL COMMENT '咨询师展示名' AFTER student_name;

