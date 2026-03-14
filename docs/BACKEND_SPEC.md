# 后端对齐说明（基于 PRD MVP v1.0）

## 1. 对齐结论（前后端统一口径）
### 1.1 咨询师账号来源与维护方式
- 咨询师账号由管理员创建与维护
- 咨询师不可自助注册；管理员可对咨询师账号进行启用/禁用与重置密码

### 1.2 学生身份字段范围
- 学生端展示字段：昵称、头像（不展示学号/姓名）
- 后端存储字段：
  - 账号基础信息：用户名、密码Hash、角色、状态
  - 学生档案（StudentProfile）：姓名、学号（用于咨询师识别与后台监管）
- 数据可见性：
  - 学生仅可查看与编辑昵称、头像
  - 咨询师仅在咨询/预约详情中可查看学生真实身份（姓名、学号），列表页默认不返回 realName/studentNo
  - 管理员可查看全量身份字段

### 1.3 咨询内容合规策略（最小动作）
- 采用“下架/隐藏”为主，不做物理删除
- 管理员对咨询线程/消息执行下架后：
  - 学生与咨询师端不可见或提示“内容已下架”
  - 后台可审计追溯（保留原始内容与操作记录）

## 2. 接口与返回规范（约定）
### 2.1 统一返回结构
- 后端所有业务接口统一返回：
  - code：业务码（成功为 0）
  - msg：提示信息
  - data：业务数据
  - timestamp：毫秒时间戳
  - traceId：链路追踪号

### 2.2 鉴权与权限
- 认证：JWT（Authorization: Bearer <token>）
- 未认证：HTTP 401
- 无权限：HTTP 403
- 数据范围控制以服务端为准（前端不做越权假设）

### 2.3 分页与时间字段
- 分页参数统一：pageNum、pageSize
- 时间字段统一：毫秒时间戳（epoch millis）

## 3. 核心枚举（代码值）
### 3.1 咨询线程状态（consult_thread_status）
- UNPROCESSED：未处理
- PROCESSING：处理中
- CLOSED：已结束

### 3.2 排班时段状态（schedule_slot_status）
- AVAILABLE：可预约
- OCCUPIED：已占用
- UNAVAILABLE：不可用

### 3.3 预约状态（appointment_status）
- CONFIRMED：已确认
- CANCELED：已取消
- COMPLETED：已完成

### 3.4 站内通知类型（notification_type）
- APPOINTMENT_CREATED：预约成功
- APPOINTMENT_CANCELED：预约取消
- CONSULTANT_REPLIED：咨询师回复咨询

## 4. 关键数据模型（字段级最小集）
### 4.1 User
- id
- username
- passwordHash
- role（STUDENT/CONSULTANT/ADMIN）
- status（ENABLED/DISABLED）
- nickname
- avatarUrl
- createTime
- updateTime

### 4.2 StudentProfile
- id
- userId
- realName
- studentNo
- createTime
- updateTime

### 4.3 CounselorProfile
- id
- userId
- realName
- title
- expertise
- intro
- createTime
- updateTime

### 4.4 ConsultThread
- id
- studentUserId
- counselorUserId
- status（UNPROCESSED/PROCESSING/CLOSED）
- topic
- hidden（0/1）
- hiddenReason
- createTime
- updateTime

### 4.5 ConsultMessage
- id
- threadId
- senderRole（STUDENT/CONSULTANT/ADMIN）
- content
- hidden（0/1）
- hiddenReason
- createTime

### 4.6 ScheduleSlot
- id
- counselorUserId
- date（yyyy-MM-dd）
- startTime（HH:mm）
- endTime（HH:mm）
- status（AVAILABLE/OCCUPIED/UNAVAILABLE）
- createTime
- updateTime

### 4.7 Appointment
- id
- studentUserId
- counselorUserId
- slotId
- status（CONFIRMED/CANCELED/COMPLETED）
- canceledReason
- createTime
- updateTime

### 4.8 Assessment（测评记录）
- id
- userId
- scaleType（PHQ9/GAD7）
- totalScore
- level
- suggestion
- createTime

### 4.9 Notification（站内通知）
- id
- receiverUserId
- type（APPOINTMENT_CREATED/APPOINTMENT_CANCELED/CONSULTANT_REPLIED）
- title
- content
- readFlag（0/1）
- createTime

### 4.10 AuditLog（审计日志）
- id
- operatorUserId
- action
- targetType
- targetId
- detail
- createTime

## 5. 对前端的交付物清单（后端需提供）
- Swagger/Knife4j 接口文档（包含字段、分页、错误码）
- 枚举代码值与状态机（以本文件为准）
- 核心接口 Mock 返回样例（至少覆盖登录、咨询、预约、测评、通知、后台列表）
