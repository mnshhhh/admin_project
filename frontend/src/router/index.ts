import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'

export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'Odometer' }
      }
    ]
  },
  {
    path: '/asset',
    component: () => import('@/layout/index.vue'),
    meta: { title: '资产管理', icon: 'Box' },
    children: [
      {
        path: 'list',
        name: 'AssetList',
        component: () => import('@/views/asset/list.vue'),
        meta: { title: '资产列表', perms: ['asset:list'] }
      },
      {
        path: 'detail/:id',
        name: 'AssetDetail',
        component: () => import('@/views/asset/detail.vue'),
        meta: { title: '资产详情', hidden: true, perms: ['asset:list'] }
      },
      {
        path: 'allocate',
        name: 'AssetAllocate',
        component: () => import('@/views/asset/allocate.vue'),
        meta: { title: '跨部门调拨', perms: ['asset:allocate'] }
      },
      {
        path: 'check',
        name: 'AssetCheck',
        component: () => import('@/views/asset/check.vue'),
        meta: { title: '资产盘点', perms: ['asset:check'] }
      },
      {
        path: 'application',
        name: 'AssetApplication',
        component: () => import('@/views/asset/application/index.vue'),
        meta: { title: '资产申请', perms: ['asset:apply'] }
      },
      {
        path: 'purchase',
        name: 'PurchaseOrder',
        component: () => import('@/views/asset/application/purchase.vue'),
        meta: { title: '采购管理', perms: ['asset:po:manage'] }
      },
      {
        path: 'warehouse',
        name: 'WarehouseEntry',
        component: () => import('@/views/asset/application/warehouse.vue'),
        meta: { title: '资产入库', perms: ['asset:entry:manage'] }
      }
    ]
  },
  {
    path: '/ops',
    component: () => import('@/layout/index.vue'),
    meta: { title: '运维管理', icon: 'Tools' },
    children: [
      {
        path: 'repair/apply',
        name: 'RepairApply',
        component: () => import('@/views/ops/repair/apply.vue'),
        meta: { title: '报修申请', perms: ['repair:add'] }
      },
      {
        path: 'repair/manage',
        name: 'RepairManage',
        component: () => import('@/views/ops/repair/manage.vue'),
        meta: { title: '维修工单池', perms: ['repair:manage'] }
      }
    ]
  },
  {
    path: '/audit',
    component: () => import('@/layout/index.vue'),
    meta: { title: '审批中心', icon: 'Bell' },
    children: [
      {
        path: 'todo',
        name: 'AuditTodo',
        component: () => import('@/views/audit/todo.vue'),
        meta: { title: '待办审批', perms: ['audit:approve'] }
      },
      {
        path: 'history',
        name: 'AuditHistory',
        component: () => import('@/views/audit/history.vue'),
        meta: { title: '我的申请', perms: ['audit:view'] }
      },
      {
        path: 'application',
        name: 'ApplicationApproval',
        component: () => import('@/views/asset/application/approval.vue'),
        meta: { title: '资产申请审批', perms: ['asset:apply:approve'] }
      }
    ]
  },
  {
    path: '/system',
    component: () => import('@/layout/index.vue'),
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', perms: ['sys:user:list'] }
      },
      {
        path: 'role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', perms: ['sys:role:manage'] }
      },
      {
        path: 'dept',
        name: 'SystemDept',
        component: () => import('@/views/system/dept/index.vue'),
        meta: { title: '部门管理', perms: ['sys:dept:manage'] }
      },
      {
        path: 'log',
        name: 'SystemLog',
        component: () => import('@/views/system/log/index.vue'),
        meta: { title: '审计日志', perms: ['sys:log:view'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes
})

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()

  if (to.path === '/login') {
    if (userStore.token) return next('/')
    return next()
  }

  if (!userStore.token) {
    return next('/login')
  }

  // 刷新后 permissions 为空 —— 从 sessionStorage 缓存恢复
  if (!userStore.permissions.length) {
    const restored = userStore.restoreFromCache()
    if (!restored) {
      // 缓存也没有，跳回登录
      userStore.logout()
      return next('/login')
    }
  }

  next()
})

export default router
