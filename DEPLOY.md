# AI 纪检监察智能审理一体化平台 - 部署文档

## 一、系统要求

| 组件 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 1.8+ | 推荐使用 Oracle JDK 8 或 OpenJDK 8 |
| Maven | 3.6+ | 构建工具 |
| MySQL | 5.7+ / 8.0+ | 关系型数据库 |
| Redis | 6.0+ | 缓存服务 |
| MinIO | 最新稳定版 | 对象存储 |
| Node.js | 16+ | 前端构建 |

## 二、数据库初始化

### 2.1 创建数据库

```sql
CREATE DATABASE intelligent_trial DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'trial_user'@'%' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON intelligent_trial.* TO 'trial_user'@'%';
FLUSH PRIVILEGES;
```

### 2.2 执行初始化脚本

```bash
mysql -u trial_user -p intelligent_trial < sql/init.sql
```

初始化脚本会创建以下 17 张表：
- `sys_user`, `sys_role`, `sys_menu`, `sys_dept` — RBAC 基础表
- `sys_user_role`, `sys_role_menu` — 关联表
- `sys_audit_log`, `sys_classification_level` — 系统表
- `case_info`, `case_party`, `case_violation_fact` — 案件管理表
- `doc_paragraph_vector`, `classification_suggestion`, `case_promotion` — AI 能力表
- `report_template`, `report_record`, `report_paragraph_vector` — 文书生成表
- 文档表 `document`, `directory`（repository 模块）

默认管理员账号：`admin / admin123`

## 三、后端部署

### 3.1 配置文件

每个模块需要配置 `application.yml`，关键配置项：

```yaml
# 数据库
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/intelligent_trial?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4
    username: trial_user
    password: your_secure_password
  redis:
    host: localhost
    port: 6379

# JWT
jwt:
  secret: your-256-bit-base64-encoded-secret-key-here
  expiration: 86400000        # 24 小时
  refresh-expiration: 604800000  # 7 天

# AI 配置（DeepSeek）
ai:
  deepseek:
    api-key: ${DEEPSEEK_API_KEY}
    base-url: https://api.deepseek.com
    model: deepseek-v4-pro

# DashScope（文档解析用）
dashscope:
  api-key: ${DASHSCOPE_API_KEY}

# MinIO
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: intelligent-trial
```

### 3.2 构建

```bash
cd /home/chenye/intelligent-trial-system
mvn clean package -DskipTests
```

各模块输出 jar 包位置：
- `auth/target/intelligent-trial-auth-1.0.0-SNAPSHOT.jar` — 端口 8081
- `case/target/intelligent-trial-case-1.0.0-SNAPSHOT.jar` — 端口 8082
- `document/target/intelligent-trial-document-1.0.0-SNAPSHOT.jar` — 端口 8083
- `repository/target/intelligent-trial-repository-1.0.0-SNAPSHOT.jar` — 端口 8084
- `report/target/intelligent-trial-report-1.0.0-SNAPSHOT.jar` — 端口 8085
- `promotion/target/intelligent-trial-promotion-1.0.0-SNAPSHOT.jar` — 端口 8086
- `workflow/target/intelligent-trial-workflow-1.0.0-SNAPSHOT.jar` — 端口 8087

### 3.3 启动各服务

```bash
# 启动 auth 服务
nohup java -jar auth/target/intelligent-trial-auth-1.0.0-SNAPSHOT.jar \
  --spring.config.location=auth/src/main/resources/application.yml \
  > logs/auth.log 2>&1 &

# 启动 case 服务
nohup java -jar case/target/intelligent-trial-case-1.0.0-SNAPSHOT.jar \
  > logs/case.log 2>&1 &

# 以此类推启动其他服务...
```

### 3.4 使用 systemd 管理（推荐）

创建 `/etc/systemd/system/trial-auth.service`:

```ini
[Unit]
Description=AI Trial Auth Service
After=network.target mysql.service redis.service

[Service]
Type=simple
User=chenye
WorkingDirectory=/home/chenye/intelligent-trial-system
ExecStart=/usr/bin/java -jar auth/target/intelligent-trial-auth-1.0.0-SNAPSHOT.jar
Restart=on-failure
RestartSec=10
Environment=JAVA_OPTS="-Xmx512m -Xms256m"
Environment=DEEPSEEK_API_KEY=your-key-here
Environment=DASHSCOPE_API_KEY=your-key-here

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable trial-auth
sudo systemctl start trial-auth
sudo systemctl status trial-auth
```

## 四、前端部署

### 4.1 安装依赖

```bash
cd /home/chenye/intelligent-trial-system/frontend
npm install
```

### 4.2 开发模式

```bash
npm run dev
```

前端默认运行在 `http://localhost:5173`

### 4.3 生产构建

```bash
npm run build
```

产物输出到 `frontend/dist/` 目录。

### 4.4 Nginx 部署

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /home/chenye/intelligent-trial-system/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /api/ {
        proxy_pass http://localhost:8081/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # 案件管理 API
    location /api/case/ {
        proxy_pass http://localhost:8082/api/case/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 文档管理 API
    location /api/document/ {
        proxy_pass http://localhost:8083/api/document/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 文件存储 API
    location /api/repository/ {
        proxy_pass http://localhost:8084/api/repository/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 文书生成 API
    location /api/report/ {
        proxy_pass http://localhost:8085/api/report/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 以案促改 API
    location /api/promotion/ {
        proxy_pass http://localhost:8086/api/promotion/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 工作流 API
    location /api/workflow/ {
        proxy_pass http://localhost:8087/api/workflow/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 文件上传大小限制
    client_max_body_size 100m;
}
```

## 五、MinIO 部署

```bash
# 使用 Docker 部署 MinIO
docker run -d \
  --name minio \
  -p 9000:9000 \
  -p 9001:9001 \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin \
  -v /data/minio:/data \
  quay.io/minio/minio server /data --console-address ":9001"

# 创建 bucket
mc alias set local http://localhost:9000 minioadmin minioadmin
mc mb local/intelligent-trial
```

## 六、验证部署

### 6.1 健康检查

```bash
# 检查各服务是否启动
curl http://localhost:8081/actuator/health  # auth
curl http://localhost:8082/actuator/health  # case
curl http://localhost:8083/actuator/health  # document
```

### 6.2 登录验证

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

预期返回：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "expiresIn": 86400,
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员"
    }
  }
}
```

### 6.3 前端访问

打开浏览器访问 `http://localhost:5173`（开发模式）或 `http://your-domain.com`（生产模式），使用 admin/admin123 登录。

## 七、常见问题

### Q1: 启动报 "端口被占用"
检查是否有其他服务占用了端口：`lsof -i :8081`

### Q2: 登录报 "用户名或密码错误"
确认数据库已正确执行 init.sql，检查 `sys_user` 表中是否存在 admin 用户。

### Q3: Token 验证失败
确认 JWT secret 配置一致，检查 Redis 是否正常运行。

### Q4: AI 功能不工作
确认 DEEPSEEK_API_KEY 环境变量已正确设置，检查网络连接是否能访问 https://api.deepseek.com。

### Q5: 文件上传失败
确认 MinIO 服务运行正常，检查 bucket 是否已创建，确认 access-key/secret-key 配置正确。

## 八、项目架构概览

```
intelligent-trial-system/
├── common/          # 公共模块（Result/异常/基础实体）
├── auth/            # 认证授权（登录/JWT/RBAC/五级定密）— 端口 8081
├── case/            # 案件管理（CRUD/搜索/当事人/违纪事实）— 端口 8082
├── document/        # 文档解析（POI/PDFBox/LLM分类/向量入库）— 端口 8083
├── repository/      # 文件存储（MinIO上传下载/目录树/搜索）— 端口 8084
├── report/          # 文书生成（DeepSeek/模板匹配/异步生成）— 端口 8085
├── promotion/       # 以案促改（AI分析/4种分析类型/异步任务）— 端口 8086
├── workflow/        # 工作流（Flowable/4步审批流程）— 端口 8087
├── api-gateway/     # API 网关（Spring Cloud Gateway）
└── frontend/        # Vue 3 前端
```

---
*文档生成时间：2026-05-16 | 版本：1.0*
