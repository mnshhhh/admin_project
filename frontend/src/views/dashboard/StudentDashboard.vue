<template>
  <div class="dashboard">
    <div class="welcome-bar">
      <div>
        <h2 class="welcome-title">你好，{{ userStore.nickname }} 👋</h2>
        <p class="welcome-sub">快速完成借用、报修等日常操作</p>
      </div>
    </div>

    <div class="action-grid">
      <button v-for="a in actions" :key="a.label" class="action-card" @click="router.push(a.to)">
        <div class="action-icon" :style="{ background: a.bg }">
          <component :is="a.icon" :style="{ color: a.color, fontSize: '22px' }" />
        </div>
        <div class="action-label">{{ a.label }}</div>
        <div class="action-desc">{{ a.desc }}</div>
      </button>
    </div>

    <div class="panel">
      <div class="panel-header">
        <h3>我的借用记录</h3>
        <router-link to="/audit/history" class="panel-link">查看全部</router-link>
      </div>
      <el-table :data="myBorrows" class="borrow-table">
        <el-table-column prop="assetName" label="资产名称" />
        <el-table-column prop="borrowNo" label="申请单号" width="180">
          <template #default="{ row }">
            <span class="mono-text">{{ row.borrowNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="expectedReturnTime" label="归还时间" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-badge', `bstatus-${row.status}`]">{{ statusLabel(row.status) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!myBorrows.length" class="empty-panel">暂无借用记录</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getMyBorrows } from '@/api/audit'
import { Camera, Tools, List, Bell } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const myBorrows = ref<any[]>([])

const actions = [
  { label: '扫码借用', desc: '扫描资产二维码发起借用', to: '/asset/list', icon: Camera, bg: '#eef2ff', color: '#6366f1' },
  { label: '报修申请', desc: '上报资产故障，拍照记录', to: '/ops/repair/apply', icon: Tools, bg: '#fffbeb', color: '#f59e0b' },
  { label: '资产查询', desc: '查看所有可借用资产', to: '/asset/list', icon: List, bg: '#ecfdf5', color: '#10b981' },
  { label: '我的申请', desc: '查看审批进度与历史', to: '/audit/history', icon: Bell, bg: '#fef2f2', color: '#ef4444' },
]

function statusLabel(s: string) {
  return { PENDING: '待审批', APPROVED: '已批准', REJECTED: '已驳回', BORROWED: '借用中', RETURNED: '已归还' }[s] || s
}

onMounted(async () => {
  try { const res: any = await getMyBorrows({ pageNum: 1, pageSize: 5 }); myBorrows.value = res.data?.rows || [] } catch {}
})
</script>

<style scoped>
.dashboard { display: flex; flex-direction: column; gap: 20px; }

.welcome-bar {
  padding: 24px 28px;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  border-radius: 16px;
  color: white;
}
.welcome-title { font-size: 22px; font-weight: 800; letter-spacing: -0.3px; margin-bottom: 4px; }
.welcome-sub { font-size: 14px; opacity: 0.5; }

.action-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14px; }
@media (min-width: 640px) { .action-grid { grid-template-columns: repeat(4, 1fr); } }

.action-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  padding: 20px;
  background: white;
  border-radius: 14px;
  border: 1px solid #f0f1f6;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
}
.action-card:hover { border-color: #c7d2fe; box-shadow: 0 4px 16px rgba(99,102,241,0.1); transform: translateY(-2px); }
.action-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.action-label { font-size: 14px; font-weight: 700; color: #1e293b; }
.action-desc { font-size: 12px; color: #94a3b8; line-height: 1.4; }

.panel { background: white; border-radius: 14px; border: 1px solid #f0f1f6; box-shadow: 0 1px 3px rgba(0,0,0,0.04); overflow: hidden; }
.panel-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid #f8f9fc; }
.panel-header h3 { font-size: 14px; font-weight: 700; color: #1e293b; }
.panel-link { font-size: 13px; color: #6366f1; text-decoration: none; }
.panel-link:hover { text-decoration: underline; }

.borrow-table { width: 100%; }
.mono-text { font-family: ui-monospace, monospace; font-size: 12px; color: #64748b; }
.empty-panel { text-align: center; padding: 40px; color: #94a3b8; font-size: 13px; }

.status-badge { display: inline-flex; align-items: center; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.bstatus-PENDING { background: #fffbeb; color: #d97706; }
.bstatus-BORROWED { background: #eef2ff; color: #4f46e5; }
.bstatus-RETURNED { background: #f1f5f9; color: #64748b; }
.bstatus-REJECTED { background: #fef2f2; color: #ef4444; }
</style>
