<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">待办审批</h1>
        <p class="page-desc">需要您处理的借用申请单据</p>
      </div>
      <div v-if="list.length" class="badge-urgent">{{ list.length }} 待处理</div>
    </div>

    <div v-if="list.length === 0 && !loading" class="empty-state">
      <div class="empty-icon-wrap">
        <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
          <circle cx="24" cy="24" r="20" stroke="#e2e8f0" stroke-width="2"/>
          <path d="M16 24l5 5 11-11" stroke="#10b981" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <h3>审批全部完成</h3>
      <p>暂时没有待处理的申请，稍后再来查看</p>
    </div>

    <div v-else class="audit-cards" v-loading="loading">
      <div v-for="item in list" :key="item.id" class="audit-card">
        <div class="audit-card-left">
          <div class="audit-asset-icon">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M4 8l6-4 6 4v5l-6 4-6-4V8z" stroke="#6366f1" stroke-width="1.5" stroke-linejoin="round"/>
              <path d="M4 8l6 4 6-4" stroke="#6366f1" stroke-width="1.5" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="audit-info">
            <div class="audit-asset-name">{{ item.assetName }}</div>
            <div class="audit-meta">
              <span class="mono">{{ item.borrowNo }}</span>
              <span class="dot">·</span>
              <span>{{ item.purpose || '未填写用途' }}</span>
            </div>
            <div class="audit-time">
              <svg width="12" height="12" viewBox="0 0 12 12" fill="none"><circle cx="6" cy="6" r="5" stroke="#94a3b8" stroke-width="1.2"/><path d="M6 3.5V6l2 1.5" stroke="#94a3b8" stroke-width="1.2" stroke-linecap="round"/></svg>
              归还：{{ item.expectedReturnTime || '未设置' }}
              <span class="dot">·</span>
              申请：{{ item.createTime }}
            </div>
          </div>
        </div>
        <div class="audit-card-actions">
          <button class="btn-approve" @click="handleAudit(item, 'APPROVE')">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M2.5 7l3.5 3.5 5.5-6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
            通过
          </button>
          <button class="btn-reject" @click="handleAudit(item, 'REJECT')">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M3 3l8 8M11 3l-8 8" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
            驳回
          </button>
        </div>
      </div>
    </div>

    <div v-if="list.length > 0" class="table-footer">
      <span class="total-text">共 {{ total }} 条待审批</span>
      <el-pagination v-model:current-page="pageNum" :page-size="10" :total="total" layout="prev, pager, next" @change="loadList" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPendingAudits, auditAction } from '@/api/audit'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)

async function loadList() {
  loading.value = true
  try {
    const res: any = await getPendingAudits({ pageNum: pageNum.value, pageSize: 10 })
    list.value = res.data?.rows || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function handleAudit(row: any, action: string) {
  let remark = ''
  if (action === 'REJECT') {
    try {
      const { value } = await ElMessageBox.prompt('请输入驳回原因（选填）', '驳回申请', { confirmButtonText: '确认驳回', cancelButtonText: '取消', inputPlaceholder: '说明驳回原因...' })
      remark = value || ''
    } catch { return }
  }
  await auditAction({ bizId: row.id, bizType: 'BORROW', action, remark })
  ElMessage.success(action === 'APPROVE' ? '已通过审批' : '已驳回')
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }

.page-header { display: flex; justify-content: space-between; align-items: flex-start; }
.page-title { font-size: 22px; font-weight: 800; color: #0f172a; letter-spacing: -0.3px; }
.page-desc { font-size: 13px; color: #94a3b8; margin-top: 4px; }

.badge-urgent {
  background: #fef2f2; color: #ef4444;
  font-size: 12px; font-weight: 700;
  padding: 4px 14px; border-radius: 20px;
  border: 1px solid #fecaca;
}

.empty-state {
  background: white;
  border-radius: 16px;
  border: 1px solid #f0f1f6;
  padding: 80px 40px;
  text-align: center;
  display: flex; flex-direction: column; align-items: center; gap: 12px;
}
.empty-icon-wrap { width: 72px; height: 72px; background: #f0fdf4; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.empty-state h3 { font-size: 16px; font-weight: 700; color: #1e293b; }
.empty-state p { font-size: 13px; color: #94a3b8; }

.audit-cards { display: flex; flex-direction: column; gap: 10px; }

.audit-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #f0f1f6;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.15s;
}
.audit-card:hover { border-color: #c7d2fe; box-shadow: 0 4px 12px rgba(99,102,241,0.08); }

.audit-card-left { display: flex; align-items: flex-start; gap: 14px; flex: 1; min-width: 0; }

.audit-asset-icon {
  width: 40px; height: 40px; border-radius: 10px;
  background: #eef2ff;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}

.audit-info { flex: 1; min-width: 0; }
.audit-asset-name { font-size: 15px; font-weight: 700; color: #1e293b; margin-bottom: 5px; }
.audit-meta { font-size: 12px; color: #64748b; display: flex; align-items: center; gap: 6px; margin-bottom: 4px; flex-wrap: wrap; }
.audit-time { font-size: 12px; color: #94a3b8; display: flex; align-items: center; gap: 5px; }
.mono { font-family: ui-monospace, monospace; }
.dot { color: #d1d5db; }

.audit-card-actions { display: flex; gap: 8px; flex-shrink: 0; }

.btn-approve, .btn-reject {
  display: flex; align-items: center; gap: 5px;
  padding: 7px 16px; border-radius: 8px;
  font-size: 13px; font-weight: 600;
  cursor: pointer; border: none; transition: all 0.15s;
}
.btn-approve { background: #ecfdf5; color: #059669; }
.btn-approve:hover { background: #d1fae5; transform: translateY(-1px); }
.btn-reject { background: #fef2f2; color: #dc2626; }
.btn-reject:hover { background: #fee2e2; transform: translateY(-1px); }

.table-footer { display: flex; align-items: center; justify-content: space-between; }
.total-text { font-size: 12px; color: #94a3b8; }
</style>
