<template>
  <div class="sidebar">
    <!-- Logo -->
    <div class="sidebar-logo">
      <div class="logo-icon">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M4 8l6-4 6 4v5l-6 4-6-4V8z" stroke="white" stroke-width="1.5" stroke-linejoin="round"/>
          <path d="M4 8l6 4 6-4" stroke="white" stroke-width="1.5" stroke-linejoin="round"/>
          <path d="M10 12v5" stroke="white" stroke-width="1.5"/>
        </svg>
      </div>
      <div class="logo-text">
        <span class="logo-name">AssetMS</span>
        <span class="logo-sub">资产管理平台</span>
      </div>
    </div>

    <!-- 用户信息 -->
    <div class="sidebar-user">
      <div class="user-avatar">{{ userStore.nickname.charAt(0) }}</div>
      <div class="user-info">
        <div class="user-name">{{ userStore.nickname }}</div>
        <div class="user-role">{{ roleLabel }}</div>
      </div>
    </div>

    <!-- 导航菜单 -->
    <nav class="sidebar-nav">
      <template v-for="group in menuGroups" :key="group.title">
        <div class="nav-group-label">{{ group.title }}</div>
        <template v-for="item in group.items" :key="item.path">
          <!-- 有子菜单 -->
          <div v-if="item.children && item.children.length > 1" class="nav-group-item">
            <button
              class="nav-item nav-parent"
              :class="{ 'is-open': openGroups.includes(item.path) }"
              @click="toggleGroup(item.path)"
            >
              <span class="nav-icon">
                <component :is="item.icon" />
              </span>
              <span class="nav-label">{{ item.title }}</span>
              <span class="nav-arrow">
                <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                  <path d="M3 4.5l3 3 3-3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
            </button>
            <div class="nav-children" :class="{ 'is-open': openGroups.includes(item.path) }">
              <router-link
                v-for="child in item.children"
                :key="child.path"
                :to="child.path"
                class="nav-child"
                :class="{ 'is-active': route.path === child.path }"
                @click="emit('close')"
              >
                <span class="child-dot" />
                {{ child.title }}
              </router-link>
            </div>
          </div>
          <!-- 单菜单 -->
          <router-link
            v-else
            :to="item.children?.length === 1 ? item.children[0].path : item.path"
            class="nav-item"
            :class="{ 'is-active': isActiveItem(item) }"
            @click="emit('close')"
          >
            <span class="nav-icon">
              <component :is="item.icon" />
            </span>
            <span class="nav-label">{{ item.children?.length === 1 ? item.children[0].title : item.title }}</span>
          </router-link>
        </template>
      </template>
    </nav>

    <!-- 底部 -->
    <div class="sidebar-footer">
      <button class="logout-btn" @click="handleLogout">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M6 2H3a1 1 0 00-1 1v10a1 1 0 001 1h3M10 11l3-3-3-3M13 8H6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        退出登录
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { constantRoutes } from '@/router'
import { ElMessageBox } from 'element-plus'
import {
  Odometer, Box, Tools, Bell, Setting,
  DataAnalysis
} from '@element-plus/icons-vue'

const emit = defineEmits(['close'])
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

function getActiveParentPaths(currentPath: string): string[] {
  const result: string[] = []
  for (const r of constantRoutes) {
    if (!r.children) continue
    for (const c of r.children) {
      if (`${r.path}/${c.path}` === currentPath) {
        result.push(r.path)
        break
      }
    }
  }
  return result
}

const openGroups = ref<string[]>(getActiveParentPaths(route.path))

watch(() => route.path, (newPath) => {
  const parents = getActiveParentPaths(newPath)
  for (const p of parents) {
    if (!openGroups.value.includes(p)) {
      openGroups.value.push(p)
    }
  }
})

const iconMap: Record<string, any> = {
  Odometer, Box, Tools, Bell, Setting, DataAnalysis,
  'chart': DataAnalysis,
}

const roleLabel = computed(() => {
  if (userStore.roles.includes('admin')) return '超级管理员'
  if (userStore.roles.includes('dept_admin')) return '部门管理员'
  if (userStore.roles.includes('repair_man')) return '维修人员'
  return '普通用户'
})

interface NavChild { title: string; path: string }
interface NavItem { title: string; path: string; icon: any; children?: NavChild[] }
interface NavGroup { title: string; items: NavItem[] }

const menuGroups = computed<NavGroup[]>(() => {
  const allRoutes = constantRoutes.filter(
    r => r.children && !r.meta?.hidden && r.path !== '/login' && r.path !== '/:pathMatch(.*)*'
  )
  const groups: NavGroup[] = []
  const mainItems: NavItem[] = []
  const systemItems: NavItem[] = []

  for (const r of allRoutes) {
    const children = (r.children || []).filter((c: any) => {
      if (c.meta?.hidden) return false
      const perms = c.meta?.perms as string[] | undefined
      if (!perms || perms.length === 0) return true
      return perms.some((p: string) => userStore.hasPermission(p))
    }).map((c: any) => ({
      title: c.meta?.title as string,
      path: `${r.path}/${c.path}`
    }))

    if (children.length === 0) continue

    const item: NavItem = {
      title: r.meta?.title as string,
      path: r.path,
      icon: iconMap[r.meta?.icon as string] || Box,
      children
    }

    if (r.path === '/system') systemItems.push(item)
    else mainItems.push(item)
  }

  if (mainItems.length) groups.push({ title: '主要功能', items: mainItems })
  if (systemItems.length) groups.push({ title: '系统配置', items: systemItems })
  return groups
})

function toggleGroup(path: string) {
  const idx = openGroups.value.indexOf(path)
  if (idx >= 0) openGroups.value.splice(idx, 1)
  else openGroups.value.push(path)
}

function isActiveItem(item: NavItem) {
  if (item.children?.length === 1) return route.path === item.children[0].path
  return route.path.startsWith(item.path)
}

async function handleLogout() {
  await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', { type: 'warning', confirmButtonText: '退出', cancelButtonText: '取消' })
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.sidebar {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #0f172a;
  color: #e2e8f0;
  overflow: hidden;
}

/* Logo */
.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 20px 16px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}
.logo-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #6366f1, #818cf8);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(99,102,241,0.4);
}
.logo-name { display: block; font-size: 15px; font-weight: 700; color: white; letter-spacing: -0.2px; }
.logo-sub { display: block; font-size: 11px; color: #475569; margin-top: 1px; }

/* 用户 */
.sidebar-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  margin: 12px 12px 4px;
  background: rgba(255,255,255,0.04);
  border-radius: 10px;
  border: 1px solid rgba(255,255,255,0.06);
}
.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #6366f1, #a855f7);
  color: white;
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.user-name { font-size: 13px; font-weight: 600; color: #f1f5f9; }
.user-role { font-size: 11px; color: #475569; margin-top: 1px; }

/* 导航 */
.sidebar-nav {
  flex: 1;
  padding: 8px 12px;
  overflow-y: auto;
}
.nav-group-label {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #334155;
  padding: 12px 8px 6px;
}

/* 通用 nav-item */
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 10px;
  border-radius: 8px;
  color: #94a3b8;
  font-size: 13.5px;
  font-weight: 500;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.15s;
  width: 100%;
  border: none;
  background: transparent;
  margin-bottom: 2px;
}
.nav-item:hover { background: rgba(255,255,255,0.06); color: #e2e8f0; }
.nav-item.is-active {
  background: rgba(99,102,241,0.15);
  color: #a5b4fc;
}
.nav-item.is-active .nav-icon { color: #818cf8; }

.nav-icon {
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 16px;
}
.nav-label { flex: 1; }
.nav-arrow {
  transition: transform 0.2s;
  color: #475569;
  display: flex;
}
.nav-parent.is-open .nav-arrow { transform: rotate(180deg); }

/* 子菜单 */
.nav-children {
  display: none;
  flex-direction: column;
  padding-left: 28px;
  padding-bottom: 4px;
}
.nav-children.is-open { display: flex; }

.nav-child {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 10px;
  border-radius: 6px;
  color: #64748b;
  font-size: 13px;
  text-decoration: none;
  transition: all 0.15s;
  margin-bottom: 1px;
}
.nav-child:hover { color: #94a3b8; background: rgba(255,255,255,0.04); }
.nav-child.is-active { color: #a5b4fc; background: rgba(99,102,241,0.1); }
.child-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: currentColor;
  opacity: 0.6;
  flex-shrink: 0;
}

/* 底部 */
.sidebar-footer {
  padding: 12px;
  border-top: 1px solid rgba(255,255,255,0.06);
}
.logout-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 12px;
  border-radius: 8px;
  color: #64748b;
  font-size: 13px;
  cursor: pointer;
  border: none;
  background: transparent;
  width: 100%;
  transition: all 0.15s;
}
.logout-btn:hover { background: rgba(239,68,68,0.1); color: #f87171; }
</style>
