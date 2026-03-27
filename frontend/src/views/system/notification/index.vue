<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">通知中心</h1>
        <p class="page-desc">查看系统通知和资产入库消息</p>
      </div>
      <el-button v-if="unreadCount > 0" type="primary" plain @click="handleMarkAllRead">全部已读</el-button>
    </div>

    <div class="filter-bar">
      <div class="filter-tabs">
        <button v-for="tab in tabs" :key="tab.value" :class="['tab-btn', filterIsRead === tab.value && 'active']" @click="switchTab(tab.value)">
          {{ tab.label }}
        </button>
      </div>
    </div>

    <div class="table-panel">
      <el-table :data="list" v-loading="loading" @row-click="handleRowClick">
        <el-table-column label="" width="40">
          <template #default="{ row }">
            <span :class="['dot', !row.isRead && 'unread']" />
          </template>
        </el-table-column>
        <el-table-column label="通知">
          <template #default="{ row }">
            <div class="notif-content">
              <div class="notif-title">{{ row.title }}</div>
              <div class="notif-body">{{ row.content }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="180">
          <template #default="{ row }">
            <span class="text-secondary text-sm">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <span :class="['status-pill', row.isRead ? 's-read' : 's-unread']">
              {{ row.isRead ? '已读' : '未读' }}
            </span>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer">
        <span class="total-text">共 {{ total }} 条记录</span>
        <el-pagination v-model:current-page="pageNum" :page-size="10" :total="total" layout="prev, pager, next" @change="loadList" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotificationList, getUnreadCount, markAsRead, markAllAsRead } from '@/api/notification'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const filterIsRead = ref()
const unreadCount = ref(0)

const tabs = [
  { label: '全部', value: undefined },
  { label: '未读', value: 0 },
  { label: '已读', value: 1 },
]

async function loadUnread() {
  try {
    const res: any = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch {}
}

async function loadList() {
  loading.value = true
  try {
    const res: any = await getNotificationList({ pageNum: pageNum.value, pageSize: 10, isRead: filterIsRead.value })
    list.value = res.data?.rows || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function switchTab(val: any) {
  filterIsRead.value = val
  pageNum.value = 1
  loadList()
}

async function handleRowClick(row: any) {
  if (!row.isRead) {
    await markAsRead(row.id)
    row.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
  if (row.relatedType === 'APPLICATION') {
    router.push('/asset/application')
  }
}

async function handleMarkAllRead() {
  await markAllAsRead()
  unreadCount.value = 0
  loadList()
}

onMounted(() => {
  loadList()
  loadUnread()
})
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; }
.page-title { font-size: 22px; font-weight: 800; color: #0f172a; letter-spacing: -0.3px; }
.page-desc { font-size: 13px; color: #94a3b8; margin-top: 4px; }

.filter-bar { display: flex; align-items: center; }
.filter-tabs { display: flex; gap: 2px; background: white; padding: 4px; border-radius: 10px; border: 1px solid #f0f1f6; box-shadow: 0 1px 3px rgba(0,0,0,0.04); }
.tab-btn {
  padding: 7px 16px; border-radius: 7px; border: none; background: transparent;
  font-size: 13px; font-weight: 500; color: #64748b; cursor: pointer; transition: all 0.15s;
}
.tab-btn:hover { color: #1e293b; background: #f8f9fc; }
.tab-btn.active { background: #6366f1; color: white; font-weight: 600; box-shadow: 0 2px 8px rgba(99,102,241,0.3); }

.table-panel { background: white; border-radius: 12px; border: 1px solid #f0f1f6; box-shadow: 0 1px 3px rgba(0,0,0,0.04); overflow: hidden; }
.table-footer { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; border-top: 1px solid #f8f9fc; }
.total-text { font-size: 12px; color: #94a3b8; }

.dot { width: 8px; height: 8px; border-radius: 50%; background: transparent; display: inline-block; }
.dot.unread { background: #ef4444; }
.notif-content { display: flex; flex-direction: column; gap: 4px; }
.notif-title { font-size: 13px; font-weight: 600; color: #1e293b; }
.notif-body { font-size: 12px; color: #64748b; }
.text-secondary { color: #64748b; }
.text-sm { font-size: 13px; }
.status-pill { display: inline-flex; align-items: center; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.s-unread { background: #fef2f2; color: #dc2626; }
.s-read { background: #f1f5f9; color: #64748b; }
</style>
