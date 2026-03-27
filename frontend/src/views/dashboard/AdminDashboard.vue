<template>
  <div class="dashboard">
    <!-- 欢迎栏 -->
    <div class="welcome-bar">
      <div>
        <h2 class="welcome-title">早上好，{{ userStore.nickname }} 👋</h2>
        <p class="welcome-sub">这是今天的资产管理概览</p>
      </div>
      <div class="welcome-date">{{ dateStr }}</div>
    </div>

    <!-- 数据卡片 -->
    <div class="stat-grid">
      <div v-for="card in statCards" :key="card.label" class="stat-card">
        <div class="stat-icon" :style="{ background: card.bg }">
          <component :is="card.icon" :style="{ color: card.color }" />
        </div>
        <div class="stat-body">
          <div class="stat-value num">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
        <div class="stat-trend" :class="card.trendClass">
          <svg width="40" height="24" viewBox="0 0 40 24" fill="none">
            <path :d="card.sparkline" :stroke="card.color" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
          </svg>
        </div>
      </div>
    </div>

    <!-- 中部两列 -->
    <div class="mid-grid">
      <!-- 资产状态分布 -->
      <div class="panel">
        <div class="panel-header">
          <h3>资产状态分布</h3>
          <span class="panel-badge">共 {{ totalAssets }} 件</span>
        </div>
        <div class="status-chart">
          <div v-for="item in statusStats" :key="item.status" class="status-row">
            <div class="status-meta">
              <span class="status-dot" :style="{ background: statusColorMap[item.status] }"/>
              <span class="status-name">{{ statusMap[item.status] || item.status }}</span>
            </div>
            <div class="status-bar-wrap">
              <div
                class="status-bar-fill"
                :style="{
                  width: totalAssets ? `${(item.count / totalAssets) * 100}%` : '0%',
                  background: statusColorMap[item.status]
                }"
              />
            </div>
            <span class="status-count num">{{ item.count }}</span>
          </div>
          <div v-if="!statusStats.length" class="empty-tip">暂无数据</div>
        </div>
      </div>

      <!-- 待办审批 -->
      <div class="panel">
        <div class="panel-header">
          <h3>待办审批</h3>
          <span v-if="pendingAudits.length" class="panel-badge urgent">{{ pendingAudits.length }} 待处理</span>
        </div>
        <div v-if="pendingAudits.length" class="audit-list">
          <div v-for="item in pendingAudits.slice(0, 5)" :key="item.id" class="audit-item">
            <div class="audit-icon">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M8 2a5 5 0 100 10A5 5 0 008 2zM8 6v4M8 5v.5" stroke="#6366f1" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="audit-info">
              <div class="audit-asset">{{ item.assetName }}</div>
              <div class="audit-no">{{ item.borrowNo }}</div>
            </div>
            <div class="audit-actions">
              <button class="btn-approve" @click="quickAudit(item.id, 'APPROVE')">通过</button>
              <button class="btn-reject" @click="quickAudit(item.id, 'REJECT')">驳回</button>
            </div>
          </div>
        </div>
        <div v-else class="empty-panel">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none" class="empty-icon">
            <path d="M20 4a16 16 0 100 32A16 16 0 0020 4zM20 14v8M20 24v2" stroke="#d1d5db" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <p>暂无待办事项</p>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-panel">
      <div class="panel-header">
        <h3>快捷操作</h3>
      </div>
      <div class="quick-grid">
        <router-link v-for="q in quickLinks" :key="q.label" :to="q.to" class="quick-item">
          <div class="quick-icon" :style="{ background: q.bg }">
            <component :is="q.icon" :style="{ color: q.color }" />
          </div>
          <span class="quick-label">{{ q.label }}</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getStatusStats } from '@/api/asset'
import { getPendingAudits, auditAction } from '@/api/audit'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { Box, Tools, Bell, DocumentChecked, List, Setting, Odometer, ShoppingCart } from '@element-plus/icons-vue'

const userStore = useUserStore()
const statusStats = ref<any[]>([])
const pendingAudits = ref<any[]>([])

const dateStr = computed(() => {
  return new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
})

const statusMap: Record<string, string> = { IDLE: '闲置', IN_USE: '在用', REPAIR: '维修中', SCRAPPED: '已报废' }
const statusColorMap: Record<string, string> = { IDLE: '#10b981', IN_USE: '#6366f1', REPAIR: '#f59e0b', SCRAPPED: '#94a3b8' }

const totalAssets = computed(() => statusStats.value.reduce((s, i) => s + i.count, 0))

const statCards = computed(() => [
  {
    label: '资产总数', value: totalAssets.value, icon: Box,
    bg: '#eef2ff', color: '#6366f1',
    sparkline: 'M2 20 L10 14 L18 16 L26 10 L34 8 L38 6',
    trendClass: ''
  },
  {
    label: '在用资产', value: statusStats.value.find(i => i.status === 'IN_USE')?.count || 0, icon: DocumentChecked,
    bg: '#ecfdf5', color: '#10b981',
    sparkline: 'M2 18 L10 16 L18 12 L26 10 L34 8 L38 6',
    trendClass: ''
  },
  {
    label: '维修中', value: statusStats.value.find(i => i.status === 'REPAIR')?.count || 0, icon: Tools,
    bg: '#fffbeb', color: '#f59e0b',
    sparkline: 'M2 12 L10 14 L18 16 L26 12 L34 14 L38 16',
    trendClass: ''
  },
  {
    label: '待审批', value: pendingAudits.value.length, icon: Bell,
    bg: '#fef2f2', color: '#ef4444',
    sparkline: 'M2 16 L10 18 L18 14 L26 16 L34 12 L38 10',
    trendClass: ''
  },
])

const quickLinks = [
  { label: '资产列表', to: '/asset/list', icon: List, bg: '#eef2ff', color: '#6366f1' },
  { label: '新增资产', to: '/asset/list', icon: Box, bg: '#ecfdf5', color: '#10b981' },
  { label: '资产申请', to: '/asset/application', icon: ShoppingCart, bg: '#fef9c3', color: '#eab308' },
  { label: '待办审批', to: '/audit/todo', icon: Bell, bg: '#fffbeb', color: '#f59e0b' },
  { label: '维修工单', to: '/ops/repair/manage', icon: Tools, bg: '#fef2f2', color: '#ef4444' },
  { label: '资产盘点', to: '/asset/check', icon: Odometer, bg: '#f0f9ff', color: '#0ea5e9' },
  { label: '系统管理', to: '/system/user', icon: Setting, bg: '#faf5ff', color: '#a855f7' },
]

async function loadData() {
  try {
    const statsRes: any = await getStatusStats()
    statusStats.value = statsRes.data || []
  } catch {}

  if (userStore.hasPermission('audit:approve')) {
    try {
      const auditRes: any = await getPendingAudits({ pageNum: 1, pageSize: 10 })
      pendingAudits.value = auditRes.data?.rows || []
    } catch {}
  }
}

async function quickAudit(bizId: number, action: string) {
  await auditAction({ bizId, bizType: 'BORROW', action })
  ElMessage.success(action === 'APPROVE' ? '已通过审批' : '已驳回')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.dashboard { display: flex; flex-direction: column; gap: 24px; }

/* 欢迎栏 */
.welcome-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding: 24px 28px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 16px;
  color: white;
  box-shadow: 0 8px 24px rgba(99,102,241,0.3);
}
.welcome-title { font-size: 22px; font-weight: 800; letter-spacing: -0.3px; margin-bottom: 4px; }
.welcome-sub { font-size: 14px; opacity: 0.75; }
.welcome-date { font-size: 13px; opacity: 0.6; white-space: nowrap; }

/* 数据卡片 */
.stat-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
@media (min-width: 1024px) { .stat-grid { grid-template-columns: repeat(4, 1fr); } }

.stat-card {
  background: white;
  border-radius: 14px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  border: 1px solid #f0f1f6;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  position: relative;
  overflow: hidden;
  transition: all 0.2s;
}
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.08); }

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}
.stat-value { font-size: 26px; font-weight: 800; color: #0f172a; line-height: 1; letter-spacing: -0.5px; }
.stat-label { font-size: 12px; color: #94a3b8; margin-top: 4px; font-weight: 500; }
.stat-trend { position: absolute; right: 12px; bottom: 12px; opacity: 0.6; }

/* 中部两列 */
.mid-grid { display: grid; grid-template-columns: 1fr; gap: 16px; }
@media (min-width: 1024px) { .mid-grid { grid-template-columns: 1fr 1fr; } }

/* Panel 通用 */
.panel {
  background: white;
  border-radius: 14px;
  border: 1px solid #f0f1f6;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  overflow: hidden;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f8f9fc;
}
.panel-header h3 { font-size: 14px; font-weight: 700; color: #1e293b; }
.panel-badge {
  font-size: 12px;
  background: #f1f5f9;
  color: #64748b;
  padding: 3px 10px;
  border-radius: 20px;
  font-weight: 500;
}
.panel-badge.urgent { background: #fef2f2; color: #ef4444; }

/* 状态图 */
.status-chart { padding: 16px 20px; display: flex; flex-direction: column; gap: 14px; }
.status-row { display: flex; align-items: center; gap: 12px; }
.status-meta { display: flex; align-items: center; gap: 7px; width: 80px; flex-shrink: 0; }
.status-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.status-name { font-size: 13px; color: #64748b; }
.status-bar-wrap { flex: 1; height: 6px; background: #f1f5f9; border-radius: 99px; overflow: hidden; }
.status-bar-fill { height: 100%; border-radius: 99px; transition: width 0.6s ease; }
.status-count { font-size: 13px; font-weight: 600; color: #374151; width: 28px; text-align: right; }

/* 审批列表 */
.audit-list { display: flex; flex-direction: column; }
.audit-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  border-bottom: 1px solid #f8f9fc;
  transition: background 0.15s;
}
.audit-item:last-child { border-bottom: none; }
.audit-item:hover { background: #fafbff; }
.audit-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #eef2ff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.audit-info { flex: 1; min-width: 0; }
.audit-asset { font-size: 13px; font-weight: 600; color: #1e293b; truncate: true; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.audit-no { font-size: 11px; color: #94a3b8; margin-top: 2px; }
.audit-actions { display: flex; gap: 6px; flex-shrink: 0; }

.btn-approve, .btn-reject {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.15s;
}
.btn-approve { background: #ecfdf5; color: #059669; }
.btn-approve:hover { background: #d1fae5; }
.btn-reject { background: #fef2f2; color: #dc2626; }
.btn-reject:hover { background: #fee2e2; }

.empty-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #94a3b8;
  font-size: 13px;
  gap: 12px;
}

/* 快捷操作 */
.quick-panel {
  background: white;
  border-radius: 14px;
  border: 1px solid #f0f1f6;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  overflow: hidden;
}
.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0;
  padding: 16px 20px;
}
@media (min-width: 640px) { .quick-grid { grid-template-columns: repeat(6, 1fr); } }

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  border-radius: 12px;
  text-decoration: none;
  transition: all 0.15s;
  color: #374151;
}
.quick-item:hover { background: #f8f9fc; transform: translateY(-2px); }
.quick-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}
.quick-label { font-size: 12px; font-weight: 600; color: #374151; }

.empty-tip { color: #94a3b8; font-size: 13px; text-align: center; padding: 20px; }
</style>
