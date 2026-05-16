# API 接口文档

> AI 纪检监察智能审理一体化平台 后端 API 接口文档
>
> **基础路径**: `/api`
> **统一返回格式**: `{ "code": 200, "message": "成功", "data": {} }`
> **认证方式**: Header `Authorization: <token>`

---

## 目录

- [统一响应格式](#统一响应格式)
- [1. 认证模块](#1-认证模块)
- [2. 用户管理](#2-用户管理)
- [3. 角色管理](#3-角色管理)
- [4. 菜单管理](#4-菜单管理)
- [5. 部门管理](#5-部门管理)
- [6. 文档解析](#6-文档解析)
- [7. 多库管理](#7-多库管理)
- [8. 审计日志](#8-审计日志)
- [9. 定密管理](#9-定密管理)

---

## 统一响应格式

所有接口统一使用 `Result<T>` 格式返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": { ... }
}
```

### 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或 Token 失效 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 1. 认证模块

### 1.1 用户登录

- **方法**: `POST`
- **路径**: `/api/auth/login`
- **认证**: 不需要

**请求参数**:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**响应示例**:

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "userId": 1,
    "username": "admin",
    "realName": "系统管理员",
    "roles": ["super_admin"],
    "permissions": ["system:user:list", "system:user:query", "..."]
  }
}
```

### 1.2 用户退出

- **方法**: `POST`
- **路径**: `/api/auth/logout`
- **认证**: 需要 Token

**请求参数**: 无

**响应示例**:

```json
{
  "code": 200,
  "message": "退出成功",
  "data": null
}
```

### 1.3 获取当前用户信息

- **方法**: `GET`
- **路径**: `/api/auth/userInfo`
- **认证**: 需要 Token

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "deptId": 1,
    "deptName": "智能审理系统",
    "phone": "13800000000",
    "email": "admin@example.com",
    "roles": [
      {
        "id": 1,
        "roleName": "超级管理员",
        "roleCode": "super_admin"
      }
    ],
    "permissions": ["system:user:list", "system:user:add", "..."],
    "menus": [
      {
        "id": 1,
        "name": "系统管理",
        "path": "/system",
        "icon": "setting",
        "children": [...]
      }
    ]
  }
}
```

### 1.4 修改密码

- **方法**: `PUT`
- **路径**: `/api/auth/password`
- **认证**: 需要 Token

**请求参数**:

```json
{
  "oldPassword": "admin123",
  "newPassword": "newPass@123"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | String | 是 | 旧密码 |
| newPassword | String | 是 | 新密码 |

**响应示例**:

```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

---

## 2. 用户管理

### 2.1 查询用户列表

- **方法**: `GET`
- **路径**: `/api/system/users`
- **认证**: 需要 Token + `system:user:list` 权限

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页条数，默认 10 |
| username | String | 否 | 用户名（模糊搜索） |
| realName | String | 否 | 真实姓名（模糊搜索） |
| deptId | Long | 否 | 部门ID |
| status | Integer | 否 | 状态（0=停用, 1=启用） |

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "admin",
        "realName": "系统管理员",
        "deptId": 1,
        "deptName": "智能审理系统",
        "phone": "13800000000",
        "email": "admin@example.com",
        "status": 1,
        "lastLoginTime": "2026-05-14 10:30:00",
        "createTime": "2026-01-01 00:00:00",
        "roles": [
          { "id": 1, "roleName": "超级管理员", "roleCode": "super_admin" }
        ]
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

### 2.2 获取用户详情

- **方法**: `GET`
- **路径**: `/api/system/users/{id}`
- **认证**: 需要 Token

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "deptId": 1,
    "phone": "13800000000",
    "email": "admin@example.com",
    "status": 1,
    "roleIds": [1]
  }
}
```

### 2.3 新增用户

- **方法**: `POST`
- **路径**: `/api/system/users`
- **认证**: 需要 Token + `system:user:add` 权限

**请求参数**:

```json
{
  "username": "zhangsan",
  "password": "Zhang@123",
  "realName": "张三",
  "deptId": 2,
  "phone": "13800111111",
  "email": "zhangsan@example.com",
  "status": 1,
  "roleIds": [2]
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名（唯一） |
| password | String | 是 | 密码 |
| realName | String | 是 | 真实姓名 |
| deptId | Long | 否 | 部门ID |
| phone | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |
| status | Integer | 否 | 状态，默认 1 |
| roleIds | Long[] | 否 | 角色ID列表 |

**响应示例**:

```json
{
  "code": 200,
  "message": "用户创建成功",
  "data": {
    "id": 2,
    "username": "zhangsan",
    "realName": "张三"
  }
}
```

### 2.4 修改用户

- **方法**: `PUT`
- **路径**: `/api/system/users/{id}`
- **认证**: 需要 Token + `system:user:edit` 权限

**请求参数**: 同新增用户（不需要 password）

**响应示例**:

```json
{
  "code": 200,
  "message": "用户更新成功",
  "data": null
}
```

### 2.5 删除用户

- **方法**: `DELETE`
- **路径**: `/api/system/users/{id}`
- **认证**: 需要 Token + `system:user:remove` 权限

**响应示例**:

```json
{
  "code": 200,
  "message": "用户删除成功",
  "data": null
}
```

### 2.6 批量删除用户

- **方法**: `DELETE`
- **路径**: `/api/system/users/batch`
- **认证**: 需要 Token + `system:user:remove` 权限

**请求参数**:

```json
[1, 2, 3]
```

**响应示例**:

```json
{
  "code": 200,
  "message": "批量删除成功",
  "data": null
}
```

### 2.7 重置密码

- **方法**: `PUT`
- **路径**: `/api/system/users/{id}/reset-password`
- **认证**: 需要 Token + `system:user:edit` 权限

**请求参数**:

```json
{
  "newPassword": "reset@123"
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": null
}
```

---

## 3. 角色管理

### 3.1 查询角色列表

- **方法**: `GET`
- **路径**: `/api/system/roles`
- **认证**: 需要 Token + `system:role:list` 权限

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页条数 |
| roleName | String | 否 | 角色名称（模糊搜索） |
| status | Integer | 否 | 状态 |

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "records": [
      {
        "id": 1,
        "roleName": "超级管理员",
        "roleCode": "super_admin",
        "description": "拥有系统全部权限",
        "status": 1,
        "createTime": "2026-01-01 00:00:00"
      }
    ],
    "total": 3
  }
}
```

### 3.2 获取角色详情

- **方法**: `GET`
- **路径**: `/api/system/roles/{id}`
- **认证**: 需要 Token

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "roleName": "超级管理员",
    "roleCode": "super_admin",
    "description": "拥有系统全部权限",
    "status": 1,
    "menuIds": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]
  }
}
```

### 3.3 新增角色

- **方法**: `POST`
- **路径**: `/api/system/roles`
- **认证**: 需要 Token + `system:role:add` 权限

**请求参数**:

```json
{
  "roleName": "审核组长",
  "roleCode": "audit_leader",
  "description": "审核组组长角色",
  "status": 1,
  "menuIds": [1, 2, 3, 4, 5]
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleName | String | 是 | 角色名称 |
| roleCode | String | 是 | 角色编码（唯一） |
| description | String | 否 | 描述 |
| status | Integer | 否 | 状态，默认 1 |
| menuIds | Long[] | 否 | 菜单/权限ID列表 |

**响应示例**:

```json
{
  "code": 200,
  "message": "角色创建成功",
  "data": {
    "id": 4,
    "roleName": "审核组长",
    "roleCode": "audit_leader"
  }
}
```

### 3.4 修改角色

- **方法**: `PUT`
- **路径**: `/api/system/roles/{id}`
- **认证**: 需要 Token + `system:role:edit` 权限

**请求参数**: 同新增角色

**响应示例**:

```json
{
  "code": 200,
  "message": "角色更新成功",
  "data": null
}
```

### 3.5 删除角色

- **方法**: `DELETE`
- **路径**: `/api/system/roles/{id}`
- **认证**: 需要 Token + `system:role:remove` 权限

**响应示例**:

```json
{
  "code": 200,
  "message": "角色删除成功",
  "data": null
}
```

### 3.6 分配角色权限

- **方法**: `PUT`
- **路径**: `/api/system/roles/{id}/menus`
- **认证**: 需要 Token + `system:role:edit` 权限

**请求参数**:

```json
{
  "menuIds": [1, 2, 3, 4, 5, 6, 7]
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "权限分配成功",
  "data": null
}
```

---

## 4. 菜单管理

### 4.1 查询菜单树

- **方法**: `GET`
- **路径**: `/api/system/menus`
- **认证**: 需要 Token + `system:menu:list` 权限

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 否 | 菜单名称（模糊搜索） |
| status | Integer | 否 | 状态 |

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "parentId": 0,
      "name": "系统管理",
      "path": "/system",
      "component": "Layout",
      "type": 1,
      "icon": "setting",
      "sort": 1,
      "visible": 1,
      "status": 1,
      "children": [
        {
          "id": 14,
          "parentId": 1,
          "name": "用户管理",
          "path": "user",
          "component": "system/user/index",
          "perms": "system:user:list",
          "type": 2,
          "icon": "user",
          "sort": 1,
          "children": [
            {
              "id": 18,
              "name": "用户查询",
              "perms": "system:user:query",
              "type": 3
            }
          ]
        }
      ]
    }
  ]
}
```

### 4.2 获取菜单详情

- **方法**: `GET`
- **路径**: `/api/system/menus/{id}`
- **认证**: 需要 Token

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 14,
    "parentId": 1,
    "name": "用户管理",
    "path": "user",
    "component": "system/user/index",
    "perms": "system:user:list",
    "type": 2,
    "icon": "user",
    "sort": 1,
    "visible": 1,
    "status": 1
  }
}
```

### 4.3 新增菜单

- **方法**: `POST`
- **路径**: `/api/system/menus`
- **认证**: 需要 Token + `system:menu:add` 权限

**请求参数**:

```json
{
  "parentId": 1,
  "name": "日志管理",
  "path": "log",
  "component": "system/log/index",
  "perms": "system:log:list",
  "type": 2,
  "icon": "document",
  "sort": 5,
  "visible": 1,
  "status": 1
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| parentId | Long | 否 | 上级菜单ID，默认 0 |
| name | String | 是 | 菜单名称 |
| path | String | 否 | 路由路径 |
| component | String | 否 | 组件路径 |
| perms | String | 否 | 权限标识 |
| type | Integer | 否 | 类型：1=目录, 2=菜单, 3=按钮 |
| icon | String | 否 | 图标 |
| sort | Integer | 否 | 排序 |
| visible | Integer | 否 | 是否可见：0=隐藏, 1=显示 |
| status | Integer | 否 | 状态，默认 1 |

**响应示例**:

```json
{
  "code": 200,
  "message": "菜单创建成功",
  "data": {
    "id": 19,
    "name": "日志管理"
  }
}
```

### 4.4 修改菜单

- **方法**: `PUT`
- **路径**: `/api/system/menus/{id}`
- **认证**: 需要 Token + `system:menu:edit` 权限

**请求参数**: 同新增菜单

**响应示例**:

```json
{
  "code": 200,
  "message": "菜单更新成功",
  "data": null
}
```

### 4.5 删除菜单

- **方法**: `DELETE`
- **路径**: `/api/system/menus/{id}`
- **认证**: 需要 Token + `system:menu:remove` 权限

**响应示例**:

```json
{
  "code": 200,
  "message": "菜单删除成功",
  "data": null
}
```

---

## 5. 部门管理

### 5.1 查询部门树

- **方法**: `GET`
- **路径**: `/api/system/depts`
- **认证**: 需要 Token + `system:dept:list` 权限

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| deptName | String | 否 | 部门名称（模糊搜索） |
| status | Integer | 否 | 状态 |

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "parentId": 0,
      "deptName": "智能审理系统",
      "leader": "系统管理员",
      "phone": "010-00000000",
      "sort": 0,
      "status": 1,
      "children": [
        {
          "id": 2,
          "parentId": 1,
          "deptName": "审理一室",
          "leader": "张三",
          "phone": "010-11111111",
          "sort": 1,
          "status": 1,
          "children": []
        }
      ]
    }
  ]
}
```

### 5.2 获取部门详情

- **方法**: `GET`
- **路径**: `/api/system/depts/{id}`
- **认证**: 需要 Token

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 2,
    "parentId": 1,
    "deptName": "审理一室",
    "leader": "张三",
    "phone": "010-11111111",
    "sort": 1,
    "status": 1
  }
}
```

### 5.3 新增部门

- **方法**: `POST`
- **路径**: `/api/system/depts`
- **认证**: 需要 Token + `system:dept:add` 权限

**请求参数**:

```json
{
  "parentId": 1,
  "deptName": "审理三室",
  "leader": "赵六",
  "phone": "010-44444444",
  "sort": 4,
  "status": 1
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| parentId | Long | 否 | 上级部门ID，默认 0 |
| deptName | String | 是 | 部门名称 |
| leader | String | 否 | 负责人 |
| phone | String | 否 | 联系电话 |
| sort | Integer | 否 | 排序 |
| status | Integer | 否 | 状态，默认 1 |

**响应示例**:

```json
{
  "code": 200,
  "message": "部门创建成功",
  "data": {
    "id": 5,
    "deptName": "审理三室"
  }
}
```

### 5.4 修改部门

- **方法**: `PUT`
- **路径**: `/api/system/depts/{id}`
- **认证**: 需要 Token + `system:dept:edit` 权限

**请求参数**: 同新增部门

**响应示例**:

```json
{
  "code": 200,
  "message": "部门更新成功",
  "data": null
}
```

### 5.5 删除部门

- **方法**: `DELETE`
- **路径**: `/api/system/depts/{id}`
- **认证**: 需要 Token + `system:dept:remove` 权限

**响应示例**:

```json
{
  "code": 200,
  "message": "部门删除成功",
  "data": null
}
```

---

## 6. 文档解析

### 6.1 上传文档

- **方法**: `POST`
- **路径**: `/api/documents/upload`
- **认证**: 需要 Token
- **Content-Type**: `multipart/form-data`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 文档文件（支持 PDF/DOCX/TXT） |
| docType | String | 是 | 文档类型 |
| categoryPath | String | 否 | 分类路径 |
| securityLevel | String | 否 | 密级，默认 internal |

**响应示例**:

```json
{
  "code": 200,
  "message": "文档上传成功",
  "data": {
    "id": 1,
    "fileName": "案件报告.pdf",
    "filePath": "trial-documents/2026/05/abc123.pdf",
    "fileType": "pdf",
    "fileSize": 1024000,
    "status": 1,
    "securityLevel": "internal",
    "uploadTime": "2026-05-14 14:30:00"
  }
}
```

### 6.2 查询文档列表

- **方法**: `GET`
- **路径**: `/api/documents/list`
- **认证**: 需要 Token

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页条数，默认 10 |
| docType | String | 否 | 文档类型 |
| keyword | String | 否 | 关键词搜索 |
| securityLevel | String | 否 | 密级筛选 |

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "records": [
      {
        "id": 1,
        "fileName": "案件报告.pdf",
        "fileType": "pdf",
        "fileSize": 1024000,
        "status": 2,
        "progress": 100,
        "createTime": "2026-05-14 14:30:00"
      }
    ],
    "total": 1,
    "current": 1
  }
}
```

### 6.3 获取文档详情

- **方法**: `GET`
- **路径**: `/api/documents/{id}`
- **认证**: 需要 Token

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "fileName": "案件报告.pdf",
    "filePath": "trial-documents/2026/05/abc123.pdf",
    "fileType": "pdf",
    "fileSize": 1024000,
    "status": 2,
    "progress": 100,
    "resultJson": {
      "title": "关于XX案件的审理报告",
      "sections": [...],
      "entities": {...}
    },
    "vectorCount": 45,
    "createTime": "2026-05-14 14:30:00",
    "parseTime": "2026-05-14 14:31:00"
  }
}
```

### 6.4 删除文档

- **方法**: `DELETE`
- **路径**: `/api/documents/{id}`
- **认证**: 需要 Token

**响应示例**:

```json
{
  "code": 200,
  "message": "文档删除成功",
  "data": null
}
```

### 6.5 查询解析任务列表

- **方法**: `GET`
- **路径**: `/api/parse/tasks`
- **认证**: 需要 Token

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页条数 |
| status | Integer | 否 | 状态（0=待处理, 1=处理中, 2=已完成, 3=失败） |
| fileType | String | 否 | 文件类型 |

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "records": [
      {
        "id": 1,
        "fileName": "案件报告.pdf",
        "status": 2,
        "progress": 100,
        "vectorCount": 45,
        "createTime": "2026-05-14 14:30:00",
        "parseTime": "2026-05-14 14:31:00"
      }
    ],
    "total": 1
  }
}
```

---

## 7. 多库管理

### 7.1 目录管理

#### 7.1.1 查询目录树

- **方法**: `GET`
- **路径**: `/api/repo/directories`
- **认证**: 需要 Token

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| repoType | Integer | 否 | 库类型（1=法规库, 2=资料库, 3=裁判文书库, 4=案例库） |

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "repoType": 1,
      "parentId": 0,
      "name": "法律法规",
      "sort": 1,
      "status": 1,
      "children": [
        {
          "id": 2,
          "parentId": 1,
          "name": "党内法规",
          "sort": 1,
          "status": 1,
          "children": []
        }
      ]
    }
  ]
}
```

#### 7.1.2 新增目录

- **方法**: `POST`
- **路径**: `/api/repo/directories`
- **认证**: 需要 Token

**请求参数**:

```json
{
  "repoType": 1,
  "parentId": 1,
  "name": "国家法律",
  "sort": 2,
  "classificationLevelId": 5,
  "permissionScope": "1,2"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| repoType | Integer | 是 | 库类型：1=法规库, 2=资料库, 3=裁判文书库, 4=案例库 |
| parentId | Long | 否 | 上级目录ID，默认 0 |
| name | String | 是 | 目录名称 |
| sort | Integer | 否 | 排序，默认 0 |
| classificationLevelId | Long | 否 | 密级ID |
| permissionScope | String | 否 | 权限范围（逗号分隔的角色ID） |

**响应示例**:

```json
{
  "code": 200,
  "message": "目录创建成功",
  "data": {
    "id": 3,
    "name": "国家法律"
  }
}
```

#### 7.1.3 修改目录

- **方法**: `PUT`
- **路径**: `/api/repo/directories/{id}`
- **认证**: 需要 Token

**请求参数**: 同新增目录

**响应示例**:

```json
{
  "code": 200,
  "message": "目录更新成功",
  "data": null
}
```

#### 7.1.4 删除目录

- **方法**: `DELETE`
- **路径**: `/api/repo/directories/{id}`
- **认证**: 需要 Token

**响应示例**:

```json
{
  "code": 200,
  "message": "目录删除成功",
  "data": null
}
```

### 7.2 文档管理

#### 7.2.1 查询文档列表

- **方法**: `GET`
- **路径**: `/api/repo/documents`
- **认证**: 需要 Token

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页条数 |
| repoType | Integer | 否 | 库类型 |
| directoryId | Long | 否 | 目录ID |
| keyword | String | 否 | 关键词 |
| publishUnit | String | 否 | 发布单位 |
| validityStatus | Integer | 否 | 效力状态 |
| classificationLevelId | Long | 否 | 密级ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "records": [
      {
        "id": 1,
        "repoType": 1,
        "directoryId": 2,
        "title": "中国共产党纪律处分条例",
        "docNo": "中发〔2023〕xx号",
        "publishUnit": "中共中央",
        "publishDate": "2023-12-27",
        "effectiveDate": "2024-01-01",
        "validityStatus": 1,
        "classificationLevelId": 5,
        "classificationLevelName": "公开",
        "fileType": "pdf",
        "summary": "条例全文...",
        "status": 1,
        "createTime": "2026-01-15 00:00:00"
      }
    ],
    "total": 1
  }
}
```

#### 7.2.2 获取文档详情

- **方法**: `GET`
- **路径**: `/api/repo/documents/{id}`
- **认证**: 需要 Token

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "repoType": 1,
    "repoTypeName": "法规库",
    "directoryId": 2,
    "directoryName": "党内法规",
    "title": "中国共产党纪律处分条例",
    "docNo": "中发〔2023〕xx号",
    "publishUnit": "中共中央",
    "publishDate": "2023-12-27",
    "effectiveDate": "2024-01-01",
    "revisionDate": null,
    "validityStatus": 1,
    "validityStatusName": "现行有效",
    "classificationLevelId": 5,
    "classificationLevelName": "公开",
    "filePath": "trial-documents/laws/regulation_xxx.pdf",
    "fileSize": 524288,
    "fileType": "pdf",
    "summary": "本条例是为了维护党章和其他党内法规...",
    "status": 1,
    "createTime": "2026-01-15 00:00:00",
    "updateTime": "2026-01-15 00:00:00"
  }
}
```

#### 7.2.3 新增文档

- **方法**: `POST`
- **路径**: `/api/repo/documents`
- **认证**: 需要 Token

**请求参数**:

```json
{
  "repoType": 1,
  "directoryId": 2,
  "title": "中国共产党纪律处分条例",
  "docNo": "中发〔2023〕xx号",
  "publishUnit": "中共中央",
  "publishDate": "2023-12-27",
  "effectiveDate": "2024-01-01",
  "validityStatus": 1,
  "classificationLevelId": 5,
  "summary": "条例摘要..."
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "文档创建成功",
  "data": {
    "id": 1,
    "title": "中国共产党纪律处分条例"
  }
}
```

#### 7.2.4 修改文档

- **方法**: `PUT`
- **路径**: `/api/repo/documents/{id}`
- **认证**: 需要 Token

**请求参数**: 同新增文档

**响应示例**:

```json
{
  "code": 200,
  "message": "文档更新成功",
  "data": null
}
```

#### 7.2.5 删除文档

- **方法**: `DELETE`
- **路径**: `/api/repo/documents/{id}`
- **认证**: 需要 Token

**响应示例**:

```json
{
  "code": 200,
  "message": "文档删除成功",
  "data": null
}
```

#### 7.2.6 语义检索

- **方法**: `POST`
- **路径**: `/api/repo/documents/search`
- **认证**: 需要 Token

**请求参数**:

```json
{
  "query": "违反廉洁纪律的处分标准",
  "repoType": 1,
  "topK": 10
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| query | String | 是 | 查询文本 |
| repoType | Integer | 否 | 限定库类型 |
| topK | Integer | 否 | 返回结果数量，默认 10 |

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "title": "中国共产党纪律处分条例",
      "summary": "...",
      "score": 0.9234,
      "snippet": "第一百二十三条 违反廉洁纪律..."
    }
  ]
}
```

---

## 8. 审计日志

### 8.1 查询审计日志

- **方法**: `GET`
- **路径**: `/api/audit/logs`
- **认证**: 需要 Token

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页条数 |
| userId | Long | 否 | 操作用户ID |
| module | String | 否 | 操作模块 |
| action | String | 否 | 操作类型 |
| result | Integer | 否 | 操作结果（0=失败, 1=成功） |
| startTime | String | 否 | 开始时间（yyyy-MM-dd HH:mm:ss） |
| endTime | String | 否 | 结束时间 |

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "userName": "admin",
        "module": "用户管理",
        "action": "新增",
        "description": "新增用户 zhangsan",
        "ip": "192.168.1.100",
        "userAgent": "Mozilla/5.0 ...",
        "requestUrl": "/api/system/users",
        "requestMethod": "POST",
        "result": 1,
        "duration": 156,
        "createTime": "2026-05-14 10:30:00"
      }
    ],
    "total": 1
  }
}
```

### 8.2 导出审计日志

- **方法**: `GET`
- **路径**: `/api/audit/logs/export`
- **认证**: 需要 Token

**请求参数**: 同查询审计日志（不含分页参数）

**响应**: Excel 文件下载

---

## 9. 定密管理

### 9.1 查询密级列表

- **方法**: `GET`
- **路径**: `/api/classification/levels`
- **认证**: 需要 Token

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 否 | 状态 |

**响应示例**:

```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "levelCode": "TOP_SECRET",
      "levelName": "绝密",
      "sort": 1,
      "status": 1
    },
    {
      "id": 2,
      "levelCode": "SECRET",
      "levelName": "机密",
      "sort": 2,
      "status": 1
    },
    {
      "id": 3,
      "levelCode": "CONFIDENTIAL",
      "levelName": "秘密",
      "sort": 3,
      "status": 1
    },
    {
      "id": 4,
      "levelCode": "INTERNAL",
      "levelName": "内部",
      "sort": 4,
      "status": 1
    },
    {
      "id": 5,
      "levelCode": "PUBLIC",
      "levelName": "公开",
      "sort": 5,
      "status": 1
    }
  ]
}
```

### 9.2 新增密级

- **方法**: `POST`
- **路径**: `/api/classification/levels`
- **认证**: 需要 Token

**请求参数**:

```json
{
  "levelCode": "RESTRICTED",
  "levelName": "限制",
  "sort": 6,
  "status": 1
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| levelCode | String | 是 | 密级编码（唯一） |
| levelName | String | 是 | 密级名称 |
| sort | Integer | 否 | 排序（数值越小密级越高） |
| status | Integer | 否 | 状态，默认 1 |

**响应示例**:

```json
{
  "code": 200,
  "message": "密级创建成功",
  "data": {
    "id": 6,
    "levelCode": "RESTRICTED",
    "levelName": "限制"
  }
}
```

### 9.3 修改密级

- **方法**: `PUT`
- **路径**: `/api/classification/levels/{id}`
- **认证**: 需要 Token

**请求参数**: 同新增密级

**响应示例**:

```json
{
  "code": 200,
  "message": "密级更新成功",
  "data": null
}
```

### 9.4 删除密级

- **方法**: `DELETE`
- **路径**: `/api/classification/levels/{id}`
- **认证**: 需要 Token

**响应示例**:

```json
{
  "code": 200,
  "message": "密级删除成功",
  "data": null
}
```

---

*最后更新: 2026-05-14*
