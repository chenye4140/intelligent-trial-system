# AI 纪检监察智能审理一体化平台

> 基于 AI 大模型的纪检监察智能审理一体化平台，集文档解析、类案推送、文书生成、处分执行、以案促改等功能于一体。

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4-green.svg)](https://vuejs.org/)
[![Java](https://img.shields.io/badge/Java-1.8-orange.svg)](https://www.oracle.com/java/)
[![Tests](https://img.shields.io/badge/Tests-703%20passing-brightgreen.svg)]()
[![Coverage](https://img.shields.io/badge/E2E-36%20tests-blue.svg)]()

---

## 📖 项目简介

**AI 纪检监察智能审理一体化平台** 是面向纪检监察机关设计的智能化案件审理系统。平台深度融合 AI 大模型（DeepSeek）与业务流转技术，实现案件材料的智能解析、类案自动推送、文书自动生成、五级定密建议、处分全流程跟踪、工作流审批等功能，全面提升纪检监察审理工作的效率与质量。

### 核心特性

- 🔐 **统一身份认证** — 基于 RBAC 模型的权限管理，支持多级部门、多角色细粒度权限控制
- 📄 **智能文档解析** — 支持 PDF/DOCX/TXT 等多种格式，自动提取结构化信息并生成向量索引
- 📚 **多库管理** — 法规库、资料库、裁判文书库、案例库四大知识库统一管理
- 🔍 **类案推送** — 基于语义向量的类案相似度匹配（MySQL + Java 余弦相似度），辅助审理人员精准参考
- 📝 **文书生成** — DeepSeek AI 自动生成审理报告、初核报告、处分决定等文书
- 🔒 **五级定密** — 绝密/机密/秘密/内部/公开，AI 辅助定密建议
- 📋 **处分执行** — 处分全流程跟踪，逾期自动检测提醒
- 🔄 **工作流审批** — Flowable 集成，4 步审批流程（提交→部门审核→纪检审核→领导审批）
- 📊 **以案促改** — AI 分析案件深层原因，生成纪律/管理/制度多维度改进建议
- 📒 **阅卷笔记** — 案件阅卷笔记管理，支持共享协作
- 📨 **来文登记** — 来文登记、状态跟踪、全文检索
- 📜 **审计日志** — 全量操作审计（77 处 @RequireLog），满足合规要求

---

## 🛠️ 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 1.8 | 运行环境 |
| Spring Boot | 2.7.18 | 核心框架 |
| Spring Cloud Gateway | 2021.0.8 | API 网关 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| JWT (jjwt) | 0.12.3 | Token 认证 |
| Flowable | 6.8.0 | 工作流引擎 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7.x | 缓存与会话存储 |
| MinIO | latest | 对象存储 |
| DeepSeek | deepseek-v4-pro | AI 大模型（文书生成/定密建议/以案促改） |
| Apache POI | 5.2.5 | Office 文档解析 |
| PDFBox | 2.0.x | PDF 文档解析 |
| Hutool | 5.8.25 | Java 工具类库 |
| FastJSON2 | 2.0.43 | JSON 序列化 |
| OkHttp | 4.x | HTTP 客户端（AI 服务调用） |
| springdoc-openapi | 1.8.0 | Swagger API 文档 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4 | 渐进式前端框架 |
| Vue Router | 4.3 | 路由管理 |
| Pinia | 2.1 | 状态管理 |
| Element Plus | 2.6 | UI 组件库 |
| Vite | 5.2 | 前端构建工具 |
| Axios | 1.6 | HTTP 客户端 |
| Playwright | latest | E2E 浏览器自动化测试 |

### DevOps

| 工具 | 说明 |
|------|------|
| Docker + Docker Compose | 容器化部署（10 个 Dockerfile，多阶段构建） |
| GitHub Actions | CI/CD 自动化（编译+测试+Docker 构建+代码质量扫描） |
| Nginx | 前端静态资源 + SPA 路由 + API 反向代理 |
| H2 | 集成测试内存数据库 |
| JUnit 5 + Mockito | 单元测试框架 |

---

## 📁 模块结构

本项目采用 **Maven 多模块微服务** 架构，共 12 个模块：

```
intelligent-trial-system/
├── pom.xml                      # 父 POM（聚合 + 依赖管理）
├── common/                      # 公共模块：工具类/通用实体/统一返回/错误码
├── auth/                        # 认证模块（端口 8081）：登录/Token/RBAC/定密/审计
├── document/                    # 文档模块（端口 8082）：文档解析/向量存储/类案推送/来文登记
├── repository/                  # 多库模块（端口 8083）：法规/资料/裁判/案例库
├── casemanage/                  # 案件模块（端口 8085）：案件CRUD/当事人/违纪事实
├── report/                      # 文书模块（端口 8084）：AI文书生成/模板管理
├── workflow/                    # 工作流模块（端口 8086）：Flowable审批流
├── promotion/                   # 以案促改模块（端口 8088）：AI分析/改进建议
├── punishment/                  # 处分执行模块（端口 8089）：处分跟踪/逾期检测
├── readingnote/                 # 阅卷笔记模块（端口 8090）：笔记CRUD/共享
├── api-gateway/                 # 网关模块（端口 8080）：统一入口/JWT校验/路由转发
├── frontend/                    # Vue 3 前端
├── sql/                         # 数据库初始化脚本 + Flyway 迁移
├── docker/                      # Docker 配置（10 个 Dockerfile + Nginx）
└── e2e/                         # Playwright E2E 测试
```

### 模块端口分配

| 模块 | 端口 | 说明 |
|------|------|------|
| api-gateway | 8080 | API 网关（统一入口） |
| auth | 8081 | 认证服务 |
| document | 8082 | 文档服务 |
| repository | 8083 | 多库服务 |
| report | 8084 | 文书生成服务 |
| casemanage | 8085 | 案件管理服务 |
| workflow | 8086 | 工作流服务 |
| promotion | 8088 | 以案促改服务 |
| punishment | 8089 | 处分执行服务 |
| readingnote | 8090 | 阅卷笔记服务 |

---

## 🚀 快速开始

### 前置条件

| 软件 | 最低版本 | 推荐版本 |
|------|----------|----------|
| JDK | 1.8 | 1.8.0_391+ |
| Maven | 3.6 | 3.9.x |
| Node.js | 18 | 20+ LTS |
| MySQL | 8.0 | 8.0.35 |
| Redis | 6.0 | 7.2 |
| Docker | 20.10 | 24.x |

### 方式一：Docker Compose 一键启动（推荐）

```bash
# 1. 复制环境变量配置
cp .env.example .env
# 编辑 .env，填入实际的 API Key 和密码

# 2. 启动全部服务（基础设施 + 后端 + 前端）
docker compose up -d

# 3. 等待服务就绪（约 60 秒）
docker compose ps

# 4. 访问系统
# 前端: http://localhost:80
# API 网关: http://localhost:8080
# Swagger 文档: http://localhost:8080/swagger-ui.html
# MinIO 控制台: http://localhost:9001
```

### 方式二：本地开发

```bash
# 1. 启动基础设施
docker compose up -d mysql redis minio

# 2. 初始化数据库
mysql -u root -p < sql/init.sql

# 3. 编译后端
mvn clean package -DskipTests

# 4. 启动各后端服务（按需启动，每个服务独立进程）
java -jar auth/target/*.jar &
java -jar casemanage/target/*.jar &
java -jar document/target/*.jar &
java -jar api-gateway/target/*.jar &

# 5. 启动前端
cd frontend
npm install
npm run dev
```

### 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://localhost:5173 | Vue 开发服务器 |
| API 网关 | http://localhost:8080/api | 统一 API 入口 |
| Swagger | http://localhost:8080/swagger-ui.html | API 文档 |
| MinIO 控制台 | http://localhost:9001 | 对象存储管理 |
| MySQL | localhost:3306 | 关系型数据库 |
| Redis | localhost:6379 | 缓存服务 |

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 超级管理员 |

---

## ✅ 功能完成情况

### 核心功能（100% 完成）

- [x] **统一身份认证** — 登录/登出/Token刷新/用户信息/RBAC权限/部门管理/菜单管理/密级管理
- [x] **案件管理** — 案件CRUD/当事人/违纪事实/案件编号自动生成/状态流转/文档关联
- [x] **智能文档解析** — PDF/DOCX/TXT解析/LLM分类/任务管理/结果展示
- [x] **多库管理** — 法规库/资料库/裁判库/案例库/树形目录/Excel导入导出/搜索
- [x] **类案推送** — 向量存储/余弦相似度搜索/按案件或文本搜索/相似度记录
- [x] **文书生成** — DeepSeek AI生成/模板管理/异步任务/状态跟踪
- [x] **五级定密** — AI辅助定密建议/DeepSeek分析/一键采纳
- [x] **以案促改** — 4种分析类型（综合/纪律/管理/制度）/AI异步分析/Redis状态跟踪
- [x] **工作流审批** — Flowable集成/4步审批流程/任务管理/流程实例管理
- [x] **处分执行** — 处分CRUD/状态变更/逾期自动检测/材料管理
- [x] **阅卷笔记** — 笔记CRUD/分页搜索/共享切换/按案件查询
- [x] **来文登记** — 来文CRUD/状态流转/标题搜索/按单位过滤
- [x] **系统管理** — 用户管理/角色管理/菜单管理/审计日志

### 质量保障

- [x] **单元测试** — 703 个测试用例，0 失败，覆盖 11 个业务模块
- [x] **集成测试** — 10 个集成测试类，覆盖核心 REST API 端点
- [x] **E2E 测试** — 36 个 Playwright 浏览器自动化测试（auth/case/document/system/workflow）
- [x] **Swagger 文档** — 20 个 Controller 全部标注，134 处 @Operation
- [x] **审计日志** — 77 处 @RequireLog，覆盖所有写操作
- [x] **参数校验** — 38 处 @Valid，JSR-303 注解全覆盖
- [x] **事务管理** — 75+ 处 @Transactional，所有写操作全覆盖
- [x] **权限校验** — 33 处 @RequirePermission，覆盖敏感操作
- [x] **安全加固** — 0 处硬编码密钥，全部环境变量引用；CORS 安全配置

### DevOps

- [x] **Docker 容器化** — 10 个 Dockerfile（多阶段构建 + 非 root 用户 + G1GC 调优）
- [x] **Docker Compose** — 15 个服务编排（含 HealthCheck）
- [x] **CI/CD** — GitHub Actions（CI: 编译+测试+E2E+质量扫描；CD: Docker 镜像构建+推送）
- [x] **Nginx 配置** — SPA 路由 + Gzip 压缩 + 安全 Headers + WebSocket 支持
- [x] **环境变量模板** — .env.example 覆盖全部配置项

---

## 📊 代码统计

| 指标 | 数量 |
|------|------|
| Java 主文件 | 214 个（17,473 行） |
| Java 测试文件 | 42 个（13,342 行） |
| Vue 页面 | 22 个（6,294 行） |
| Mapper XML | 25 个（966 行） |
| SQL 文件 | 20 个（1,515 行） |
| 测试用例 | 703 个（单元测试 + 集成测试） |
| E2E 测试 | 36 个（Playwright） |
| Controller | 20 个 |
| ServiceImpl | 20 个 |
| 业务模块 | 11 个 |

---

## 🗺️ 后续优化方向

1. **SonarQube 代码质量门禁** — 接入 SonarQube 进行持续代码质量监控
2. **性能压测基线** — 关键接口（登录/搜索/文书生成）性能压测
3. **前端 vendor chunk 优化** — element-plus chunk (1.07MB) 可按需加载优化
4. **端到端集成测试增强** — 补充全链路 API 集成测试
5. **Milvus 向量库集成** — 设计文档中预留，当前 MySQL + Java 余弦相似度方案已满足需求，未来数据量增长时可迁移

---

## 📄 文档索引

| 文档 | 路径 | 说明 |
|------|------|------|
| 部署文档 | [DEPLOY.md](./DEPLOY.md) | 开发/生产环境部署指南 |
| 详细设计文档 | [详细设计文档-逐模块设计.md](./详细设计文档-逐模块设计.md) | 逐模块详细设计 |
| E2E 测试规划 | [e2e/TEST_PLAN.md](./e2e/TEST_PLAN.md) | 浏览器自动化测试规划 |

---

## 📝 开发规范

- Java 代码遵循阿里巴巴 Java 开发手册
- 前端代码遵循 Vue 风格指南
- Git 提交信息遵循 Conventional Commits 规范
- 所有接口统一使用 `R<T>` 返回格式
- 错误处理统一使用 `BusinessException` + `ErrorCode` 枚举
- 敏感配置全部使用 `${ENV_VAR:default}` 环境变量引用格式

---

*Copyright © 2024-2026 智能审理系统团队. All rights reserved.*
