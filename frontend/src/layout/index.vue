<template>
  <div class="app-layout">
    <!-- 移动端 Drawer -->
    <el-drawer
      v-model="drawerVisible"
      direction="ltr"
      :size="260"
      :with-header="false"
      class="sidebar-drawer"
    >
      <SidebarMenu @close="drawerVisible = false" />
    </el-drawer>

    <!-- PC 侧边栏 -->
    <aside class="app-sidebar">
      <SidebarMenu />
    </aside>

    <!-- 主内容区 -->
    <div class="app-main">
      <!-- Topbar -->
      <header class="app-topbar">
        <div class="topbar-left">
          <button class="mobile-menu-btn" @click="drawerVisible = true">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M3 5h14M3 10h14M3 15h14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </button>
          <div class="breadcrumb">
            <span class="breadcrumb-home">{{ parentTitle }}</span>
            <svg v-if="parentTitle && currentTitle !== parentTitle" width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M5 3l4 4-4 4" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span v-if="currentTitle !== parentTitle" class="breadcrumb-current">{{ currentTitle }}</span>
          </div>
        </div>

        <div class="topbar-right">
          <el-popover placement="bottom-end" :width="360" trigger="click">
            <template #reference>
              <button class="topbar-btn" title="通知">
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                  <path d="M9 2a5 5 0 00-5 5v3l-1.5 2.5h13L14 10V7a5 5 0 00-5-5zM7.5 14.5a1.5 1.5 0 003 0" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                <span v-if="unreadCount > 0" class="notif-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
              </button>
            </template>
            <div class="notif-panel">
              <div class="notif-header">
                <span class="notif-title">通知中心</span>
                <el-button link type="primary" size="small" @click="markAllRead" v-if="unreadCount > 0">全部已读</el-button>
              </div>
              <div class="notif-list" v-if="notifList.length > 0">
                <div v-for="item in notifList" :key="item.id" :class="['notif-item', !item.isRead && 'unread']" @click="handleNotifClick(item)">
                  <div class="notif-item-title">{{ item.title }}</div>
                  <div class="notif-item-content">{{ item.content }}</div>
                  <div class="notif-item-time">{{ item.createTime }}</div>
                </div>
              </div>
              <el-empty v-else description="暂无通知" :image-size="60" />
            </div>
          </el-popover>

          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-trigger">
              <div class="user-avatar-sm">{{ userStore.nickname.charAt(0) }}</div>
              <span class="user-name-sm">{{ userStore.nickname }}</span>
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path d="M3 5l4 4 4-4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <span style="color:#ef4444">退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="app-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-up" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessageBox } from 'element-plus'
import SidebarMenu from './SidebarMenu.vue'
import { constantRoutes } from '@/router'
import { getUnreadCount, getNotificationList, markAsRead, markAllAsRead } from '@/api/notification'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const drawerVisible = ref(false)
const unreadCount = ref(0)
const notifList = ref<any[]>([])

async function loadUnread() {
  try {
    const res: any = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch {}
}

async function loadNotifList() {
  try {
    const res: any = await getNotificationList({ pageNum: 1, pageSize: 10 })
    notifList.value = res.data?.rows || []
  } catch {}
}

async function handleNotifClick(item: any) {
  if (!item.isRead) {
    await markAsRead(item.id)
    item.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
  if (item.relatedType === 'APPLICATION') {
    router.push(`/asset/application`)
  }
}

async function markAllRead() {
  await markAllAsRead()
  unreadCount.value = 0
  notifList.value.forEach(i => i.isRead = 1)
}

loadUnread()
setInterval(loadUnread, 30000)

const currentTitle = computed(() => route.meta?.title as string || '工作台')
const parentTitle = computed(() => {
  for (const r of constantRoutes) {
    if (r.children) {
      for (const c of r.children) {
        if (`${r.path}/${c.path}` === route.path || route.path === `${r.path}/${c.path}`) {
          return r.meta?.title as string || ''
        }
      }
    }
  }
  return currentTitle.value
})

async function handleCommand(command: string) {
  if (command === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', { type: 'warning', confirmButtonText: '退出', cancelButtonText: '取消' })
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.app-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: #f8f9fc;
}

/* 侧边栏 */
.app-sidebar {
  width: 240px;
  flex-shrink: 0;
  display: none;
  height: 100vh;
  overflow: hidden;
}
@media (min-width: 1024px) { .app-sidebar { display: block; } }

:deep(.sidebar-drawer .el-drawer__body) { padding: 0; }
:deep(.sidebar-drawer .el-drawer) { background: #0f172a !important; }

/* 主区域 */
.app-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

/* Topbar */
.app-topbar {
  height: 56px;
  background: white;
  border-bottom: 1px solid #f0f1f6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.topbar-left { display: flex; align-items: center; gap: 12px; }

.mobile-menu-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  transition: all 0.15s;
}
.mobile-menu-btn:hover { background: #f1f5f9; color: #0f172a; }
@media (min-width: 1024px) { .mobile-menu-btn { display: none; } }

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}
.breadcrumb-home { color: #94a3b8; }
.breadcrumb-current { color: #1e293b; font-weight: 600; }

.topbar-right { display: flex; align-items: center; gap: 8px; }

.topbar-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}
.topbar-btn:hover { background: #f1f5f9; color: #0f172a; }

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
  color: #374151;
}
.user-trigger:hover { background: #f1f5f9; }

.user-avatar-sm {
  width: 28px;
  height: 28px;
  border-radius: 7px;
  background: linear-gradient(135deg, #6366f1, #a855f7);
  color: white;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.user-name-sm {
  font-size: 13px;
  font-weight: 600;
  display: none;
}
@media (min-width: 640px) { .user-name-sm { display: block; } }

/* 内容区 */
.app-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}
@media (min-width: 1024px) { .app-content { padding: 28px 32px; } }

/* 页面切换动画 */
.fade-up-enter-active { transition: all 0.2s ease; }
.fade-up-leave-active { transition: all 0.15s ease; }
.fade-up-enter-from { opacity: 0; transform: translateY(8px); }
.fade-up-leave-to { opacity: 0; transform: translateY(-4px); }

/* 通知 */
.notif-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  min-width: 16px;
  height: 16px;
  border-radius: 8px;
  background: #ef4444;
  color: white;
  font-size: 10px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}
.notif-panel { padding: 0; }
.notif-header { display: flex; justify-content: space-between; align-items: center; padding: 8px 0 12px; border-bottom: 1px solid #f0f1f6; }
.notif-title { font-size: 14px; font-weight: 700; color: #0f172a; }
.notif-list { max-height: 360px; overflow-y: auto; }
.notif-item { padding: 12px 0; border-bottom: 1px solid #f8f9fc; cursor: pointer; transition: background 0.15s; }
.notif-item:hover { background: #f8f9fc; margin: 0 -12px; padding: 12px; }
.notif-item.unread .notif-item-title { color: #0f172a; }
.notif-item-title { font-size: 13px; font-weight: 600; color: #64748b; margin-bottom: 4px; }
.notif-item-content { font-size: 12px; color: #94a3b8; line-height: 1.5; }
.notif-item-time { font-size: 11px; color: #cbd5e1; margin-top: 4px; }
</style>
