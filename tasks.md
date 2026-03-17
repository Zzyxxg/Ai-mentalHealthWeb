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
- [x] 修正 `auth.ts` 类型定义 (LoginResp, UserMeResp 字段对齐)
- [x] 优化 `LoginPage.vue` 登录逻辑 (自动识别角色并跳转，移除手动选择)
- [x] 实现 `StudentHome.vue` 咨询师列表展示与搜索
- [x] 实现 `StudentHome.vue` 在线预约弹窗逻辑
- [x] 实现 `MyAppointments.vue` 我的预约列表与取消功能

## 下一步计划 (M2: 匿名咨询模块)

- [x] 后端：实现咨询线程 (`ConsultThread`) 与消息 (`ConsultMessage`) 接口
- [x] 前端：实现学生端发起咨询页面
- [x] 前端：实现学生端咨询列表与详情联调 (状态码与字段对齐)
- [x] 后端：实现咨询师端结束咨询 (`closeThread`) 接口
- [x] 前端：实现咨询师端待处理列表与回复功能
- [ ] 站内通知系统集成

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

## 2026-03-16 咨询师端与排班管理后端开发

- [x] 后端：实现 ConsultThread 结束咨询接口
- [x] 后端：实现 ScheduleSlot 排班管理接口 (Entity, Mapper, Service, Controller + 状态更新/删除)
- [x] 后端：扩展 ConsultAppointment 接口 (支持咨询师查询、标记完成及 slotId 绑定与占用/释放)

