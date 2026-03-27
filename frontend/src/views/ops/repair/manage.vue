<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">维修工单池</h1>
        <p class="page-desc">接受并处理设备故障维修工单</p>
      </div>
      <div class="header-stats">
        <div v-for="s in statusSummary" :key="s.label" class="stat-pill" :style="{ background: s.bg, color: s.color }">
          <span class="stat-num">{{ s.count }}</span>
          <span>{{ s.label }}</span>
        </div>
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

    <div class="repair-cards" v-loading="loading">
      <div v-for="item in list" :key="item.id" class="repair-card">
        <div class="rc-header">
          <div class="rc-no mono">{{ item.repairNo }}</div>
          <span :class="['status-pill', `rs-${item.status}`]">{{ repairStatusLabel(item.status) }}</span>
        </div>
        <div class="rc-asset">
          <div class="rc-asset-name">{{ item.assetName }}</div>
          <div class="rc-asset-code mono">{{ item.assetCode }}</div>
        </div>
        <div class="rc-desc">{{ item.description || '未填写故障描述' }}</div>
        <div class="rc-footer">
          <div class="rc-time">
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none"><circle cx="6" cy="6" r="5" stroke="#94a3b8" stroke-width="1.2"/><path d="M6 3.5V6l2 1.5" stroke="#94a3b8" stroke-width="1.2" stroke-linecap="round"/></svg>
            {{ item.createTime }}
          </div>
          <div class="rc-actions">
            <button v-if="item.status === 'PENDING'" class="action-primary" @click="handleAccept(item.id)">接单处理</button>
            <button v-if="item.status === 'IN_PROGRESS'" class="action-success" @click="openComplete(item)">完成维修</button>
          </div>
        </div>
      </div>
      <div v-if="!loading && !list.length" class="empty-card">
        <svg width="40" height="40" viewBox="0 0 40 40" fill="none"><circle cx="20" cy="20" r="18" stroke="#e2e8f0" stroke-width="2"/><path d="M14 20h12M20 14v12" stroke="#cbd5e1" stroke-width="2" stroke-linecap="round"/></svg>
        <p>暂无工单</p>
      </div>
    </div>

    <div class="table-footer">
      <span class="total-text">共 {{ total }} 条工单</span>
      <el-pagination v-model:current-page="pageNum" :page-size="10" :total="total" layout="prev, pager, next" @change="loadList" />
    </div>

    <el-dialog v-model="completeVisible" title="完成维修记录" width="420px" align-center>
      <div class="complete-form">
        <div class="cf-item">
          <label>维修费用（元）</label>
          <el-input-number v-model="completeForm.cost" :min="0" :precision="2" style="width:100%" />
        </div>
        <div class="cf-item">
          <label>维修工时（小时）</label>
          <el-input-number v-model="completeForm.hours" :min="0" :precision="1" style="width:100%" />
        </div>
        <div class="cf-item">
          <label>维修备注</label>
          <el-input v-model="completeForm.remark" type="textarea" :rows="3" placeholder="描述维修内容、更换零件等..." />
        </div>
      </div>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" @click="handleComplete">确认完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getRepairList, acceptRepair, completeRepair } from '@/api/repair'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const filterStatus = ref('')
const completeVisible = ref(false)
const currentRepairId = ref<number | null>(null)
const completeForm = reactive({ cost: 0, hours: 0, remark: '' })
const allList = ref<any[]>([])

const tabs = [
  { label: '全部', value: '' },
  { label: '待处理', value: 'PENDING' },
  { label: '维修中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'COMPLETED' },
]

const statusSummary = computed(() => [
  { label: '待处理', count: allList.value.filter(i => i.status === 'PENDING').length, bg: '#fffbeb', color: '#d97706' },
  { label: '维修中', count: allList.value.filter(i => i.status === 'IN_PROGRESS').length, bg: '#eef2ff', color: '#4f46e5' },
  { label: '已完成', count: allList.value.filter(i => i.status === 'COMPLETED').length, bg: '#ecfdf5', color: '#059669' },
])

function repairStatusLabel(s: string) {
  return { PENDING: '待处理', IN_PROGRESS: '维修中', COMPLETED: '已完成', CLOSED: '已关闭' }[s] || s
}

function switchTab(val: string) { filterStatus.value = val; pageNum.value = 1; loadList() }

async function loadList() {
  loading.value = true
  try {
    const res: any = await getRepairList({ pageNum: pageNum.value, pageSize: 10, status: filterStatus.value })
    list.value = res.data?.rows || []
    total.value = res.data?.total || 0
    if (!filterStatus.value) allList.value = list.value
  } finally { loading.value = false }
}

async function handleAccept(id: number) {
  await acceptRepair(id)
  ElMessage.success('已接单，请尽快处理')
  loadList()
}

function openComplete(row: any) {
  currentRepairId.value = row.id
  completeForm.cost = 0; completeForm.hours = 0; completeForm.remark = ''
  completeVisible.value = true
}

async function handleComplete() {
  if (!currentRepairId.value) return
  await completeRepair(currentRepairId.value, completeForm)
  ElMessage.success('维修完成，资产已恢复闲置状态')
  completeVisible.value = false
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 12px; }
.page-title { font-size: 22px; font-weight: 800; color: #0f172a; letter-spacing: -0.3px; }
.page-desc { font-size: 13px; color: #94a3b8; margin-top: 4px; }

.header-stats { display: flex; gap: 8px; }
.stat-pill { display: flex; align-items: center; gap: 6px; padding: 6px 14px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.stat-num { font-size: 16px; font-weight: 800; }

.filter-bar { display: flex; }
.filter-tabs { display: flex; gap: 2px; background: white; padding: 4px; border-radius: 10px; border: 1px solid #f0f1f6; box-shadow: 0 1px 3px rgba(0,0,0,0.04); }
.tab-btn { padding: 7px 16px; border-radius: 7px; border: none; background: transparent; font-size: 13px; font-weight: 500; color: #64748b; cursor: pointer; transition: all 0.15s; }
.tab-btn:hover { color: #1e293b; background: #f8f9fc; }
.tab-btn.active { background: #6366f1; color: white; font-weight: 600; box-shadow: 0 2px 8px rgba(99,102,241,0.3); }

.repair-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 14px; }

.repair-card {
  background: white; border-radius: 14px; border: 1px solid #f0f1f6;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04); padding: 18px;
  display: flex; flex-direction: column; gap: 12px;
  transition: all 0.2s;
}
.repair-card:hover { border-color: #c7d2fe; box-shadow: 0 4px 16px rgba(99,102,241,0.08); }

.rc-header { display: flex; justify-content: space-between; align-items: center; }
.rc-no { font-size: 11px; color: #94a3b8; }
.rc-asset-name { font-size: 15px; font-weight: 700; color: #1e293b; }
.rc-asset-code { font-size: 11px; color: #94a3b8; margin-top: 2px; }
.rc-desc { font-size: 13px; color: #64748b; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.rc-footer { display: flex; justify-content: space-between; align-items: center; padding-top: 8px; border-top: 1px solid #f8f9fc; }
.rc-time { display: flex; align-items: center; gap: 5px; font-size: 12px; color: #94a3b8; }
.rc-actions { display: flex; gap: 8px; }

.action-primary { padding: 6px 16px; border-radius: 8px; border: none; background: #eef2ff; color: #4f46e5; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.15s; }
.action-primary:hover { background: #6366f1; color: white; }
.action-success { padding: 6px 16px; border-radius: 8px; border: none; background: #ecfdf5; color: #059669; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.15s; }
.action-success:hover { background: #10b981; color: white; }

.empty-card { grid-column: 1/-1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px; padding: 60px; color: #94a3b8; font-size: 13px; }

.status-pill { display: inline-flex; align-items: center; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.rs-PENDING { background: #fffbeb; color: #d97706; }
.rs-IN_PROGRESS { background: #eef2ff; color: #4f46e5; }
.rs-COMPLETED { background: #ecfdf5; color: #059669; }
.rs-CLOSED { background: #f1f5f9; color: #64748b; }

.table-footer { display: flex; align-items: center; justify-content: space-between; }
.total-text { font-size: 12px; color: #94a3b8; }
.mono { font-family: ui-monospace, monospace; }

.complete-form { display: flex; flex-direction: column; gap: 16px; }
.cf-item { display: flex; flex-direction: column; gap: 6px; }
.cf-item label { font-size: 13px; font-weight: 600; color: #374151; }
</style>
