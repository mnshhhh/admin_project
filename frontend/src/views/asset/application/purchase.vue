<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">采购管理</h1>
        <p class="page-desc">管理采购单，向外部合作方下发采购任务</p>
      </div>
      <button class="btn-primary" @click="showCreateDialog = true">
        <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M7 2v10M2 7h10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
        生成采购单
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
        <el-table-column label="采购单号" width="190">
          <template #default="{ row }">
            <span class="mono text-muted">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="关联申请" width="190">
          <template #default="{ row }">
            <span class="mono text-muted">{{ row.applyNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="资产名称" prop="assetName" min-width="140" />
        <el-table-column label="外部合作方" min-width="140">
          <template #default="{ row }">
            <span class="text-secondary">{{ row.externalPartner || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="采购金额" width="120">
          <template #default="{ row }">
            <span v-if="row.totalAmount">¥{{ row.totalAmount }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <span :class="['status-pill', `s-${row.status}`]">{{ poStatusLabel(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <button v-if="row.status === 'PENDING'" class="action-btn" @click="openEditDialog(row)">下单</button>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer">
        <span class="total-text">共 {{ total }} 条记录</span>
        <el-pagination v-model:current-page="pageNum" :page-size="10" :total="total" layout="prev, pager, next" @change="loadList" />
      </div>
    </div>

    <el-dialog v-model="showCreateDialog" title="生成采购单" width="520px" :close-on-click-modal="false">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="申请单ID" prop="applicationId">
          <el-input-number v-model="createForm.applicationId" :min="1" placeholder="已审批通过的申请单ID" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="外部合作方">
          <el-input v-model="createForm.externalPartner" placeholder="供应商/合作方名称" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="createForm.contactInfo" placeholder="联系电话或邮箱" />
        </el-form-item>
        <el-form-item label="采购金额">
          <el-input-number v-model="createForm.totalAmount" :min="0" :precision="2" :step="100" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="预计到货日期">
          <el-date-picker v-model="createForm.expectedDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="submitting">生成</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showEditDialog" title="确认下单" width="520px" :close-on-click-modal="false">
      <el-form :model="editForm" ref="editFormRef" label-width="100px">
        <el-form-item label="外部合作方">
          <el-input v-model="editForm.externalPartner" placeholder="供应商/合作方名称" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="editForm.contactInfo" placeholder="联系电话或邮箱" />
        </el-form-item>
        <el-form-item label="采购金额">
          <el-input-number v-model="editForm.totalAmount" :min="0" :precision="2" :step="100" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="预计到货日期">
          <el-date-picker v-model="editForm.expectedDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate" :loading="submitting">确认下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getPurchaseOrders, createPurchaseOrder, updatePurchaseOrder } from '@/api/assetApplication'
import { ElMessage, type FormInstance } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const filterStatus = ref('')
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const createFormRef = ref<FormInstance>()
const editFormRef = ref<FormInstance>()

const tabs = [
  { label: '全部', value: '' },
  { label: '待采购', value: 'PENDING' },
  { label: '已下单', value: 'ORDERED' },
  { label: '已到货', value: 'DELIVERED' },
  { label: '已完成', value: 'COMPLETED' },
]

const createForm = reactive({
  applicationId: undefined as number | undefined,
  externalPartner: '',
  contactInfo: '',
  totalAmount: undefined as number | undefined,
  expectedDate: '',
  remark: '',
})

const createRules = {
  applicationId: [{ required: true, message: '请输入申请单ID', trigger: 'blur' }],
}

const editForm = reactive({
  id: undefined as number | undefined,
  applicationId: undefined as number | undefined,
  externalPartner: '',
  contactInfo: '',
  totalAmount: undefined as number | undefined,
  expectedDate: '',
  remark: '',
})

function poStatusLabel(s: string) {
  return { PENDING: '待采购', ORDERED: '已下单', DELIVERED: '已到货', COMPLETED: '已完成' }[s] || s
}

function switchTab(val: string) {
  filterStatus.value = val
  pageNum.value = 1
  loadList()
}

async function loadList() {
  loading.value = true
  try {
    const res: any = await getPurchaseOrders({ pageNum: pageNum.value, pageSize: 10, status: filterStatus.value })
    list.value = res.data?.rows || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function openEditDialog(row: any) {
  editForm.id = row.id
  editForm.applicationId = row.applicationId
  editForm.externalPartner = row.externalPartner || ''
  editForm.contactInfo = row.contactInfo || ''
  editForm.totalAmount = row.totalAmount
  editForm.expectedDate = row.expectedDate || ''
  editForm.remark = row.remark || ''
  showEditDialog.value = true
}

async function handleCreate() {
  await createFormRef.value?.validate()
  submitting.value = true
  try {
    await createPurchaseOrder({ ...createForm })
    ElMessage.success('采购单已生成')
    showCreateDialog.value = false
    Object.assign(createForm, { applicationId: undefined, externalPartner: '', contactInfo: '', totalAmount: undefined, expectedDate: '', remark: '' })
    loadList()
  } finally { submitting.value = false }
}

async function handleUpdate() {
  submitting.value = true
  try {
    await updatePurchaseOrder({ ...editForm })
    ElMessage.success('已确认下单')
    showEditDialog.value = false
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

.mono { font-family: ui-monospace, monospace; font-size: 12px; }
.text-muted { color: #94a3b8; }
.text-secondary { color: #64748b; }

.status-pill { display: inline-flex; align-items: center; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.s-PENDING { background: #fffbeb; color: #d97706; }
.s-ORDERED { background: #eef2ff; color: #4f46e5; }
.s-DELIVERED { background: #ecfdf5; color: #059669; }
.s-COMPLETED { background: #f1f5f9; color: #64748b; }

.action-btn {
  padding: 5px 14px; border-radius: 7px; border: 1px solid #e5e7ef;
  background: white; color: #374151; font-size: 12px; font-weight: 600;
  cursor: pointer; transition: all 0.15s;
}
.action-btn:hover { background: #eef2ff; border-color: #6366f1; color: #4f46e5; }
</style>
