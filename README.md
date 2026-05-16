# AI 纪检监察智能审理一体化平台

> 基于 AI 大模型的纪检监察智能审理一体化平台，集文档解析、类案推送、处分执行、以案促改等功能于一体。

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4-green.svg)](https://vuejs.org/)
[![Java](https://img.shields.io/badge/Java-1.8-orange.svg)](https://www.oracle.com/java/)

---

## 📖 项目简介

**AI 纪检监察智能审理一体化平台** 是面向纪检监察机关设计的智能化案件审理系统。平台深度融合 AI 大模型（阿里云 DashScope）与向量检索技术（Milvus），实现案件材料的智能解析、类案自动推送、量纪辅助建议等功能，全面提升纪检监察审理工作的效率与质量。

### 核心特性

- 🔐 **统一身份认证** — 基于 RBAC 模型的权限管理，支持多级部门、多角色细粒度权限控制
- 📄 **智能文档解析** — 支持 PDF/DOCX/TXT 等多种格式，自动提取结构化信息并生成向量索引
- 📚 **多库管理** — 法规库、资料库、裁判文书库、案例库四大知识库统一管理
- 🔍 **类案推送** — 基于语义向量的类案相似度匹配，辅助审理人员精准参考
- 📋 **定密管理** — 五级密级体系（绝密/机密/秘密/内部/公开），文档自动分类定密
- 📝 **处分执行** — 处分全流程跟踪，逾期自动提醒
- 📊 **审计日志** — 全量操作审计，满足合规要求

---

## 🛠️ 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 1.8 | 运行环境 |
| Spring Boot | 2.7.18 | 核心框架 |
| Spring Cloud | 2021.0.8 | 微服务治理 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| Sa-Token | 1.37.0 | 权限认证 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7.x | 缓存与会话存储 |
| MinIO | latest | 对象存储 |
| Milvus | 2.4.0 | 向量数据库 |
| Apache Tika | 2.9.1 | 文档内容提取 |
| Apache POI | 5.2.5 | Office 文档解析 |
| Flowable | 6.8.0 | 工作流引擎 |
| Hutool | 5.8.25 | Java 工具类库 |
| FastJSON2 | 2.0.43 | JSON 序列化 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4 | 渐进式前端框架 |
| Vue Router | 4.3 | 路由管理 |
| Pinia | 2.1 | 状态管理 |
| Element Plus | 2.6 | UI 组件库 |
| Vite | 5.2 | 前端构建工具 |
| Axios | 1.6 | HTTP 客户端 |

### AI 服务

| 服务 | 说明 |
|------|------|
| DashScope (通义千问) | 阿里云大语言模型 API，用于文档理解、量纪建议生成 |
| Milvus | 语义向量检索，用于类案相似度匹配 |

---

## 📁 模块结构

本项目采用 **Maven 多模块** 架构，共 7 个模块：

```
intelligent-trial-system/
├── pom.xml                 # 父 POM（聚合模块）
├── common/                 # 公共模块：工具类、通用实体、统一返回
├── auth/                   # 认证模块（端口 8081）：登录、Token、用户信息
├── document/               # 文档模块（端口 8082）：文档解析、OCR、向量化
├── repository/             # 多库模块（端口 8083）：法规/资料/裁判/案例库
├── workflow/               # 流程模块（端口 8084）：审批流、处分执行
├── api-gateway/            # 网关模块（端口 8080）：统一入口、路由转发
├── backend/                # 单体后端（开发/第一阶段使用）
├── frontend/               # Vue 3 前端
├── sql/                    # 数据库初始化脚本
├── docker/                 # Docker 相关配置
└── docker-compose.yml      # Docker Compose 编排文件
```

### 模块端口分配

| 模块 | 端口 | 说明 |
|------|------|------|
| api-gateway | 8080 | API 网关（统一入口） |
| auth | 8081 | 认证服务 |
| document | 8082 | 文档服务 |
| repository | 8083 | 多库服务 |
| workflow | 8084 | 流程服务 |

> **注**：第一阶段采用单体部署（backend/），所有服务运行在 8080 端口。微服务拆分将在第二阶段实施。

---

## 🚀 快速开始

### 前置条件

| 软件 | 最低版本 | 推荐版本 |
|------|----------|----------|
| JDK | 1.8 | 1.8.0_391+ |
| Maven | 3.6 | 3.9.x |
| Node.js | 18 | 22 LTS |
| MySQL | 8.0 | 8.0.35 |
| Redis | 6.0 | 7.2 |
| Docker | 20.10 | 24.x |
| Docker Compose | 2.0 | 2.24 |

### 方式一：Docker Compose 一键启动（推荐）

```bash
# 1. 启动基础设施（MySQL + Redis + MinIO）
docker compose up -d

# 2. 等待服务就绪（约 30 秒）
docker compose ps

# 3. 编译后端
mvn clean package -DskipTests

# 4. 启动后端服务
java -jar backend/target/intelligent-trial-system-1.0.0-SNAPSHOT.jar

# 5. 构建并部署前端
cd frontend
npm install
npm run build
npm run dev
```

### 方式二：本地开发

```bash
# 1. 初始化数据库
mysql -u root -p < sql/init.sql

# 2. 启动 MySQL、Redis、MinIO
docker run -d --name trial-mysql -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=Root@Trial2026! \
  -e MYSQL_DATABASE=intelligent_trial \
  -e MYSQL_USER=trial_user \
  -e MYSQL_PASSWORD=TrialPass@2026! \
  mysql:8.0

docker run -d --name trial-redis -p 6379:6379 \
  redis:7-alpine redis-server --requirepass Redis@Trial2026!

docker run -d --name trial-minio -p 9000:9000 -p 9001:9001 \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=MinioAdmin@2026! \
  minio/minio server /data --console-address ":9001"

# 3. 编译并启动后端
cd backend && mvn spring-boot:run

# 4. 启动前端开发服务器
cd frontend && npm install && npm run dev
```

### 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://localhost:5173 | Vue 开发服务器 |
| 后端 API | http://localhost:8080/api | Spring Boot 后端 |
| MinIO 控制台 | http://localhost:9001 | 对象存储管理 |
| MySQL | localhost:3306 | 关系型数据库 |
| Redis | localhost:6379 | 缓存服务 |

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 超级管理员 |

---

## ✅ 第一阶段功能清单

### 已完成

- [x] **基础设施**
  - [x] MySQL 数据库设计与初始化
  - [x] Redis 缓存集成
  - [x] MinIO 对象存储集成
  - [x] Milvus 向量数据库集成
  - [x] Docker Compose 编排

- [x] **认证与权限**
  - [x] 用户登录/登出
  - [x] Sa-Token 认证集成
  - [x] RBAC 权限模型
  - [x] 用户/角色/菜单/部门 CRUD
  - [x] 密码 BCrypt 加密

- [x] **文档管理**
  - [x] 文档上传（PDF/DOCX/TXT）
  - [x] 文档列表查询与分页
  - [x] 文档详情查看
  - [x] 文档删除
  - [x] MinIO 文件存储

- [x] **目录管理**
  - [x] 树形目录结构
  - [x] 多库分类（法规/资料/裁判/案例）

- [x] **前端基础**
  - [x] Vue 3 + Element Plus 框架搭建
  - [x] 路由与状态管理
  - [x] 登录页面
  - [x] 主框架布局
  - [x] 响应式设计

### 开发中

- [ ] **智能文档解析**
  - [ ] PDF/DOCX 内容提取
  - [ ] 关键信息自动抽取
  - [ ] 向量索引生成

- [ ] **类案推送**
  - [ ] 语义相似度计算
  - [ ] 类案检索与排序

### 规划中（后续阶段）

- [ ] 处分执行跟踪与提醒
- [ ] 阅卷笔记
- [ ] 以案促改
- [ ] 来文登记与 OCR
- [ ] AI 量纪辅助建议

---

## 🗺️ 后续规划

### 第二阶段 — 智能化增强

- [ ] 微服务架构拆分（6 个独立服务）
- [ ] API Gateway 统一路由
- [ ] DashScope 大模型集成（智能摘要、量纪建议）
- [ ] 类案语义检索完善
- [ ] 文档 OCR 识别（来文登记）

### 第三阶段 — 流程与协作

- [ ] Flowable 工作流集成
- [ ] 处分执行全流程管理
- [ ] 阅卷笔记协作功能
- [ ] 以案促改报告生成
- [ ] 消息通知系统

### 第四阶段 — 分析与决策

- [ ] 数据统计仪表盘
- [ ] 案件趋势分析
- [ ] 智能报告生成
- [ ] 多维度数据可视化

---

## 📄 文档索引

| 文档 | 路径 | 说明 |
|------|------|------|
| 部署文档 | [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) | 开发/生产环境部署指南 |
| API 文档 | [docs/API.md](docs/API.md) | 后端接口文档 |
| 用户手册 | [docs/USER_GUIDE.md](docs/USER_GUIDE.md) | 系统操作指南 |

---

## 📝 开发规范

- Java 代码遵循阿里巴巴 Java 开发手册
- 前端代码遵循 Vue 风格指南
- Git 提交信息遵循 Conventional Commits 规范
- 所有接口统一使用 `Result<T>` 返回格式

---

*Copyright © 2024-2026 智能审理系统团队. All rights reserved.*
