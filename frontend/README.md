# 前端 — 高校固定资产全生命周期管理系统

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5 | Composition API + `<script setup>` |
| TypeScript | 5.9 | 类型安全 |
| Vite | 5.4 | 开发服务器与生产打包 |
| Element Plus | 2.13 | PC 端 UI 组件库 |
| Tailwind CSS | 4.x | 原子化样式与响应式布局 |
| Pinia | 3.0 | 全局状态管理 |
| Vue Router | 5.0 | 路由与导航守卫 |
| Axios | 1.x | HTTP 请求封装 |
| ECharts | 6.x | 数据可视化图表 |
| html5-qrcode | 2.3 | 摄像头扫码（WebRTC） |

---

## 目录结构

```
frontend/
├── index.html                        # SPA 入口 HTML
├── vite.config.ts                    # Vite 配置
├── tsconfig.app.json                 # TypeScript 配置
├── package.json                      # 依赖与脚本
├── public/                           # 静态资源
└── src/
    ├── main.ts                       # 应用入口
    ├── App.vue                       # 根组件
    ├── style.css                     # 全局样式
    ├── api/                          # 接口层
    │   ├── auth.ts                   #   登录 / 登出
    │   ├── asset.ts                  #   资产 CRUD / 借用 / 归还
    │   ├── audit.ts                  #   审批相关
    │   ├── repair.ts                 #   报修 / 维修工单
    │   ├── check.ts                  #   盘点任务
    │   └── system.ts                 #   用户 / 角色 / 部门 / 日志
    ├── components/                   # 公共组件
    │   └── QrScanner.vue             #   摄像头扫码组件
    ├── directive/                    # 自定义指令
    │   └── permission.ts             #   v-permission 权限指令
    ├── layout/                       # 布局层
    │   ├── index.vue                 #   主布局（侧边栏 + 顶栏 + 内容区）
    │   └── SidebarMenu.vue           #   侧边导航菜单
    ├── router/                       # 路由
    │   └── index.ts                  #   路由表 + beforeEach 守卫
    ├── store/                        # 状态管理
    │   └── user.ts                   #   用户信息 / 权限 / Token
    ├── utils/                        # 工具
    │   └── request.ts                #   Axios 实例封装
    └── views/                        # 页面视图
        ├── login/index.vue           #   登录页
        ├── dashboard/                #   工作台
        │   ├── index.vue             #     权限分发页
        │   ├── AdminDashboard.vue    #     管理员仪表盘
        │   └── StudentDashboard.vue  #     师生仪表盘
        ├── asset/                    #   资产管理
        │   ├── list.vue              #     资产列表
        │   ├── detail.vue            #     资产详情
        │   ├── check.vue             #     资产盘点
        │   └── allocate.vue          #     跨部门调拨
        ├── audit/                    #   审批中心
        │   ├── todo.vue              #     待办审批
        │   └── history.vue           #     我的申请
        ├── ops/repair/               #   运维管理
        │   ├── apply.vue             #     报修申请
        │   └── manage.vue            #     维修工单池
        └── system/                   #   系统管理
            ├── user/index.vue        #     用户管理
            ├── role/index.vue        #     角色管理
            ├── dept/index.vue        #     部门管理
            └── log/index.vue         #     审计日志
```

---

## 包设计说明

### `src/api/` — 接口层

按业务模块拆分 API 文件，每个文件对应一组后端接口。所有请求通过 `utils/request.ts` 统一发送，自动注入 Token、处理错误码，页面组件无需关心 HTTP 细节。

### `src/components/` — 公共组件

存放跨页面复用的通用组件。`QrScanner.vue` 封装了 html5-qrcode 摄像头扫码能力，并提供手动输入降级方案，被资产借用、盘点等多个页面调用。

### `src/directive/` — 自定义指令

`v-permission` 指令实现按钮级权限控制。采用 `display:none` 而非 `removeChild` 策略，确保权限异步更新后 DOM 可恢复显示。同时注册 `mounted` 和 `updated` 双钩子，动态响应权限变化。

### `src/layout/` — 布局层

`index.vue` 作为主布局容器，PC 端固定 240px 侧边栏 + 顶栏 + 内容区，移动端切换为 Drawer 抽屉式侧边栏。`SidebarMenu.vue` 根据用户权限过滤菜单项，并监听路由变化自动展开对应父级菜单。

### `src/router/` — 路由

采用静态路由表 + `meta.perms` 权限标识方案。`beforeEach` 全局守卫负责：无 Token 跳转登录页、刷新后从 `sessionStorage` 恢复 Pinia 状态、缓存失效时重新认证。

### `src/store/` — 状态管理

Pinia `userStore` 管理全局用户状态（Token、角色、权限列表）。Token 存 `localStorage`（持久化），完整用户信息存 `sessionStorage`（Tab 级生命周期）。登录时写入，刷新时通过 `restoreFromCache()` 恢复。

### `src/utils/` — 工具层

`request.ts` 基于 Axios 封装统一请求实例：请求拦截器注入 `Authorization` 头；响应拦截器处理 `401`（跳登录）、`403`（静默）、其他错误（Toast 提示）。

### `src/views/` — 页面视图

按业务域划分子目录，每个目录对应一个导航模块：

| 目录 | 职责 |
|------|------|
| `login/` | 登录页，双栏布局（品牌区 + 表单区） |
| `dashboard/` | 工作台，按权限分发管理员 / 师生不同视图 |
| `asset/` | 资产全生命周期：列表、详情、盘点、调拨 |
| `audit/` | 审批流程：待办审批、我的申请与归还 |
| `ops/repair/` | 运维：报修申请、维修工单接单与完成 |
| `system/` | 系统管理：用户、角色、部门、审计日志 |
