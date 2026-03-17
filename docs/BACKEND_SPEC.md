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
- CREATED：已提交（初始状态）
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
- counselorName（冗余字段，方便前端展示）
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

## 6. 预约与排班（ScheduleSlot / Appointment）接口口径（MVP）

### 6.1 排班接口

#### POST `/api/v1/schedule-slots`（咨询师）批量创建排班
- **请求**：数组
  - date: string（yyyy-MM-dd）
  - startTime: string（HH:mm 或 HH:mm:ss）
  - endTime: string（HH:mm 或 HH:mm:ss）
- **规则**
  - 同一咨询师同一 `date+startTime+endTime` 唯一（DB 唯一约束 + 冲突返回 409）
  - `endTime` 必须晚于 `startTime`

#### GET `/api/v1/schedule-slots` 查询排班
- **查询参数**
  - counselorUserId?: number（学生查询必须传；咨询师查询自己可不传）
  - startDate: number（epoch millis）
  - endDate: number（epoch millis）
- **返回**：`ScheduleSlotResp[]`

#### PATCH `/api/v1/schedule-slots/{slotId}`（咨询师）更新排班状态
- **请求**：`{ status: "AVAILABLE" | "UNAVAILABLE" }`
- **规则**
  - 仅允许操作未来时段
  - `OCCUPIED` 不允许手动改状态

#### PATCH `/api/v1/schedule-slots/{slotId}/delete`（咨询师）删除排班（软删）
- **规则**
  - 仅允许删除未来时段
  - `OCCUPIED` 不允许删除

### 6.2 预约接口（与排班强绑定）

#### POST `/api/v1/consult-appointments`（学生）创建预约
- **请求**：`{ counselorUserId, startTime(epoch millis), durationMinutes, note? }`
- **规则**
  - 创建成功即视为 **CONFIRMED**
  - 根据 `counselorUserId + startTime + durationMinutes` 精确匹配唯一 `ScheduleSlot`（date=startTime日期，startTime=startTime时间，endTime=start+duration）
  - 仅当 slot.status=AVAILABLE 才可创建；创建成功后 slot.status 置为 **OCCUPIED**

#### PATCH `/api/v1/consult-appointments/{id}` action=CANCEL（学生）取消预约
- **规则**
  - 已完成（COMPLETED）不可取消
  - 取消成功后，若绑定 slot 且 slot 为 OCCUPIED，则释放为 **AVAILABLE**

### 4.8 Assessment（测评记录）
- id
- userId
- scaleType（PHQ9/GAD7）
- totalScore
- level
- suggestion
- createTime

## 8. 测评（Assessment）接口口径（MVP）

### 8.1 GET `/api/v1/assessments/scales`
- 返回 PHQ9 / GAD7 的列表（含 type/name）

### 8.2 GET `/api/v1/assessments/scales/{type}`
- 返回量表结构（题目 + 选项分值）

### 8.3 POST `/api/v1/assessments`（学生）
- **请求**：`{ scaleType: "PHQ9" | "GAD7", answers: number[] }`（answers 分值范围 0~3）
- **返回**：`AssessmentResp`（含 totalScore/level/suggestion）

### 8.4 GET `/api/v1/assessments/my`（分页）
- **查询参数**：pageNum/pageSize/scaleType?
- **返回**：`PageResp<AssessmentResp>`

### 4.9 Notification（站内通知）
- id
- receiverUserId
- type（APPOINTMENT_CREATED/APPOINTMENT_CANCELED/CONSULTANT_REPLIED）
- title
- content
- readFlag（0/1）
- createTime

## 7. 通知（Notification）接口口径（MVP）

### 7.1 GET `/api/v1/notifications` 通知列表（分页）
- **查询参数**
  - pageNum: number（默认 1）
  - pageSize: number（默认 10）
  - readFlag?: number（0 未读 / 1 已读）
- **返回**：`PageResp<NotificationResp>`

### 7.2 PATCH `/api/v1/notifications/{id}/read` 标记已读
- **规则**：仅能标记自己的通知

### 7.3 POST `/api/v1/notifications/read-all` 全部标记已读

## 9. 管理端（Admin）接口口径（MVP）

### 9.1 GET `/api/v1/admin/users`
- 查询：pageNum/pageSize/role?/keyword?
- 返回：`PageResp<AdminUserResp>`

### 9.2 PATCH `/api/v1/admin/users/{id}/status`
- 请求：`{ status: "ENABLED" | "DISABLED" }`

### 9.3 POST `/api/v1/admin/users/{id}/reset-password`
- 请求：`{ newPassword: string }`

### 9.4 GET `/api/v1/admin/appointments`
- 查询：pageNum/pageSize/status?

### 9.5 GET `/api/v1/admin/consult-threads`
- 查询：pageNum/pageSize

### 9.6 GET `/api/v1/admin/assessments`
- 查询：pageNum/pageSize/scaleType?

### 9.7 GET `/api/v1/admin/stats`
- 查询：days（默认30）

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
