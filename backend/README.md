# 后端 — 高校固定资产全生命周期管理系统

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 21 | 语言运行时 |
| Spring Boot | 3.3.5 | 应用框架与自动配置 |
| Spring Security | 6.x | 认证与方法级鉴权 |
| JJWT | 0.12.3 | 无状态 JWT 签发与校验 |
| MyBatis-Plus | 3.5.12 | ORM、分页、逻辑删除 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 6.0+ | 缓存（Lettuce 连接） |
| ZXing | 3.5.2 | 二维码生成 |
| EasyExcel | 3.3.3 | Excel 导入导出 |
| Hutool | 5.8.25 | 通用工具库 |
| Lombok | — | 简化 POJO 代码 |
| Maven | 3.9.x | 构建与依赖管理 |

---

## 目录结构

```
backend/
├── pom.xml                                         # Maven 配置
└── src/
    ├── main/
    │   ├── java/com/university/asset/
    │   │   ├── AssetManagementApplication.java     # 启动入口
    │   │   ├── common/                             # 公共基础层
    │   │   │   ├── annotation/                     #   自定义注解
    │   │   │   ├── aspect/                         #   AOP 切面
    │   │   │   ├── exception/                      #   异常处理
    │   │   │   └── result/                         #   统一响应体
    │   │   ├── config/                             # 配置类
    │   │   ├── security/                           # 安全层
    │   │   │   ├── filter/                         #   JWT 过滤器
    │   │   │   ├── handler/                        #   认证失败处理
    │   │   │   └── service/                        #   用户加载与权限判断
    │   │   ├── entity/                             # 数据库实体（12 个）
    │   │   ├── dto/                                # 请求参数对象（8 个）
    │   │   ├── vo/                                 # 响应视图对象（3 个）
    │   │   ├── mapper/                             # MyBatis Mapper 接口（12 个）
    │   │   ├── service/                            # 业务逻辑层（8 个）
    │   │   ├── controller/                         # REST 接口层（10 个）
    │   │   └── utils/                              # 工具类
    │   └── resources/
    │       ├── application.yml                     # 主配置
    │       ├── mapper/                             # MyBatis XML 映射（11 个）
    │       └── sql/
    │           └── init.sql                        # 建库建表 + 种子数据
    └── test/                                       # 单元测试
```

---

## 包设计说明

### `common/` — 公共基础层

提供横切关注点支撑，包含四个子包：

| 子包 | 内容 | 说明 |
|------|------|------|
| `annotation/` | `@DataScope`、`@Log` | 自定义注解，标记需要数据权限过滤或审计日志记录的方法 |
| `aspect/` | `DataScopeAspect`、`LogAspect` | AOP 切面实现。`DataScopeAspect` 根据角色 `dataScope` 动态拼接 SQL WHERE 子句；`LogAspect` 拦截 `@Log` 注解方法记录操作日志 |
| `exception/` | `GlobalExceptionHandler`、`ServiceException` | `@RestControllerAdvice` 统一异常处理，将业务异常、参数校验异常等转换为标准 `R` 响应 |
| `result/` | `R`、`PageResult` | `R<T>` 统一响应体 `{code, msg, data}`；`PageResult<T>` 分页响应体 `{total, rows}` |

### `config/` — 配置类

- **`SecurityConfig`**：配置 Spring Security 过滤链（放行登录接口、其他请求需认证）、CORS 跨域、注册 JWT 过滤器
- **`MyBatisPlusConfig`**：注册分页插件、配置自动填充处理器（`createTime`、`updateTime`）

### `security/` — 安全层

实现无状态 JWT 认证体系：

| 类 | 职责 |
|------|------|
| `LoginUser` | `UserDetails` 实现，封装用户信息与权限 `Set<String>` |
| `JwtAuthenticationFilter` | `OncePerRequestFilter`，从请求头解析 JWT、加载用户、注入 `SecurityContext` |
| `AuthEntryPoint` | 未认证请求返回 401 JSON 响应 |
| `UserDetailsServiceImpl` | 从数据库加载用户及其角色-菜单权限集合 |
| `PermissionService` | `@ss.hasPermi()` SpEL 表达式实现，供 `@PreAuthorize` 调用 |

### `entity/` — 数据库实体

12 个实体类，通过 MyBatis-Plus `@TableName` 映射数据库表。采用逻辑删除（`@TableLogic`）和自动填充（`@TableField(fill)`）。实体分两组：

- **系统实体**：`SysUser`、`SysDept`、`SysRole`、`SysMenu`、`SysLog` — RBAC 权限模型
- **业务实体**：`SysAsset`、`AssetBorrow`、`AssetAllocate`、`AssetRepair`、`AssetCheckTask`、`AssetCheckDetail`、`AuditRecord` — 资产全生命周期

### `dto/` — 请求参数对象

8 个 DTO 类封装前端请求参数。`BaseQueryDTO` 作为分页基类，预留 `dataScope` 字段供 AOP 注入 SQL 片段。其他 DTO 按业务场景拆分（登录、资产查询/保存、借用申请、审批操作、报修申请、用户保存）。

### `vo/` — 响应视图对象

3 个 VO 类面向前端展示需求：

| VO | 说明 |
|------|------|
| `LoginVO` | 登录返回：Token + 权限列表 + 动态路由树 |
| `AssetVO` | 资产列表：在实体基础上补充部门名称、责任人姓名 |
| `RouterVO` | 动态路由节点：`path`、`component`、`meta`、`children` |

### `mapper/` — 数据访问层

12 个 Mapper 接口继承 MyBatis-Plus `BaseMapper`，简单 CRUD 由框架自动实现。复杂查询（多表关联、数据权限拼接、树状查询）在 `resources/mapper/*.xml` 中编写原生 SQL。

### `service/` — 业务逻辑层

8 个 Service 类封装核心业务规则：

| Service | 职责 |
|------|------|
| `AuthService` | 登录认证、JWT 签发、动态路由树构建 |
| `SysUserService` | 用户 CRUD、密码重置 |
| `SysDeptService` | 部门树 CRUD |
| `SysRoleService` | 角色 CRUD、角色-菜单绑定 |
| `SysAssetService` | 资产 CRUD、借用/归还、QR 码生成、统计 |
| `AuditService` | 通用审批（借用/调拨/报修/报废） |
| `RepairService` | 报修工单：提交、接单、完成 |
| `AssetCheckService` | 盘点任务创建、扫码盘点、完成汇总 |

### `controller/` — REST 接口层

10 个 Controller 提供 RESTful API，统一使用 `@PreAuthorize("@ss.hasPermi('xxx')")` 进行方法级权限控制，使用 `@Log` 注解记录操作日志。接口前缀 `/api`（通过 `context-path` 配置）。

### `utils/` — 工具类

- **`JwtUtils`**：JWT 签发（载荷含 userId、username）与校验解析
- **`QrCodeUtils`**：基于 ZXing 生成二维码 Base64 字符串，新增资产时自动调用
