<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">资产入库</h1>
        <p class="page-desc">采购到货后回填入库信息，提交审批</p>
      </div>
      <button class="btn-primary" @click="showCreateDialog = true">
        <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M7 2v10M2 7h10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
        新增入库单
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
        <el-table-column label="入库单号" width="190">
          <template #default="{ row }">
            <span class="mono text-muted">{{ row.entryNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="采购单号" width="190">
          <template #default="{ row }">
            <span class="mono text-muted">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="资产名称" prop="assetName" min-width="140" />
        <el-table-column label="数量" prop="quantity" width="80" />
        <el-table-column label="单价" width="100">
          <template #default="{ row }">
            <span v-if="row.unitPrice">¥{{ row.unitPrice }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="总金额" width="120">
          <template #default="{ row }">
            <span v-if="row.totalAmount">¥{{ row.totalAmount }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="入库日期" width="120">
          <template #default="{ row }">
            <span class="text-secondary text-sm">{{ row.entryDate || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <span :class="['status-pill', `s-${row.status}`]">{{ entryStatusLabel(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" v-if="hasApprovePermission">
          <template #default="{ row }">
            <div v-if="row.status === 'PENDING'" class="action-group">
              <button class="btn-approve-sm" @click="handleApprove(row, 'APPROVE')">通过</button>
              <button class="btn-reject-sm" @click="handleApprove(row, 'REJECT')">驳回</button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer">
        <span class="total-text">共 {{ total }} 条记录</span>
        <el-pagination v-model:current-page="pageNum" :page-size="10" :total="total" layout="prev, pager, next" @change="loadList" />
      </div>
    </div>

    <el-dialog v-model="showCreateDialog" title="新增入库单" width="520px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="采购单ID" prop="purchaseOrderId">
          <el-input-number v-model="form.purchaseOrderId" :min="1" placeholder="关联的采购单ID" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="资产名称">
          <el-input v-model="form.assetName" placeholder="入库资产名称" />
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="form.specification" placeholder="规格型号" />
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" :max="999" />
        </el-form-item>
        <el-form-item label="单价">
          <el-input-number v-model="form.unitPrice" :min="0" :precision="2" :step="100" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="总金额">
          <el-input-number v-model="form.totalAmount" :min="0" :precision="2" :step="100" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="入库日期">
          <el-date-picker v-model="form.entryDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getWarehouseEntries, submitWarehouseEntry, approveWarehouseEntry } from '@/api/assetApplication'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

const userStore = useUserStore()
const hasApprovePermission = computed(() => userStore.hasPermission('asset:entry:approve'))

const loading = ref(false)
const submitting = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const filterStatus = ref('')
const showCreateDialog = ref(false)
const formRef = ref<FormInstance>()

const tabs = [
  { label: '全部', value: '' },
  { label: '待审批', value: 'PENDING' },
  { label: '已审批', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' },
]

const form = reactive({
  purchaseOrderId: undefined as number | undefined,
  assetName: '',
  specification: '',
  quantity: 1,
  unitPrice: undefined as number | undefined,
  totalAmount: undefined as number | undefined,
  entryDate: '',
  remark: '',
})

const rules = {
  purchaseOrderId: [{ required: true, message: '请输入采购单ID', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
}

function entryStatusLabel(s: string) {
  return { PENDING: '待审批', APPROVED: '已审批', REJECTED: '已驳回' }[s] || s
}

function switchTab(val: string) {
  filterStatus.value = val
  pageNum.value = 1
  loadList()
}

async function loadList() {
  loading.value = true
  try {
    const res: any = await getWarehouseEntries({ pageNum: pageNum.value, pageSize: 10, status: filterStatus.value })
    list.value = res.data?.rows || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    await submitWarehouseEntry({ ...form })
    ElMessage.success('入库单已提交')
    showCreateDialog.value = false
    Object.assign(form, { purchaseOrderId: undefined, assetName: '', specification: '', quantity: 1, unitPrice: undefined, totalAmount: undefined, entryDate: '', remark: '' })
    loadList()
  } finally { submitting.value = false }
}

async function handleApprove(row: any, action: string) {
  let remark = ''
  if (action === 'REJECT') {
    try {
      const { value } = await ElMessageBox.prompt('请输入驳回原因（选填）', '驳回入库', { confirmButtonText: '确认驳回', cancelButtonText: '取消', inputPlaceholder: '说明驳回原因...' })
      remark = value || ''
    } catch { return }
  }
  await approveWarehouseEntry(row.id, { action, remark })
  ElMessage.success(action === 'APPROVE' ? '入库已审批通过，已通知申请人' : '已驳回')
  loadList()
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

.mono { font-family: ui-monospace, monospace; font-size: 12px; }
.text-muted { color: #94a3b8; }
.text-secondary { color: #64748b; }
.text-sm { font-size: 13px; }

.status-pill { display: inline-flex; align-items: center; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.s-PENDING { background: #fffbeb; color: #d97706; }
.s-APPROVED { background: #ecfdf5; color: #059669; }
.s-REJECTED { background: #fef2f2; color: #dc2626; }

.action-group { display: flex; gap: 6px; }
.btn-approve-sm, .btn-reject-sm {
  padding: 4px 10px; border-radius: 6px; border: none;
  font-size: 12px; font-weight: 600; cursor: pointer; transition: all 0.15s;
}
.btn-approve-sm { background: #ecfdf5; color: #059669; }
.btn-approve-sm:hover { background: #d1fae5; }
.btn-reject-sm { background: #fef2f2; color: #dc2626; }
.btn-reject-sm:hover { background: #fee2e2; }
</style>
