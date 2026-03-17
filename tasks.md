# 开发任务清单

## 2026-03-16 后端恢复与前端联调

### 后端 (Backend)
- [x] 恢复 `pom.xml` 和 `application.yml`
- [x] 恢复实体类 (`User`, `CounselorProfile`, `StudentProfile`, `ConsultAppointment`)
- [x] 恢复 Mapper 接口与 XML
- [x] 恢复 Service 实现 (`AuthServiceImpl`, `ConsultServiceImpl`)
- [x] 恢复 Controller (`AuthController`, `ConsultController`)
- [x] 恢复 Security 配置 (`JwtAuthenticationFilter`, `SecurityConfig`)
- [x] 恢复全局异常处理 (`GlobalExceptionHandler`)
- [x] 添加 `docker-compose.yml` 用于本地启动 MySQL/Redis
- [x] 修复 Redis 序列化 Bug (LocalDateTime 支持)
- [x] 修复 500 内部错误 (时间戳类型转换与幂等性 ID 类型兼容)

### 前端 (Frontend)
- [x] 配置 `vite.config.ts` 反向代理 (`/api` -> `http://localhost:8080`)
- [x] 完善 `http.ts` 拦截器 (Authorization Header)
- [x] 修正 `auth.ts` 类型 definition (LoginResp, UserMeResp 字段对齐)
- [x] 优化 `LoginPage.vue` 登录逻辑 (自动识别角色并跳转，移除手动选择)
- [x] 实现 `StudentHome.vue` 咨询师列表展示与搜索
- [x] 实现 `StudentHome.vue` 在线预约弹窗逻辑
- [x] 实现 `MyAppointments.vue` 我的预约列表与取消功能

## 2026-03-17 注册功能开发（P0）

### 后端 (Backend)
- [x] 实现 `POST /api/v1/auth/register/student` 接口 (含 User/StudentProfile 关联入库)
- [x] 实现 `POST /api/v1/auth/register/counselor` 接口 (含 User/CounselorProfile 关联入库)
- [x] 增加注册字段校验逻辑 (用户名重复、密码强度、必填项校验)

### 前端 (Frontend)
- [x] 在登录页增加“学生注册”入口及弹窗表单
- [x] 在登录页增加“咨询师申请”入口及弹窗表单
- [x] 联调学生与咨询师注册接口，注册成功后提示并跳转登录

## 2026-03-17 内容治理、审计与优化 (P1/Governance/Polish)

### 第一阶段：内容治理与合规 (P1)
- [x] **管理端**：在咨询记录详情页增加“下架”按钮及原因填写弹窗。
- [x] **管理端**：实现咨询管理列表页 (`AdminConsultations.vue`)。
- [x] **学生/咨询师端**：当内容 `hidden=true` 时，展示“该内容已因违规被下架”的占位符。
- [x] **学生/咨询师端**：在列表页展示“已下架”标签。
- [x] **后端**：在 AdminController 中增加 hideThread 和 hideMessage 接口。
- [x] **后端**：在 AdminService 中调用现有的隐藏逻辑，并记录 hiddenReason。

### 第二阶段：审计日志系统 (Governance)
- [x] **前端**：新增“审计日志”页面 (`AdminAuditLogs.vue`)，支持按用户、操作类型筛选查看。
- [x] **前端**：配置审计日志路由与菜单项。
- [x] **后端**：执行 V6__audit_log.sql 迁移，建立 audit_log 表。
- [x] **后端**：实现 AOP 或 Service 层埋点，记录登录、退出、密码重置、取消预约、结束会话等关键操作。
- [x] **后端**：提供 GET /api/v1/admin/audit-logs 分页查询接口。

### 第三阶段：体验集成与回归测试 (Polish)
- [x] **全局 Layout**：顶部导航栏集成通知铃铛 (`NotificationBell.vue`)，显示未读红点，点击跳转通知中心。
- [x] **整体任务**：完成跨角色的权限隔离回归测试。

## 下一步计划 (M2: 匿名咨询模块)
- [x] 后端：实现咨询线程 (`ConsultThread`) 与消息 (`ConsultMessage`) 接口
- [x] 前端：实现学生端发起咨询页面
- [x] 前端：实现学生端咨询列表与详情联调 (状态码与字段对齐)
- [x] 后端：实现咨询师端结束咨询 (`closeThread`) 接口
- [x] 前端：实现咨询师端待处理列表与回复功能
- [x] 站内通知系统集成 (含预约与咨询回复通知)

## 2026-03-17 通知模块（P0）
- [x] 后端：Notification 表/实体/Mapper/XML + 通知列表与已读接口
- [x] 后端：在预约创建/取消、咨询师回复处写入通知
- [x] 前端：通知中心列表页（学生/咨询师/管理员复用）+ 已读/全部已读

## 2026-03-17 测评模块（P1）
- [x] 后端：PHQ-9 / GAD-7 量表结构接口 + 提交评分入库 + 我的历史分页
- [x] 前端：学生端测评入口/答题/结果/历史页（对齐后端接口）

## 2026-03-17 管理端（P2 基础版）
- [x] 后端：管理员用户列表/启用禁用/重置密码接口 + 后台记录列表接口（预约/咨询/测评）+ 统计概览接口
- [x] 前端：管理员用户管理页 + 预约记录页 + 统计概览页（基础版展示）
