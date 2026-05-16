# AI 纪检监察智能审理一体化平台 — 部署文档

> **版本**: v1.0  
> **更新日期**: 2026-05-17  
> **适用环境**: Linux (CentOS 7+ / Ubuntu 20.04+)  

---

## 一、系统架构

```
┌─────────────────────────────────────────────────────────┐
│                    Nginx (反向代理)                       │
│                  端口: 80 / 443 (HTTPS)                   │
└──────────┬──────────────────────────────────────────────┘
           │
           ├── 静态文件 ──→ Vue 3 前端 (dist/)
           │
           └── API 路由 ──→ 各后端微服务
                           ├── auth:8081       (认证 + 系统管理 + 案件 + 工作流)
                           ├── document:8082   (文档解析 + 类案推送 + 向量存储)
                           └── repository:8083 (多库管理 + MinIO 文件存储)
                                                    
基础设施:
  ├── MySQL 8.0           (业务数据库)
  ├── Redis 6+            (Token 缓存 + 异步状态)
  └── MinIO               (文档对象存储)
```

### 模块清单

| 模块 | 端口 | 说明 | 状态 |
|------|------|------|------|
| common | - | 公共组件（R/PageResult/BaseEntity/异常处理） | ✅ |
| auth | 8081 | 登录认证 + RBAC + 五级定密 + 审计日志 | ✅ |
| casemanage | 8081 | 案件管理 CRUD + 当事人 + 违纪事实 | ✅ |
| workflow | 8081 | Flowable 审批流（4 步审批） | ✅ |
| promotion | 8081 | 以案促改 AI 分析 | ✅ |
| document | 8082 | 文档解析(POI/PDFBox) + 类案推送 + 向量检索 | ✅ |
| repository | 8083 | 文档上传下载 + 目录树 + 搜索 + Excel | ✅ |
| report | 8084 | AI 文书生成（DeepSeek） | ✅ |
| api-gateway | - | Spring Cloud Gateway（可选） | ⚠️ 骨架 |
| frontend | 3000 | Vue 3 + Element Plus + Vite | ✅ |

### 技术栈

- **后端**: Spring Boot 2.7.18 + Java 8 + MyBatis-Plus 3.5.5
- **前端**: Vue 3.4 + Element Plus 2.6 + Vite 5.2
- **数据库**: MySQL 8.0 + Redis 6+
- **存储**: MinIO 8.5.9
- **AI**: DeepSeek deepseek-v4-pro + DashScope（通义千问 qwen-plus / qwen-vl-max）
- **工作流**: Flowable 6.8.0

---

## 二、环境准备

### 2.1 系统要求

| 组件 | 最低配置 | 推荐配置 |
|------|---------|---------|
| CPU | 4 核 | 8 核 |
| 内存 | 8 GB | 16 GB |
| 磁盘 | 50 GB | 100 GB SSD |
| JDK | 1.8 (OpenJDK 8) | 1.8.0_392+ |
| Maven | 3.6+ | 3.9.x |
| Node.js | 18+ | 20 LTS |

### 2.2 安装依赖

```bash
# === Ubuntu/Debian ===
sudo apt update
sudo apt install -y openjdk-8-jdk maven mysql-server redis-server nginx

# === CentOS/RHEL ===
sudo yum install -y java-1.8.0-openjdk-devel maven mysql-server redis nginx

# 验证安装
java -version    # 应显示 1.8.x
mvn -version     # 应显示 3.6+
mysql --version  # 应显示 8.0.x
redis-cli ping   # 应返回 PONG
```

### 2.3 安装 MinIO

```bash
# 下载 MinIO
wget https://dl.min.io/server/minio/release/linux-amd64/minio
chmod +x minio
sudo mv minio /usr/local/bin/

# 创建数据目录
sudo mkdir -p /data/minio
sudo chown -R $USER:$USER /data/minio

# 启动 MinIO（生产环境建议使用 systemd 服务）
export MINIO_ROOT_USER=minioadmin
export MINIO_ROOT_PASSWORD=minioadmin
minio server /data/minio --address ":9000" --console-address ":9001" &

# 访问控制台: http://<服务器IP>:9001
# 创建 bucket: trial-documents 和 intelligent-trial
```

### 2.4 初始化数据库

```bash
# 启动 MySQL
sudo systemctl start mysql    # Ubuntu
sudo systemctl start mysqld   # CentOS

# 创建数据库
mysql -u root -p << 'EOF'
CREATE DATABASE IF NOT EXISTS intelligent_trial 
  DEFAULT CHARACTER SET utf8mb4 
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS trial_repository 
  DEFAULT CHARACTER SET utf8mb4 
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS trial_document 
  DEFAULT CHARACTER SET utf8mb4 
  DEFAULT COLLATE utf8mb4_unicode_ci;
EOF

# 执行初始化脚本（含 RBAC 基础数据）
mysql -u root -p intelligent_trial < sql/init.sql
```

### 2.5 环境变量

创建 `/etc/profile.d/intelligent-trial.sh`：

```bash
#!/bin/bash
# AI 纪检监察智能审理平台环境变量

# DeepSeek API Key（文书生成 + 定密建议 + 以案促改）
export DEEPSEEK_API_KEY="sk-your-deepseek-api-key"

# DashScope API Key（文档分类 + OCR + 向量嵌入）
export DASHSCOPE_API_KEY="sk-your-dashscope-api-key"

# JWT Secret（生产环境务必更换！）
export JWT_SECRET="your-production-jwt-secret-change-this"
```

```bash
source /etc/profile.d/intelligent-trial.sh
```

---

## 三、后端编译与部署

### 3.1 编译

```bash
cd /home/chenye/intelligent-trial-system

# 编译所有模块（跳过测试以加速）
mvn clean package -DskipTests -q

# 或编译并运行测试
mvn clean package -q
```

编译产物位于各模块的 `target/` 目录：

| 模块 | JAR 文件 |
|------|---------|
| auth | `auth/target/intelligent-trial-auth-1.0.0-SNAPSHOT.jar` |
| document | `document/target/intelligent-trial-document-1.0.0-SNAPSHOT.jar` |
| repository | `repository/target/intelligent-trial-repository-1.0.0-SNAPSHOT.jar` |
| report | `report/target/intelligent-trial-report-1.0.0-SNAPSHOT.jar` |

### 3.2 配置修改（生产环境）

各模块的 `application.yml` 需要修改以下配置项：

**auth (8081)**:
```yaml
spring:
  datasource:
    url: jdbc:mysql://<DB_HOST>:3306/intelligent_trial?...
    username: <DB_USER>
    password: <DB_PASSWORD>
  redis:
    host: <REDIS_HOST>
    password: <REDIS_PASSWORD>

jwt:
  secret: ${JWT_SECRET}  # 务必使用强随机密钥
```

**document (8082)**:
```yaml
spring:
  datasource:
    url: jdbc:mysql://<DB_HOST>:3306/trial_document?...
    username: <DB_USER>
    password: <DB_PASSWORD>

minio:
  endpoint: http://<MINIO_HOST>:9000
  access-key: <MINIO_ACCESS_KEY>
  secret-key: <MINIO_SECRET_KEY>

dashscope:
  api-key: ${DASHSCOPE_API_KEY}
  deepseek-api-key: ${DEEPSEEK_API_KEY}
```

**repository (8083)**:
```yaml
spring:
  datasource:
    url: jdbc:mysql://<DB_HOST>:3306/trial_repository?...
    username: <DB_USER>
    password: <DB_PASSWORD>

minio:
  endpoint: http://<MINIO_HOST>:9000
  access-key: <MINIO_ACCESS_KEY>
  secret-key: <MINIO_SECRET_KEY>
```

### 3.3 启动服务

推荐使用 systemd 管理各服务：

**创建 service 文件**:

```bash
# /etc/systemd/system/trial-auth.service
[Unit]
Description=AI Trial System - Auth Service
After=network.target mysql.service redis.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/intelligent-trial
ExecStart=/usr/bin/java -jar auth/target/intelligent-trial-auth-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=prod
Restart=on-failure
RestartSec=10
Environment=DEEPSEEK_API_KEY=${DEEPSEEK_API_KEY}
Environment=DASHSCOPE_API_KEY=${DASHSCOPE_API_KEY}

[Install]
WantedBy=multi-user.target
```

```bash
# 复制并修改其他服务的 service 文件
# trial-document.service (document:8082)
# trial-repository.service (repository:8083)
# trial-report.service (report:8084)

# 启用并启动
sudo systemctl daemon-reload
sudo systemctl enable trial-auth trial-document trial-repository trial-report
sudo systemctl start trial-auth trial-document trial-repository trial-report

# 查看状态
sudo systemctl status trial-auth
```

**手动启动（开发/调试）**:

```bash
# 各模块目录下执行
cd auth && java -jar target/intelligent-trial-auth-1.0.0-SNAPSHOT.jar &
cd document && java -jar target/intelligent-trial-document-1.0.0-SNAPSHOT.jar &
cd repository && java -jar target/intelligent-trial-repository-1.0.0-SNAPSHOT.jar &
cd report && java -jar target/intelligent-trial-report-1.0.0-SNAPSHOT.jar &
```

### 3.4 健康检查

```bash
# 检查各服务端口是否监听
netstat -tlnp | grep -E '8081|8082|8083|8084'

# 测试登录接口
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 预期返回:
# {"code":200,"msg":"登录成功","data":{"accessToken":"...","refreshToken":"..."}}
```

---

## 四、前端部署

### 4.1 构建

```bash
cd /home/chenye/intelligent-trial-system/frontend

# 安装依赖（首次）
npm install

# 生产构建
npm run build
```

构建产物在 `frontend/dist/` 目录。

### 4.2 Nginx 配置

```nginx
# /etc/nginx/conf.d/intelligent-trial.conf
upstream auth_backend {
    server 127.0.0.1:8081;
}

upstream document_backend {
    server 127.0.0.1:8082;
}

upstream repository_backend {
    server 127.0.0.1:8083;
}

upstream report_backend {
    server 127.0.0.1:8084;
}

server {
    listen 80;
    server_name your-domain.com;  # 替换为实际域名

    # 前端静态文件
    root /opt/intelligent-trial/frontend/dist;
    index index.html;

    # SPA 路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 认证 & 系统管理
    location /api/auth/ {
        proxy_pass http://auth_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 系统管理
    location /api/system/ {
        proxy_pass http://auth_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 案件管理
    location /api/case/ {
        proxy_pass http://auth_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 工作流
    location /api/workflow/ {
        proxy_pass http://auth_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 以案促改
    location /api/promotion/ {
        proxy_pass http://auth_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 文档解析 & 类案推送
    location /api/document/ {
        proxy_pass http://document_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 多库管理
    location /api/repository/ {
        proxy_pass http://repository_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 文书生成
    location /api/report/ {
        proxy_pass http://report_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 文件上传大小限制
    client_max_body_size 100m;
}
```

```bash
# 检查配置并重启
sudo nginx -t
sudo systemctl restart nginx
```

### 4.3 HTTPS 配置（可选）

```bash
# 使用 Let's Encrypt 免费证书
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

---

## 五、Docker 部署（可选）

### 5.1 docker-compose.yml

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: intelligent_trial
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql/init.sql:/docker-entrypoint-initdb.d/init.sql
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  minio:
    image: minio/minio:latest
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio_data:/data
    command: server /data --console-address ":9001"

  auth:
    build:
      context: .
      dockerfile: Dockerfile.auth
    ports:
      - "8081:8081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/intelligent_trial?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
      SPRING_REDIS_HOST: redis
      DEEPSEEK_API_KEY: ${DEEPSEEK_API_KEY}
      DASHSCOPE_API_KEY: ${DASHSCOPE_API_KEY}
    depends_on:
      - mysql
      - redis

  document:
    build:
      context: .
      dockerfile: Dockerfile.document
    ports:
      - "8082:8082"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/trial_document?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
      MINIO_ENDPOINT: http://minio:9000
      DEEPSEEK_API_KEY: ${DEEPSEEK_API_KEY}
      DASHSCOPE_API_KEY: ${DASHSCOPE_API_KEY}
    depends_on:
      - mysql
      - minio

  repository:
    build:
      context: .
      dockerfile: Dockerfile.repository
    ports:
      - "8083:8083"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/trial_repository?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
      MINIO_ENDPOINT: http://minio:9000
    depends_on:
      - mysql
      - minio

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/conf.d/default.conf
      - ./frontend/dist:/usr/share/nginx/html
    depends_on:
      - auth
      - document
      - repository

volumes:
  mysql_data:
  redis_data:
  minio_data:
```

### 5.2 启动

```bash
# 设置环境变量
export DEEPSEEK_API_KEY="sk-your-key"
export DASHSCOPE_API_KEY="sk-your-key"

# 构建并启动
docker-compose up -d

# 查看日志
docker-compose logs -f auth
```

---

## 六、安全配置清单

### 6.1 必须修改项

| 配置项 | 位置 | 说明 |
|--------|------|------|
| JWT Secret | `auth/application.yml` | 使用 `openssl rand -base64 64` 生成 |
| 数据库密码 | 各模块 `application.yml` | 禁止使用默认 `root/root` |
| Redis 密码 | `auth/application.yml` | 设置 `requirepass` |
| MinIO 密钥 | `repository/document/application.yml` | 修改 `MINIO_ROOT_USER/PASSWORD` |
| 默认管理员密码 | `sql/init.sql` | 首次登录后立即修改 |
| DEEPSEEK_API_KEY | 环境变量 | 妥善保管，不要提交到代码库 |

### 6.2 推荐加固项

- [ ] 开启 MySQL 二进制日志审计
- [ ] Redis 配置 `bind 127.0.0.1` 限制访问
- [ ] MinIO 启用 HTTPS
- [ ] Nginx 配置 HTTPS + HSTS
- [ ] 配置防火墙仅开放必要端口（80/443）
- [ ] 定期备份数据库（`mysqldump`）
- [ ] 开启审计日志（系统内置）

---

## 七、常用运维命令

### 7.1 服务管理

```bash
# 查看服务状态
sudo systemctl status trial-auth trial-document trial-repository trial-report

# 重启单个服务
sudo systemctl restart trial-auth

# 查看日志
journalctl -u trial-auth -f --no-pager

# 查看最近的错误日志
journalctl -u trial-auth --since "1 hour ago" --priority=err
```

### 7.2 数据库维护

```bash
# 备份
mysqldump -u root -p --databases intelligent_trial trial_repository trial_document \
  > backup_$(date +%Y%m%d_%H%M%S).sql

# 恢复
mysql -u root -p < backup_20260517.sql

# 检查表
mysqlcheck -u root -p --all-databases
```

### 7.3 日志位置

```bash
# 如果使用 systemd，日志在 journald
journalctl -u trial-auth

# 如果直接 java -jar 启动，日志在终端输出
# 建议配置 logging.file.path 将日志写入文件
```

---

## 八、故障排查

### 8.1 登录失败

```bash
# 1. 检查 auth 服务是否运行
curl http://localhost:8081/api/auth/captcha

# 2. 检查数据库连接
mysql -u root -p -e "SELECT COUNT(*) FROM intelligent_trial.sys_user"

# 3. 检查 Redis 连接
redis-cli ping

# 4. 检查 JWT 配置
# 确保 auth/application.yml 中 jwt.secret 已正确配置
```

### 8.2 文件上传失败

```bash
# 1. 检查 MinIO 是否运行
curl http://localhost:9000/minio/health/live

# 2. 检查 bucket 是否存在
# 访问 MinIO 控制台 http://localhost:9001

# 3. 检查 application.yml 中的 MinIO 配置
```

### 8.3 AI 功能不工作

```bash
# 1. 检查 API Key 是否配置
echo $DEEPSEEK_API_KEY
echo $DASHSCOPE_API_KEY

# 2. 检查网络连接
curl -H "Authorization: Bearer $DEEPSEEK_API_KEY" \
  https://api.deepseek.com/v1/models

# 3. 查看应用日志中是否有 AI 调用错误
journalctl -u trial-auth | grep -i "deepseek\|error"
```

---

## 九、默认账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 超级管理员 | admin | admin123 | 首次登录后务必修改 |
| 审理员 | reviewer | reviewer123 | 案件审理权限 |
| 领导 | leader | leader123 | 审批权限 |

> ⚠️ **生产环境必须修改默认密码！** 初始化 SQL 中已包含 BCrypt 加密的密码。

---

## 十、更新记录

| 日期 | 版本 | 说明 |
|------|------|------|
| 2026-05-17 | v1.0 | 初始部署文档，覆盖全模块部署流程 |
