# 后端及数据库技术方案说明书 (backend_db_proposal.md)

## 1. 技术架构选型

*   **开发语言**：Java 21
*   **核心框架**：Spring Boot 3.x
*   **ORM 框架**：MyBatis-Plus (支持动态 SQL 与分页)
*   **权限安全**：**Spring Security** (认证) + **JWT** (无状态 Token) + **Custom Permission Evaluator** (鉴权)
*   **数据库**：MySQL 8.0
*   **缓存**：Redis (Token 存储、权限缓存、字典缓存)
*   **工具库**：Hutool (简化工具类调用), Lombok

---

## 2. 核心权限架构设计 (RBAC + Data Scope)

### 2.1 认证与鉴权流程
1.  **认证 (Authentication)**：
    *   用户登录 -> 校验账号密码 -> 生成 JWT -> 返回 Token。
    *   Token 载荷包含 `user_id` 和 `dept_id`。
2.  **动态鉴权 (Authorization)**：
    *   请求到达 -> Filter 解析 Token -> 从 Redis/DB 加载用户权限列表 (`Set<String> permissions`)。
    *   接口注解 `@PreAuthorize("@ss.hasPermi('asset:add')")` 进行拦截。

### 2.2 数据权限过滤 (Data Scope Handler)
为了实现“部门隔离”，系统通过 **AOP (面向切面编程)** 自动注入 SQL 过滤条件。

*   **定义注解**：`@DataScope(deptAlias = "d", userAlias = "u")`
*   **AOP 逻辑**：
    1.  拦截带有 `@DataScope` 的 Service/Mapper 方法。
    2.  获取当前用户角色 `Data Scope` 类型：
        *   **全部数据**：不拼接 SQL。
        *   **本部门数据**：拼接 `AND d.dept_id = {current_dept_id}`。
        *   **本部门及下级**：拼接 `AND d.dept_id IN (SELECT id FROM sys_dept WHERE find_in_set({current_dept_id}, ancestors))`。
        *   **仅本人**：拼接 `AND u.user_id = {current_user_id}`。
    3.  将拼接后的 SQL 片段注入 MyBatis 的 `BaseEntity.params` 中，XML 中通过 `${params.dataScope}` 执行。

---

## 3. 数据库设计 (RBAC 核心表)

### 3.1 用户与组织
**sys_user (用户表)**
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| user_id | bigint | 主键 |
| dept_id | bigint | **归属部门 ID** (核心数据权限字段) |
| username | varchar | 账号 |
| password | varchar | 加密密码 |
| status | char | 状态 (0正常 1停用) |

**sys_dept (部门表)**
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| dept_id | bigint | 主键 |
| parent_id | bigint | 父部门 ID |
| ancestors | varchar | 祖级列表 (如 "0,100,101") 用于快速查询下级 |
| dept_name | varchar | 部门名称 |

### 3.2 权限模型 (Permission Model)
**sys_role (角色表)**
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| role_id | bigint | 主键 |
| role_name | varchar | 角色名称 (如 "学院管理员") |
| role_key | varchar | 角色权限字符串 (如 "admin", "common") |
| **data_scope** | char | **数据范围** (1:全部, 2:本部门及下级, 3:本部门, 4:仅本人) |

**sys_menu (菜单/权限点表)**
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| menu_id | bigint | 主键 |
| parent_id | bigint | 父菜单 ID |
| menu_name | varchar | 菜单名称 |
| **perms** | varchar | **权限标识** (核心! 如 `asset:list`, `sys:user:add`) |
| menu_type | char | 类型 (M:目录, C:菜单, F:按钮) |
| path | varchar | 路由地址 (Vue Router Path) |
| component| varchar | 组件路径 (Vue Component Path) |

**sys_role_menu (角色-菜单关联表)**
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| role_id | bigint | 角色 ID |
| menu_id | bigint | 菜单 ID |

### 3.3 业务表 (关联权限)
**sys_asset (资产表)**
| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | bigint | 主键 |
| ... | ... | 基础资产信息 |
| **dept_id** | bigint | **资产归属部门** (用于数据权限过滤) |
| **manager_id**| bigint | **资产责任人** (用于"仅本人"权限过滤) |

---

## 4. 接口设计规范 (RESTful)

### 4.1 登录与权限
*   `POST /login`: 登录，返回 Token。
*   `GET /getInfo`: 获取当前用户详情、Roles 列表、Permissions 列表。
*   `GET /getRouters`: 根据权限获取动态路由树。

### 4.2 资产管理 (示例)
*   `GET /asset/list`: 查询资产列表。
    *   **鉴权**：`@PreAuthorize("@ss.hasPermi('asset:list')")`
    *   **数据权限**：`@DataScope(deptAlias = "d")` -> 自动过滤数据。
*   `POST /asset`: 新增资产。
    *   **鉴权**：`@PreAuthorize("@ss.hasPermi('asset:add')")`
*   `DELETE /asset/{id}`: 删除资产。
    *   **鉴权**：`@PreAuthorize("@ss.hasPermi('asset:remove')")`

---

## 5. 开发规范
1.  **权限标识命名**：`模块:功能:操作`，例如 `system:user:list`。
2.  **数据隔离原则**：所有涉及查询业务数据的 Service 方法，必须考虑是否添加 `@DataScope` 注解。
3.  **异常处理**：
    *   无权限 (403) -> 全局异常捕获，返回 `{"code": 403, "msg": "您没有操作权限"}`。
