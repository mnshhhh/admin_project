<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">我的申请</h1>
        <p class="page-desc">查看借用申请进度，管理归还操作</p>
      </div>
    </div>

    <div class="filter-bar">
      <div class="filter-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          :class="['tab-btn', filterStatus === tab.value && 'active']"
          @click="switchTab(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>

    <div class="table-panel">
      <el-table :data="list" v-loading="loading">
        <el-table-column label="资产信息" min-width="200">
          <template #default="{ row }">
            <div class="asset-cell">
              <div class="asset-dot" />
              <div>
                <div class="asset-name">{{ row.assetName }}</div>
                <div class="asset-code mono">{{ row.assetCode }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="申请单号" width="190">
          <template #default="{ row }">
            <span class="mono text-muted">{{ row.borrowNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="purpose" label="用途" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="text-secondary">{{ row.purpose || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="归还时间" width="160">
          <template #default="{ row }">
            <span class="text-secondary text-sm">{{ row.expectedReturnTime || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <span :class="['status-pill', `s-${row.status}`]">{{ statusLabel(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <button v-if="row.status === 'BORROWED'" class="return-btn" @click="handleReturn(row.id)">
              归还
            </button>
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
import { ref, onMounted } from 'vue'
import { getMyBorrows } from '@/api/audit'
import { returnAsset } from '@/api/asset'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const filterStatus = ref('')

const tabs = [
  { label: '全部', value: '' },
  { label: '待审批', value: 'PENDING' },
  { label: '借用中', value: 'BORROWED' },
  { label: '已归还', value: 'RETURNED' },
  { label: '已驳回', value: 'REJECTED' },
]

function statusLabel(s: string) {
  return { PENDING: '待审批', APPROVED: '已批准', REJECTED: '已驳回', BORROWED: '借用中', RETURNED: '已归还' }[s] || s
}

function switchTab(val: string) {
  filterStatus.value = val
  pageNum.value = 1
  loadList()
}

async function loadList() {
  loading.value = true
  try {
    const res: any = await getMyBorrows({ pageNum: pageNum.value, pageSize: 10, status: filterStatus.value })
    list.value = res.data?.rows || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function handleReturn(borrowId: number) {
  await returnAsset(borrowId)
  ElMessage.success('归还成功')
  loadList()
}

onMounted(loadList)
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

.asset-cell { display: flex; align-items: center; gap: 10px; }
.asset-dot { width: 8px; height: 8px; border-radius: 50%; background: #6366f1; flex-shrink: 0; }
.asset-name { font-size: 13px; font-weight: 600; color: #1e293b; }
.asset-code { font-size: 11px; color: #94a3b8; margin-top: 2px; }
.mono { font-family: ui-monospace, monospace; font-size: 12px; }
.text-muted { color: #94a3b8; }
.text-secondary { color: #64748b; }
.text-sm { font-size: 13px; }

.status-pill { display: inline-flex; align-items: center; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.s-PENDING { background: #fffbeb; color: #d97706; }
.s-BORROWED { background: #eef2ff; color: #4f46e5; }
.s-RETURNED { background: #f1f5f9; color: #64748b; }
.s-REJECTED { background: #fef2f2; color: #dc2626; }
.s-APPROVED { background: #ecfdf5; color: #059669; }

.return-btn {
  padding: 5px 14px; border-radius: 7px; border: 1px solid #e5e7ef;
  background: white; color: #374151; font-size: 12px; font-weight: 600;
  cursor: pointer; transition: all 0.15s;
}
.return-btn:hover { background: #fef3c7; border-color: #fbbf24; color: #d97706; }
</style>
