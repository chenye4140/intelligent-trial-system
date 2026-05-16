# 部署文档

本文档详细说明 AI 纪检监察智能审理一体化平台的部署流程，涵盖开发环境、Docker Compose 一键部署和生产环境三种部署方式。

---

## 目录

- [1. 开发环境部署](#1-开发环境部署)
- [2. Docker Compose 一键部署](#2-docker-compose-一键部署)
- [3. 生产环境部署](#3-生产环境部署)

---

## 1. 开发环境部署

### 1.1 环境要求

| 软件 | 最低版本 | 安装方式 |
|------|----------|----------|
| JDK | 1.8.0_201+ | `apt install openjdk-8-jdk` 或下载 Oracle JDK |
| Maven | 3.6+ | `apt install maven` 或 [下载](https://maven.apache.org/) |
| Node.js | 22 LTS | 使用 [nvm](https://github.com/nvm-sh/nvm) 安装 |
| MySQL | 8.0 | `apt install mysql-server` 或 Docker |
| Redis | 7.x | `apt install redis-server` 或 Docker |
| MinIO | latest | Docker 运行 |

#### 验证安装

```bash
java -version        # 应显示 1.8.x
mvn -version         # 应显示 3.6+
node -v              # 应显示 v22.x
mysql --version      # 应显示 8.0.x
redis-cli --version  # 应显示 7.x
```

### 1.2 数据库初始化

```bash
# 方式一：使用初始化脚本
mysql -u root -p < sql/init.sql

# 方式二：手动执行
mysql -u root -p
source sql/init.sql;
exit;
```

初始化脚本会自动完成：
- 创建数据库 `intelligent_trial`（utf8mb4 编码）
- 创建所有业务表（权限管理、定密管理、多库管理、文档解析、审计日志等）
- 插入初始化数据：
  - 5 级密级字典（绝密/机密/秘密/内部/公开）
  - 默认部门（智能审理系统、审理一室、审理二室、案件监督管理室）
  - 默认角色（超级管理员、审理人员、查看人员）
  - 默认管理员用户（admin / admin123）
  - 系统菜单及按钮权限

### 1.3 Maven 编译打包

```bash
# 进入项目根目录
cd /home/chenye/intelligent-trial-system

# 清理并编译（跳过测试）
mvn clean package -DskipTests

# 如需同时编译所有模块
mvn clean install -DskipTests

# 仅编译后端模块
cd backend && mvn clean package -DskipTests
```

编译成功后，JAR 包位于：
```
backend/target/intelligent-trial-system-1.0.0-SNAPSHOT.jar
```

### 1.4 配置修改

编辑 `backend/src/main/resources/application.yml`，确保以下配置正确：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/intelligent_trial?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    username: trial_user
    password: trial_pass

  data:
    redis:
      host: localhost
      port: 6379
      password: redis123456

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minio123456
  bucket-name: trial-documents

milvus:
  host: localhost
  port: 19530
```

也可以通过环境变量覆盖：

```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/intelligent_trial?..."
export SPRING_DATASOURCE_USERNAME="trial_user"
export SPRING_DATASOURCE_PASSWORD="trial_pass"
export SPRING_DATA_REDIS_HOST="localhost"
export SPRING_DATA_REDIS_PASSWORD="redis123456"
export MINIO_ENDPOINT="http://localhost:9000"
export MINIO_ACCESS_KEY="minioadmin"
export MINIO_SECRET_KEY="minio123456"
export MILVUS_HOST="localhost"
export MILVUS_PORT="19530"
```

### 1.5 后端服务启动

```bash
# 方式一：使用 java -jar
java -jar backend/target/intelligent-trial-system-1.0.0-SNAPSHOT.jar

# 方式二：使用 Maven 插件（开发模式，支持热部署）
cd backend && mvn spring-boot:run

# 方式三：后台运行
nohup java -jar backend/target/intelligent-trial-system-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=dev \
  > app.log 2>&1 &
```

启动成功后，访问：
- API 地址：http://localhost:8080/api
- 健康检查：http://localhost:8080/api/actuator/health（如启用）

### 1.6 前端构建

```bash
cd frontend

# 安装依赖
npm install

# 开发模式（带热重载）
npm run dev

# 生产构建
npm run build

# 构建产物位于 frontend/dist/
```

前端开发服务器默认地址：http://localhost:5173

### 1.7 各服务启动顺序

```
1. MySQL     → 数据库服务
2. Redis     → 缓存服务
3. MinIO     → 对象存储
4. Milvus    → 向量数据库（可选，第一阶段可暂不启动）
5. Backend   → Spring Boot 后端
6. Frontend  → Vue 前端
```

### 1.8 启动检查清单

```bash
# 检查 MySQL
mysql -u trial_user -ptrial_pass -e "SHOW DATABASES;" | grep intelligent_trial

# 检查 Redis
redis-cli -a redis123456 ping

# 检查 MinIO
curl http://localhost:9000/minio/health/live

# 检查后端
curl http://localhost:8080/api/

# 检查前端
curl http://localhost:5173/
```

---

## 2. Docker Compose 一键部署

### 2.1 前置条件

- Docker Engine 20.10+
- Docker Compose V2（`docker compose` 命令）

### 2.2 一键启动基础设施

```bash
# 进入项目根目录
cd /home/chenye/intelligent-trial-system

# 启动所有基础设施服务（MySQL + Redis + MinIO）
docker compose up -d

# 查看服务状态
docker compose ps

# 查看服务日志
docker compose logs -f
```

### 2.3 包含的服务

| 服务 | 镜像 | 端口 | 说明 |
|------|------|------|------|
| MySQL | mysql:8.0 | 3306 | 关系型数据库，自动创建 intelligent_trial 数据库 |
| Redis | redis:7-alpine | 6379 | 缓存与会话存储 |
| MinIO | minio/minio:latest | 9000 (API), 9001 (Console) | 对象存储 |

### 2.4 数据持久化

所有数据通过 Docker Volume 持久化，删除容器不会丢失数据：

| Volume | 挂载路径 | 说明 |
|--------|----------|------|
| mysql_data | /var/lib/mysql | MySQL 数据目录 |
| redis_data | /data | Redis 数据目录 |
| minio_data | /data | MinIO 数据目录 |

### 2.5 停止与清理

```bash
# 停止所有服务
docker compose stop

# 停止并删除容器（保留数据卷）
docker compose down

# 停止并删除容器和数据卷（⚠️ 数据将丢失）
docker compose down -v

# 删除所有相关镜像
docker compose down --rmi all
```

### 2.6 连接外部服务

启动后，外部应用可通过以下方式连接：

```bash
# MySQL
mysql -h 127.0.0.1 -P 3306 -u trial_user -ptrial_pass intelligent_trial

# Redis
redis-cli -h 127.0.0.1 -p 6379 -a redis123456

# MinIO API
# 端点: http://127.0.0.1:9000
# Access Key: minioadmin
# Secret Key: minio123456

# MinIO 控制台
# 浏览器访问: http://127.0.0.1:9001
```

### 2.7 完整部署流程

```bash
# Step 1: 启动基础设施
docker compose up -d

# Step 2: 等待 MySQL 就绪（约 15-30 秒）
docker compose logs mysql | grep "ready for connections"

# Step 3: 初始化数据库（init.sql 已自动执行）
# 如需手动执行：
# docker cp sql/init.sql trial-mysql:/tmp/init.sql
# docker exec trial-mysql mysql -u root -proot123456 < /tmp/init.sql

# Step 4: 编译后端
mvn clean package -DskipTests

# Step 5: 启动后端（连接 Docker 中的 MySQL/Redis/MinIO）
SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/intelligent_trial?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai" \
SPRING_DATASOURCE_USERNAME="trial_user" \
SPRING_DATASOURCE_PASSWORD="trial_pass" \
SPRING_DATA_REDIS_HOST="localhost" \
SPRING_DATA_REDIS_PASSWORD="redis123456" \
MINIO_ENDPOINT="http://localhost:9000" \
MINIO_ACCESS_KEY="minioadmin" \
MINIO_SECRET_KEY="minio123456" \
java -jar backend/target/intelligent-trial-system-1.0.0-SNAPSHOT.jar

# Step 6: 启动前端
cd frontend && npm install && npm run dev
```

---

## 3. 生产环境部署

### 3.1 硬件配置建议

#### 最低配置

| 组件 | 配置 | 说明 |
|------|------|------|
| CPU | 4 核 | 支持并发请求 |
| 内存 | 8 GB | JVM + 应用 + 缓存 |
| 磁盘 | 100 GB SSD | 数据库 + 文件存储 |
| 网络 | 100 Mbps | 内网部署 |

#### 推荐配置

| 组件 | 配置 | 说明 |
|------|------|------|
| CPU | 8 核+ | 支持高并发与 AI 处理 |
| 内存 | 16 GB+ | 充足堆内存与缓存 |
| 磁盘 | 500 GB SSD | 大容量文件存储 |
| 网络 | 1 Gbps | 高速内网 |

#### 大规模部署（100+ 并发）

| 组件 | 配置 |
|------|------|
| 应用服务器 | 4 台 × 8 核 16GB |
| MySQL | 主从架构，8 核 32GB，SSD 1TB |
| Redis | Cluster 模式，3 主 3 从 |
| MinIO | 分布式部署，4 节点 |
| Milvus | 独立集群，8 核 32GB |
| 负载均衡 | Nginx / HAProxy / SLB |

### 3.2 高可用架构说明

```
                    ┌─────────────┐
                    │   Nginx LB  │
                    │  (主 + 备)  │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
        ┌─────▼─────┐ ┌───▼────┐ ┌─────▼─────┐
        │  Backend  │ │Backend │ │  Backend  │
        │  Node 1   │ │Node 2  │ │  Node 3   │
        └─────┬─────┘ └───┬────┘ └─────┬─────┘
              │            │            │
              └────────────┼────────────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
    ┌─────▼─────┐   ┌─────▼─────┐   ┌──────▼──────┐
    │   MySQL   │   │   Redis   │   │    MinIO    │
    │  主从架构  │   │  Cluster  │   │ 分布式存储  │
    └───────────┘   └───────────┘   └─────────────┘
```

#### 关键高可用措施

1. **应用层**
   - 多实例部署，无状态设计
   - 通过 Nginx/SLB 负载均衡
   - 健康检查 + 自动故障转移

2. **数据库层**
   - MySQL 主从复制 + MHA/Orchestrator 自动切换
   - 定期全量 + 增量备份
   - 读写分离（可选）

3. **缓存层**
   - Redis Cluster 模式
   - 多副本保证数据不丢失

4. **存储层**
   - MinIO 分布式模式（纠删码）
   - 跨机房备份

5. **网络层**
   - VPC 内网隔离
   - WAF 防火墙
   - HTTPS 加密传输

### 3.3 K8s 部署建议

#### 架构概览

```yaml
# 推荐使用 Helm Chart 或 Kustomize 管理
# 命名空间: intelligent-trial
#
# 部署清单:
# - Deployment: backend (3 副本)
# - Deployment: frontend (2 副本)
# - StatefulSet: mysql (主从)
# - StatefulSet: redis (Cluster)
# - StatefulSet: minio (分布式)
# - StatefulSet: milvus (分布式)
# - Service: 各服务 ClusterIP
# - Ingress: 统一入口 + TLS
# - ConfigMap: 应用配置
# - Secret: 敏感信息（密码、密钥）
# - HPA: 自动扩缩容
# - CronJob: 数据库备份
```

#### 基础配置示例

```yaml
# backend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: intelligent-trial-backend
  namespace: intelligent-trial
spec:
  replicas: 3
  selector:
    matchLabels:
      app: intelligent-trial-backend
  template:
    metadata:
      labels:
        app: intelligent-trial-backend
    spec:
      containers:
        - name: backend
          image: registry.example.com/intelligent-trial-backend:1.0.0
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "prod"
            - name: SPRING_DATASOURCE_URL
              valueFrom:
                configMapKeyRef:
                  name: app-config
                  key: db-url
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: app-secret
                  key: db-password
          resources:
            requests:
              cpu: "500m"
              memory: "512Mi"
            limits:
              cpu: "2000m"
              memory: "2Gi"
          readinessProbe:
            httpGet:
              path: /api/actuator/health/readiness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /api/actuator/health/liveness
              port: 8080
            initialDelaySeconds: 60
            periodSeconds: 15
---
apiVersion: v1
kind: Service
metadata:
  name: intelligent-trial-backend
  namespace: intelligent-trial
spec:
  type: ClusterIP
  ports:
    - port: 8080
      targetPort: 8080
  selector:
    app: intelligent-trial-backend
```

#### 生产环境变量清单

| 变量名 | 说明 | 示例值 |
|--------|------|--------|
| SPRING_PROFILES_ACTIVE | 运行环境 | prod |
| SPRING_DATASOURCE_URL | 数据库连接串 | jdbc:mysql://mysql-svc:3306/... |
| SPRING_DATASOURCE_USERNAME | 数据库用户名 | trial_user |
| SPRING_DATASOURCE_PASSWORD | 数据库密码 | (从 Secret 注入) |
| SPRING_DATA_REDIS_HOST | Redis 地址 | redis-svc |
| SPRING_DATA_REDIS_PASSWORD | Redis 密码 | (从 Secret 注入) |
| MINIO_ENDPOINT | MinIO 地址 | http://minio-svc:9000 |
| MINIO_ACCESS_KEY | MinIO 密钥 | (从 Secret 注入) |
| MINIO_SECRET_KEY | MinIO 密钥 | (从 Secret 注入) |
| MILVUS_HOST | Milvus 地址 | milvus-svc |
| MILVUS_PORT | Milvus 端口 | 19530 |

### 3.4 安全加固建议

1. **网络安全**
   - 仅开放必要端口（80/443）
   - 数据库、Redis、MinIO 不暴露公网
   - 使用 VPC 内网通信

2. **数据安全**
   - 所有密码使用强密码策略
   - 敏感配置使用 K8s Secret / Vault
   - 数据库定期备份
   - 传输层使用 HTTPS

3. **应用安全**
   - 启用 CORS 白名单
   - JWT/Token 有效期控制
   - SQL 注入防护（MyBatis-Plus 预编译）
   - XSS 防护
   - 操作审计日志

4. **监控告警**
   - 接入 Prometheus + Grafana
   - 应用日志接入 ELK
   - 关键指标告警（CPU/内存/磁盘/响应时间）
   - 数据库慢查询监控

### 3.5 备份策略

```bash
# MySQL 定时备份（CronJob）
mysqldump -u root -p intelligent_trial | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz

# MinIO 数据备份
mc mirror local-backup/ minio/trial-documents/

# 建议保留周期
# - 每日增量备份，保留 7 天
# - 每周全量备份，保留 30 天
# - 每月归档备份，保留 1 年
```

---

*最后更新: 2026-05-14*
