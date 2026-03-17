# 心理健康咨询与测评平台（MVP）最终文档

本文件作为项目交付级“最终说明”，以当前仓库代码为准，整合并覆盖 [PRD.md](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/docs/PRD.md)、[BACKEND_SPEC.md](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/docs/BACKEND_SPEC.md)、[FRONTEND_SCHEDULE.md](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/docs/FRONTEND_SCHEDULE.md) 的核心口径。

## 1. 项目概览

### 1.1 产品定位
高校心理健康咨询与测评平台（MVP），三端分离：
- 学生端：发起/跟进咨询、心理测评、预约咨询、通知中心
- 咨询师端：处理咨询、排班与预约管理、通知中心
- 管理端：用户管理、记录监管、统计概览、内容治理（下架）、审计日志

### 1.2 核心闭环
- 测评筛查：量表选择 → 答题提交 → 评分/分级 → 历史可追溯
- 预约咨询：咨询师排班 → 学生预约（可选时长）→ 占用/释放 → 完成
- 匿名咨询：学生发起线程 → 咨询师回复 → 多轮对话 → 结束/治理

## 2. 仓库结构

```
Ai-mentalHealthWeb/
  frontend/                       # 前端（Vue3 + TS + Vite）
  _verify_mentalhealth_backend/    # 后端（Spring Boot 3 + MyBatis）
  docs/                            # 文档（PRD/对齐/排期/最终文档）
  docker-compose.yml               # 本地 MySQL/Redis
  README.md                        # 快速启动说明
```

## 3. 技术栈与运行时约束

### 3.1 前端
- Vue 3 + TypeScript（Composition API）
- Vite 5
- Element Plus
- Pinia（登录态/角色）
- Vue Router（路由守卫 + 角色路由）
- Axios（拦截器统一注入 JWT）
- ECharts + vue-echarts（管理端统计）

关键文件：
- 入口：[main.ts](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/frontend/src/main.ts)
- 路由：[router/index.ts](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/frontend/src/router/index.ts)
- HTTP 封装：[services/http.ts](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/frontend/src/services/http.ts)
- 登录态：[stores/auth.ts](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/frontend/src/stores/auth.ts)
- Vite 代理：[vite.config.ts](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/frontend/vite.config.ts)

### 3.2 后端
- Java 17 + Spring Boot 3.3.4
- Spring Security（无状态 JWT）
- MyBatis + PageHelper
- MySQL 8
- Redis（Spring Data Redis）+ Redisson（分布式锁）
- Flyway（数据库迁移）
- SpringDoc OpenAPI（Swagger UI）

关键文件：
- 应用入口：[MentalHealthApplication.java](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/_verify_mentalhealth_backend/src/main/java/com/example/mentalhealth/MentalHealthApplication.java)
- 配置：[application.yml](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/_verify_mentalhealth_backend/src/main/resources/application.yml)
- 安全：[SecurityConfig.java](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/_verify_mentalhealth_backend/src/main/java/com/example/mentalhealth/config/SecurityConfig.java)
- JWT：[JwtTokenProvider.java](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/_verify_mentalhealth_backend/src/main/java/com/example/mentalhealth/security/JwtTokenProvider.java)
- 迁移脚本：[_verify_mentalhealth_backend/src/main/resources/db/migration](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/_verify_mentalhealth_backend/src/main/resources/db/migration)

## 4. 本地启动（推荐口径）

### 4.1 启动依赖（MySQL/Redis）
在仓库根目录：
```bash
docker compose up -d
```

默认端口：
- MySQL：3306
- Redis：6379

重要说明（配置对齐）：
- docker-compose 默认 MySQL root 密码是 `password`（见 [docker-compose.yml](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/docker-compose.yml#L4-L15)）
- 后端默认连接密码是 `123456`（见 [application.yml](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/_verify_mentalhealth_backend/src/main/resources/application.yml#L7-L11)）

二选一对齐即可：
- 方案 A：把 `application.yml` 的 `spring.datasource.password` 改为 `password`
- 方案 B：把 `docker-compose.yml` 的 `MYSQL_ROOT_PASSWORD` 改为 `123456`，并重建容器数据卷

### 4.2 启动后端
```bash
cd _verify_mentalhealth_backend
mvn spring-boot:run
```

后端默认：
- Base URL：`http://localhost:8080`
- Health：`GET /api/v1/health`
- Swagger UI：`/swagger-ui/index.html`

### 4.3 启动前端
```bash
cd frontend
npm install
npm run dev
```

前端默认：
- URL：`http://localhost:5173`
- 通过 Vite 代理转发 `/api` 到 `http://localhost:8080`（见 [vite.config.ts](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/frontend/vite.config.ts#L13-L20)）

## 5. 前端架构说明

### 5.1 路由与三端布局
- 三端 Layout：`StudentLayout` / `CounselorLayout` / `AdminLayout`
- Router Guard：根据 `to.meta.roles` 进行角色校验，未登录跳 `/login`，无权限跳 `/403`（见 [router/index.ts](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/frontend/src/router/index.ts#L74-L99)）

路由入口（示例）：
- 学生：`/student/*`
- 咨询师：`/counselor/*`
- 管理员：`/admin/*`

### 5.2 API 调用分层
- `services/*`：按业务域封装接口（auth/consult/assessment/notification/admin）
- `services/http.ts`：统一注入 `Authorization: Bearer <token>`，并处理 401/403 全局跳转

### 5.3 通知体系（前端）
- 顶部铃铛组件：轮询未读数 + 事件刷新（见 [NotificationBell.vue](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/frontend/src/components/NotificationBell.vue)）
- 通知列表页：支持全部/未读/已读筛选，切换即刷新（见 [NotificationList.vue](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/frontend/src/views/pages/common/NotificationList.vue)）
- 页面间同步：通过 `window.dispatchEvent(new Event('mh:notifications-updated'))` 触发刷新

## 6. 后端架构说明

### 6.1 分层约定
- `controller`：HTTP 层（入参校验、鉴权、响应封装）
- `service`：业务层（事务、权限/数据范围、核心规则）
- `mapper` + `resources/mapper/*.xml`：数据访问层（MyBatis XML）
- `entity` / `dto`：实体与传输对象
- `common`：统一返回、错误码、分页、异常处理
- `security`：JWT 与 Spring Security 集成

### 6.2 统一返回与错误语义
后端业务接口统一返回 `Result`：
- `code=0` 表示成功
- 认证失败：HTTP 401
- 授权失败：HTTP 403
- 参数错误：HTTP 400（业务码见 `ErrorCode`）

相关实现：
- [Result.java](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/_verify_mentalhealth_backend/src/main/java/com/example/mentalhealth/common/Result.java)
- [GlobalExceptionHandler.java](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/_verify_mentalhealth_backend/src/main/java/com/example/mentalhealth/common/GlobalExceptionHandler.java)

### 6.3 鉴权与授权
- JWT：HS256，claims 包含 `userId`、`role`（见 [JwtTokenProvider.java](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/_verify_mentalhealth_backend/src/main/java/com/example/mentalhealth/security/JwtTokenProvider.java#L31-L45)）
- 保护路径：默认全部需要登录，放行 `/api/v1/auth/**`、`/api/v1/consultants/**`、Swagger、health（见 [SecurityConfig.java](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/_verify_mentalhealth_backend/src/main/java/com/example/mentalhealth/config/SecurityConfig.java#L34-L53)）
- 方法级权限：使用 `@PreAuthorize`（例如咨询师排班接口）

## 7. 核心业务模块（以代码为准）

### 7.1 Auth（注册/登录/当前用户）
控制器：[AuthController.java](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/_verify_mentalhealth_backend/src/main/java/com/example/mentalhealth/controller/AuthController.java)
- POST `/api/v1/auth/login`
- POST `/api/v1/auth/register/student`
- POST `/api/v1/auth/register/counselor`
- GET `/api/v1/auth/me`
- POST `/api/v1/auth/logout`

### 7.2 咨询师信息（公开可查）
控制器：[ConsultController.java](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/_verify_mentalhealth_backend/src/main/java/com/example/mentalhealth/controller/ConsultController.java)
- GET `/api/v1/consultants`
- GET `/api/v1/consultants/{consultantId}`

### 7.3 排班与预约（ScheduleSlot / Appointment）
排班：
- POST `/api/v1/schedule-slots`（咨询师）
- GET `/api/v1/schedule-slots`
- PATCH `/api/v1/schedule-slots/{slotId}`（咨询师）
- PATCH `/api/v1/schedule-slots/{slotId}/delete`（咨询师）

预约：
- POST `/api/v1/consult-appointments`（学生）
- GET `/api/v1/consult-appointments`（分页，学生/咨询师视角不同）
- GET `/api/v1/consult-appointments/{appointmentId}`
- PATCH `/api/v1/consult-appointments/{appointmentId}`（action=CANCEL/COMPLETE）

预约“可选时长”与时段占用规则（当前代码口径）：
- 学生预约请求携带 `startTime + durationMinutes`
- 后端不再要求与排班时段“完全一致”，而是寻找一个 `AVAILABLE` 且能覆盖该时间范围的时段
- 若预约只占用部分时段：后端会将原时段拆分为“占用段（OCCUPIED）+ 剩余可用段（AVAILABLE）”，保证后续仍可预约剩余时间
- 幂等：支持 `Idempotency-Key`（相同用户 + key 返回同一预约）

关键实现：
- [ConsultServiceImpl.createAppointment](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/_verify_mentalhealth_backend/src/main/java/com/example/mentalhealth/service/impl/ConsultServiceImpl.java#L163-L247)

### 7.4 匿名咨询（ConsultThread / ConsultMessage）
- POST `/api/v1/consult-threads`（学生）
- GET `/api/v1/consult-threads`（列表）
- GET `/api/v1/consult-threads/{threadId}`
- POST `/api/v1/consult-messages`（发送消息）
- PATCH `/api/v1/consult-threads/{threadId}/close`（咨询师结束）

内容治理（下架）：
- PATCH `/api/v1/consult-threads/{threadId}/hide`（管理员）
- PATCH `/api/v1/consult-messages/{messageId}/hide`（管理员）

### 7.5 通知中心（Notification）
控制器：[NotificationController.java](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/_verify_mentalhealth_backend/src/main/java/com/example/mentalhealth/controller/NotificationController.java)
- GET `/api/v1/notifications`（分页，可选 readFlag=0/1）
- PATCH `/api/v1/notifications/{id}/read`
- POST `/api/v1/notifications/read-all`

通知类型（以枚举为准）：
- `APPOINTMENT_CREATED` / `APPOINTMENT_CANCELED`
- `CONSULTANT_REPLIED`
- `STUDENT_MESSAGE`

预约通知内容已包含“咨询师是谁”（姓名优先，无姓名降级为 ID）：
- [writeAppointmentNotification](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/_verify_mentalhealth_backend/src/main/java/com/example/mentalhealth/service/impl/ConsultServiceImpl.java#L593-L613)

### 7.6 测评（Assessment）
控制器：[AssessmentController.java](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/_verify_mentalhealth_backend/src/main/java/com/example/mentalhealth/controller/AssessmentController.java)
- GET `/api/v1/assessments/scales`
- GET `/api/v1/assessments/scales/{type}`
- POST `/api/v1/assessments`
- GET `/api/v1/assessments/my`

### 7.7 管理端（Admin）
控制器：[AdminController.java](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/_verify_mentalhealth_backend/src/main/java/com/example/mentalhealth/controller/AdminController.java)
- GET `/api/v1/admin/users`
- PATCH `/api/v1/admin/users/{id}/status`
- POST `/api/v1/admin/users/{id}/reset-password`
- GET `/api/v1/admin/appointments`
- GET `/api/v1/admin/consult-threads`
- GET `/api/v1/admin/assessments`
- GET `/api/v1/admin/stats`
- PATCH `/api/v1/admin/consult-threads/{id}/hide`
- PATCH `/api/v1/admin/consult-messages/{id}/hide`
- GET `/api/v1/admin/audit-logs`

## 8. 数据库与迁移（Flyway）

迁移目录：[_verify_mentalhealth_backend/src/main/resources/db/migration](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/_verify_mentalhealth_backend/src/main/resources/db/migration)

核心表（节选）：
- `mentalhealth_user`：账号、角色、状态、昵称头像
- `student_profile` / `counselor_profile`：角色扩展资料
- `schedule_slot`：排班时段（date + start_time + end_time + status）
- `consult_appointment`：预约（start_time + duration_minutes + status）
- `consult_thread` / `consult_message`：咨询线程与消息
- `notification`：站内通知
- `assessment`：测评记录
- `audit_log`：审计日志

初始化表结构见：[V1__init.sql](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/_verify_mentalhealth_backend/src/main/resources/db/migration/V1__init.sql)

## 9. 预置数据（开发演示）

后端启动会初始化测试数据（见 [DataInitializer.java](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/_verify_mentalhealth_backend/src/main/java/com/example/mentalhealth/config/DataInitializer.java)）：
- 学生：`demo / demo1234`
- 咨询师：`counselor1 / 123456`（以及 counselor2、counselor3）

## 10. 构建与交付

### 10.1 前端构建
```bash
cd frontend
npm install
npm run build
```
产物：`frontend/dist/`

### 10.2 后端构建
```bash
cd _verify_mentalhealth_backend
mvn -DskipTests package
```
产物：`_verify_mentalhealth_backend/target/*.jar`

## 11. 常见问题排查

### 11.1 前端接口 404 / 跨域
- 确认前端通过 `/api` 访问，且 Vite 代理指向 `http://localhost:8080`（见 [vite.config.ts](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/frontend/vite.config.ts#L13-L20)）
- 确认后端已启动并能访问 health

### 11.2 登录后 401
- JWT 失效或后端 secret 不一致会导致 401
- 前端拦截器会自动清理 token 并跳转 login（见 [services/http.ts](file:///Users/zhangzeyu/ai_completion/trace/Ai-mentalHealthWeb/frontend/src/services/http.ts#L21-L35)）

### 11.3 数据库连不上 / Flyway 失败
- 优先检查 MySQL root 密码与 `application.yml` 是否一致（见“4.1 配置对齐”）

### 11.4 预约失败提示“该时段未配置/不可预约”
- 先确认咨询师端排班已经创建且状态为 `AVAILABLE`
- 预约请求必须落在某个 `AVAILABLE` 时段内；若时段被拆分占用，剩余段仍可继续预约

