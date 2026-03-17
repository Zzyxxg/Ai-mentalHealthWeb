# Ai-mentalHealthWeb

心理健康咨询与测评平台（MVP）。包含：学生端、咨询师端、管理端；支持咨询会话、预约排班、测评量表、站内通知、审计日志、内容治理（下架）。

## 技术栈

**前端**
- Vue 3 + TypeScript
- Vite
- Element Plus
- Pinia + Vue Router
- ECharts（统计图表）

**后端**
- Java 17
- Spring Boot 3.3.x
- Spring Security + JWT
- MyBatis + PageHelper
- MySQL 8
- Redis（Spring Data Redis / Redisson）
- Flyway（数据库迁移）

**基础设施（本地）**
- Docker Compose（启动 MySQL / Redis）

---

## 本地部署（macOS）

### 1) 准备环境

- 安装 Git
- 安装 JDK 17
- 安装 Maven（`mvn -v` 能输出版本）
- 安装 Node.js 18+（建议 LTS）与 npm（`node -v` / `npm -v`）
- 安装 Docker Desktop（用于 `docker compose`）

### 2) 启动数据库与缓存

在项目根目录执行：

```bash
docker compose up -d
```

默认端口：
- MySQL: `3306`
- Redis: `6379`

### 3) 启动后端

```bash
cd _verify_mentalhealth_backend
mvn spring-boot:run
```

后端默认端口：`8080`

后端启动后 Flyway 会自动创建表结构。

### 4) 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认端口：`5173`

浏览器访问：
- 前端：`http://localhost:5173/`
- 后端健康检查：`http://localhost:8080/api/v1/health`

---

## 本地部署（Windows）

### 1) 准备环境

- 安装 Git
- 安装 JDK 17（并配置 `JAVA_HOME`）
- 安装 Maven（并配置环境变量，`mvn -v` 能输出版本）
- 安装 Node.js 18+（建议 LTS）与 npm
- 安装 Docker Desktop（启用 WSL2 推荐）

### 2) 启动数据库与缓存

在项目根目录使用 PowerShell 执行：

```powershell
docker compose up -d
```

### 3) 启动后端

```powershell
cd _verify_mentalhealth_backend
mvn spring-boot:run
```

### 4) 启动前端

```powershell
cd frontend
npm install
npm run dev
```

---

## 生产构建（可选）

### 前端

```bash
cd frontend
npm install
npm run build
```

产物在 `frontend/dist/`。

### 后端

```bash
cd _verify_mentalhealth_backend
mvn -DskipTests package
```

产物在 `_verify_mentalhealth_backend/target/*.jar`。

---

## 初始化账号（开发环境）

后端启动时会自动初始化一些测试数据（见 `DataInitializer`）：

- 学生账号：`demo` / `demo1234`
- 咨询师账号：`counselor1` / `123456`（还有 `counselor2`、`counselor3`）
- 批量学生账号：`student001` ~ `student100` / `123456`
- 批量咨询师账号：`counselor001` ~ `counselor050` / `123456`
- 管理员账号：`admin01` ~ `admin03` / `123456`

说明：初始化逻辑会跳过已存在的账号，不会重复插入。

---

## 常见问题

- MySQL 端口被占用：修改 `docker-compose.yml` 的端口映射或停止本机已有 MySQL。
- 前端接口 404：确认前端 dev server 代理已启用（`frontend/vite.config.ts`）且后端在 `8080` 运行。
- 登录后 401：确认后端 `JWT` 配置与前端 `Authorization` Header 拦截器正常。
