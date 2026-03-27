# 前端架构方案说明书 (frontend_proposal.md)

## 1. 技术栈选型

*   **核心框架**：Vue 3 (Composition API) + TypeScript
*   **构建工具**：Vite
*   **UI 框架**：
    *   **Element Plus**：主 UI 库，用于 PC 端及通用复杂组件（表格、树、弹窗）。
    *   **Tailwind CSS**：原子化 CSS，核心用于实现**响应式布局**与**移动端适配**。
*   **状态管理**：Pinia (存储 User Profile, Permissions List, Dictionary)。
*   **权限控制**：自定义指令 `v-permission` + Vue Router 全局守卫。
*   **扫码组件**：`html5-qrcode` (封装为通用组件 `<QrScanner />`)。

---

## 2. 权限驱动的架构设计

前端不再硬编码“PC版”或“移动版”页面，而是通过**权限点**动态组装界面。

### 2.1 权限控制流程
1.  **登录 (Login)**：获取 `token` 存储至 Cookie/LocalStorage。
2.  **获取用户信息 (GetInfo)**：
    *   调用 `/api/user/info`，后端返回：
        *   `roles`: `['admin', 'dept_manager']`
        *   `permissions`: `['asset:add', 'asset:scan', 'sys:user:view']`
3.  **动态路由生成 (GenerateRoutes)**：
    *   根据 `permissions` 过滤路由表（Route Map）。
    *   例如：只有拥有 `sys:manage` 权限，才会注册“系统管理”相关路由。
    *   使用 `router.addRoute()` 动态挂载。

### 2.2 指令级权限控制 (`v-permission`)
封装 Vue 指令，实现按钮/元素级的细粒度控制。

```typescript
// directives/permission.ts
import { useUserStore } from '@/store/user'

export const permission = {
  mounted(el: HTMLElement, binding: any) {
    const { value } = binding
    const userStore = useUserStore()
    const permissions = userStore.permissions

    if (value && value instanceof Array && value.length > 0) {
      const permissionRoles = value
      const hasPermission = permissions.some(role => permissionRoles.includes(role))

      if (!hasPermission) {
        // 无权限则移除 DOM 元素
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else {
      throw new Error(`need roles! Like v-permission="['asset:add','asset:edit']"`)
    }
  }
}
```

**使用示例**：
```html
<!-- 只有拥有 'asset:add' 权限的用户能看到新增按钮 -->
<el-button v-permission="['asset:add']">新增资产</el-button>

<!-- 只有拥有 'asset:scan' 权限（通常是手机端操作较多）能看到扫码按钮 -->
<div v-permission="['asset:scan']" class="scan-btn">...</div>
```

---

## 3. 响应式布局与 Dashboard 设计

### 3.1 统一布局 (Layout)
采用 **"Sidebar + Navbar + AppMain"** 的经典布局，利用 Tailwind 实现响应式切换。

*   **Navbar**：顶部导航。移动端时显示“汉堡菜单”触发侧边栏。
*   **Sidebar**：
    *   **PC (lg:block)**：固定左侧。
    *   **Mobile (hidden)**：默认隐藏，通过 Drawer 抽屉组件弹出。
*   **AppMain**：内容区域。

### 3.2 角色化 Dashboard
首页 (`/dashboard`) 根据用户角色动态渲染不同组件：

*   **对于学生 (Student)**：
    *   渲染 `<MyAssetsCard />` (名下资产)
    *   渲染 `<ScanAction />` (大号扫码按钮)
*   **对于管理员 (Admin)**：
    *   渲染 `<DataPanel />` (资产统计图表)
    *   渲染 `<AuditTodo />` (待办审批列表)

```vue
<!-- Dashboard.vue 伪代码 -->
<template>
  <div class="dashboard-container">
    <component :is="currentRoleDashboard" />
  </div>
</template>

<script setup>
import AdminDashboard from './editor/index.vue'
import StudentDashboard from './student/index.vue'
// 根据权限判断显示哪个 Dashboard
const currentRoleDashboard = computed(() => {
  if (userStore.roles.includes('admin')) return AdminDashboard
  return StudentDashboard
})
</script>
```

---

## 4. 核心业务组件设计

### 4.1 智能表格组件 (`SmartTable`)
封装 Element Plus Table，集成权限与响应式。
*   **Desktop**：展示完整列（编号、名称、型号、价格、入库时间、状态）。
*   **Mobile**：自动隐藏次要列，仅展示（名称、状态、操作），或切换为 Card List 视图。

### 4.2 全能扫码组件 (`QrCodeScanner`)
*   **功能**：封装 `html5-qrcode`。
*   **鉴权**：组件内部检查 HTTPS 环境。
*   **交互**：提供全屏 Overlay，中间镂空扫描框。
*   **输出**：扫描成功后 emit `on-scan(text)` 事件，父组件根据 text (AssetID) 跳转详情页。

---

## 5. 目录结构规范

```text
src/
├── api/                # API 接口 (按模块: asset, user, sys)
├── assets/             # 静态资源
├── components/         # 通用组件 (QrCodeScanner, SmartTable)
├── directive/          # 自定义指令 (v-permission)
├── layout/             # 布局组件
├── router/             # 路由配置 (包含 constantRoutes 和 asyncRoutes)
├── store/              # Pinia 状态 (user, permission, app)
├── views/              # 页面视图
│   ├── dashboard/      # 仪表盘 (含分角色视图)
│   ├── asset/          # 资产管理
│   ├── system/         # 系统管理 (用户、角色、菜单)
│   └── login/          # 登录页
└── utils/              # 工具类 (request, auth)
```
