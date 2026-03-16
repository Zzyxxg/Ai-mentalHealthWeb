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
- [ ] 前端：实现咨询师端待处理列表与回复功能
- [ ] 站内通知系统集成
