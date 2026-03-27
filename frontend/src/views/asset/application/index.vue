<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">资产申请</h1>
        <p class="page-desc">提交资产采购申请，查看申请进度</p>
      </div>
      <button class="btn-primary" @click="showDialog = true">
        <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M7 2v10M2 7h10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
        新建申请
      </button>
    </div>

    <div class="filter-bar">
      <div class="filter-tabs">
        <button v-for="tab in tabs" :key="tab.value" :class="['tab-btn', filterStatus === tab.value && 'active']" @click="switchTab(tab.value)">
          {{ tab.label }}
        </button>
      </div>
    </div>

    <div class="table-panel">
      <el-table :data="list" v-loading="loading">
        <el-table-column label="申请单号" width="190">
          <template #default="{ row }">
            <span class="mono text-muted">{{ row.applyNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="资产名称" min-width="160">
          <template #default="{ row }">
            <div>
              <div class="asset-name">{{ row.assetName }}</div>
              <div class="asset-spec" v-if="row.specification">{{ row.specification }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="预估金额" width="120">
          <template #default="{ row }">
            <span v-if="row.estimatedAmount">¥{{ row.estimatedAmount }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="申请理由" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="text-secondary">{{ row.reason || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <span :class="['status-pill', `s-${row.status}`]">{{ statusLabel(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">
            <span class="text-secondary text-sm">{{ row.createTime }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer">
        <span class="total-text">共 {{ total }} 条记录</span>
        <el-pagination v-model:current-page="pageNum" :page-size="10" :total="total" layout="prev, pager, next" @change="loadList" />
      </div>
    </div>

    <el-dialog v-model="showDialog" title="新建资产申请" width="520px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="资产名称" prop="assetName">
          <el-input v-model="form.assetName" placeholder="请输入需要采购的资产名称" />
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="form.specification" placeholder="如：ThinkPad E14 / Dell P2422H" />
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" :max="999" />
        </el-form-item>
        <el-form-item label="预估金额">
          <el-input-number v-model="form.estimatedAmount" :min="0" :precision="2" :step="100" placeholder="元" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="申请理由">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请说明申请原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { applyAsset, getMyApplications } from '@/api/assetApplication'
import { ElMessage, type FormInstance } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const filterStatus = ref('')
const showDialog = ref(false)
const formRef = ref<FormInstance>()

const tabs = [
  { label: '全部', value: '' },
  { label: '待审批', value: 'PENDING' },
  { label: '已批准', value: 'APPROVED' },
  { label: '采购中', value: 'PROCUREMENT' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已驳回', value: 'REJECTED' },
]

const form = reactive({
  assetName: '',
  specification: '',
  quantity: 1,
  estimatedAmount: undefined as number | undefined,
  reason: '',
})

const rules = {
  assetName: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
}

function statusLabel(s: string) {
  return { PENDING: '待审批', APPROVED: '已批准', REJECTED: '已驳回', PROCUREMENT: '采购中', COMPLETED: '已完成' }[s] || s
}

function switchTab(val: string) {
  filterStatus.value = val
  pageNum.value = 1
  loadList()
}

async function loadList() {
  loading.value = true
  try {
    const res: any = await getMyApplications({ pageNum: pageNum.value, pageSize: 10, status: filterStatus.value })
    list.value = res.data?.rows || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    await applyAsset({ ...form })
    ElMessage.success('申请已提交')
    showDialog.value = false
    Object.assign(form, { assetName: '', specification: '', quantity: 1, estimatedAmount: undefined, reason: '' })
    loadList()
  } finally { submitting.value = false }
}

onMounted(loadList)
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; }
.page-title { font-size: 22px; font-weight: 800; color: #0f172a; letter-spacing: -0.3px; }
.page-desc { font-size: 13px; color: #94a3b8; margin-top: 4px; }

.btn-primary {
  display: flex; align-items: center; gap: 6px;
  padding: 9px 20px; border-radius: 10px; border: none;
  background: #6366f1; color: white;
  font-size: 13px; font-weight: 600;
  cursor: pointer; transition: all 0.15s;
  box-shadow: 0 2px 8px rgba(99,102,241,0.3);
}
.btn-primary:hover { background: #4f46e5; transform: translateY(-1px); }

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

.asset-name { font-size: 13px; font-weight: 600; color: #1e293b; }
.asset-spec { font-size: 11px; color: #94a3b8; margin-top: 2px; }
.mono { font-family: ui-monospace, monospace; font-size: 12px; }
.text-muted { color: #94a3b8; }
.text-secondary { color: #64748b; }
.text-sm { font-size: 13px; }

.status-pill { display: inline-flex; align-items: center; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.s-PENDING { background: #fffbeb; color: #d97706; }
.s-APPROVED { background: #ecfdf5; color: #059669; }
.s-REJECTED { background: #fef2f2; color: #dc2626; }
.s-PROCUREMENT { background: #eef2ff; color: #4f46e5; }
.s-COMPLETED { background: #f1f5f9; color: #64748b; }
</style>
