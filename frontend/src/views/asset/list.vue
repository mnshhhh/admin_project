<template>
  <div class="page">
    <!-- 页头 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">资产管理</h1>
        <p class="page-desc">管理全校固定资产，支持查询、借用、报修</p>
      </div>
      <div class="page-actions">
        <el-button v-permission="['asset:scan']" plain :icon="Camera" @click="showScanner = true">扫码</el-button>
        <el-button v-permission="['asset:add']" type="primary" :icon="Plus" @click="openForm(null)">新增资产</el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索名称、编码、品牌..."
        clearable
        class="search-input"
        :prefix-icon="Search"
        @keyup.enter="loadList"
      />
      <el-select v-model="query.status" placeholder="全部状态" clearable class="filter-select">
        <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value">
          <span class="status-opt">
            <i class="opt-dot" :style="{ background: s.color }"/>{{ s.label }}
          </span>
        </el-option>
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadList">搜索</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      <div class="view-toggle">
        <button :class="['toggle-btn', viewMode === 'table' && 'active']" @click="viewMode = 'table'">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <rect x="1" y="1" width="12" height="3" rx="1" fill="currentColor"/>
            <rect x="1" y="6" width="12" height="3" rx="1" fill="currentColor"/>
            <rect x="1" y="11" width="12" height="2" rx="1" fill="currentColor"/>
          </svg>
        </button>
        <button :class="['toggle-btn', viewMode === 'card' && 'active']" @click="viewMode = 'card'">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <rect x="1" y="1" width="5" height="5" rx="1.5" fill="currentColor"/>
            <rect x="8" y="1" width="5" height="5" rx="1.5" fill="currentColor"/>
            <rect x="1" y="8" width="5" height="5" rx="1.5" fill="currentColor"/>
            <rect x="8" y="8" width="5" height="5" rx="1.5" fill="currentColor"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- 表格视图 -->
    <div v-if="viewMode === 'table'" class="table-panel">
      <el-table
        :data="list"
        v-loading="loading"
        row-class-name="table-row"
        @row-click="(row: any) => viewDetail(row.id)"
      >
        <el-table-column prop="assetCode" label="资产编码" width="160">
          <template #default="{ row }">
            <span class="mono-text">{{ row.assetCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="assetName" label="资产名称" min-width="140">
          <template #default="{ row }">
            <div class="asset-name-cell">
              <div class="asset-avatar" :style="{ background: nameColor(row.assetName) }">
                {{ row.assetName.charAt(0) }}
              </div>
              <div>
                <div class="asset-name">{{ row.assetName }}</div>
                <div class="asset-brand">{{ row.brand }} {{ row.model }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="110" />
        <el-table-column prop="deptName" label="归属部门" width="130" />
        <el-table-column prop="location" label="存放位置" width="130" show-overflow-tooltip />
        <el-table-column prop="purchasePrice" label="购置金额" width="110">
          <template #default="{ row }">
            <span v-if="row.purchasePrice" class="price-text num">¥{{ row.purchasePrice.toLocaleString() }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <span :class="['status-badge', `status-${row.status}`]">{{ statusLabel(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" @click.stop>
          <template #default="{ row }">
            <div class="row-actions" @click.stop>
              <button class="action-btn blue" @click.stop="viewDetail(row.id)" title="查看详情">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                  <path d="M7 3C4 3 1.5 7 1.5 7S4 11 7 11s5.5-4 5.5-4S10 3 7 3z" stroke="currentColor" stroke-width="1.3"/>
                  <circle cx="7" cy="7" r="1.5" fill="currentColor"/>
                </svg>
              </button>
              <button class="action-btn purple" @click.stop="showQr(row)" title="显示二维码">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                  <rect x="1.5" y="1.5" width="4" height="4" rx="0.5" stroke="currentColor" stroke-width="1.2"/>
                  <rect x="8.5" y="1.5" width="4" height="4" rx="0.5" stroke="currentColor" stroke-width="1.2"/>
                  <rect x="1.5" y="8.5" width="4" height="4" rx="0.5" stroke="currentColor" stroke-width="1.2"/>
                  <rect x="8.5" y="8.5" width="2" height="2" fill="currentColor"/>
                  <rect x="11" y="8.5" width="1.5" height="1.5" fill="currentColor"/>
                  <rect x="8.5" y="11" width="1.5" height="1.5" fill="currentColor"/>
                </svg>
              </button>
              <button v-permission="['asset:edit']" class="action-btn blue" @click.stop="openForm(row)" title="编辑">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                  <path d="M9 2.5l2.5 2.5-6.5 6.5H2.5V9L9 2.5z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/>
                </svg>
              </button>
              <button v-permission="['asset:remove']" class="action-btn red" @click.stop="handleDelete(row.id)" title="删除">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                  <path d="M2 4h10M5 4V2.5h4V4M5.5 6.5v4M8.5 6.5v4M3 4l.5 7.5h7L11 4" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
                </svg>
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer">
        <span class="total-text">共 {{ total }} 条记录</span>
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="prev, pager, next"
          @change="loadList"
        />
      </div>
    </div>

    <!-- 卡片视图 -->
    <div v-else class="card-grid">
      <div
        v-for="item in list"
        :key="item.id"
        class="asset-card"
        @click="viewDetail(item.id)"
      >
        <div class="card-top">
          <div class="card-avatar" :style="{ background: nameColor(item.assetName) }">
            {{ item.assetName.charAt(0) }}
          </div>
          <span :class="['status-badge', `status-${item.status}`]">{{ statusLabel(item.status) }}</span>
        </div>
        <div class="card-name">{{ item.assetName }}</div>
        <div class="card-code mono-text">{{ item.assetCode }}</div>
        <div class="card-meta">
          <span>{{ item.deptName }}</span>
          <span v-if="item.location">· {{ item.location }}</span>
        </div>
        <div v-if="item.purchasePrice" class="card-price num">¥{{ item.purchasePrice.toLocaleString() }}</div>
      </div>
      <div v-if="!loading && !list.length" class="card-empty">
        <p>暂无数据</p>
      </div>
      <div class="card-footer">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total" layout="prev, pager, next" @change="loadList" />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="formVisible" :title="formData.id ? '编辑资产' : '新增资产'" width="580px" align-center>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px" class="asset-form">
        <div class="form-row">
          <el-form-item label="资产名称" prop="assetName" class="form-full">
            <el-input v-model="formData.assetName" placeholder="请输入资产名称" />
          </el-form-item>
        </div>
        <div class="form-row two-col">
          <el-form-item label="品牌">
            <el-input v-model="formData.brand" placeholder="品牌" />
          </el-form-item>
          <el-form-item label="型号">
            <el-input v-model="formData.model" placeholder="型号" />
          </el-form-item>
        </div>
        <div class="form-row two-col">
          <el-form-item label="序列号">
            <el-input v-model="formData.serialNo" placeholder="序列号" />
          </el-form-item>
          <el-form-item label="购置日期">
            <el-date-picker v-model="formData.purchaseDate" type="date" value-format="YYYY-MM-DD" class="w-full" />
          </el-form-item>
        </div>
        <div class="form-row two-col">
          <el-form-item label="购置金额">
            <el-input-number v-model="formData.purchasePrice" :min="0" :precision="2" style="width:100%" />
          </el-form-item>
          <el-form-item label="折旧年限">
            <el-input-number v-model="formData.depreciationYears" :min="1" :max="50" style="width:100%" />
          </el-form-item>
        </div>
        <div class="form-row two-col">
          <el-form-item label="归属部门" prop="deptId">
            <el-select v-model="formData.deptId" placeholder="请选择部门" style="width:100%">
              <el-option v-for="d in flatDepts" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
            </el-select>
          </el-form-item>
          <el-form-item label="存放位置">
            <el-input v-model="formData.location" placeholder="存放位置" />
          </el-form-item>
        </div>
        <el-form-item label="备注" class="form-full">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存资产</el-button>
      </template>
    </el-dialog>

    <!-- 扫码弹窗 -->
    <el-dialog v-model="showScanner" title="扫码识别" width="400px" align-center>
      <QrScanner v-if="showScanner" @scan="onScan" />
    </el-dialog>

    <!-- 资产二维码弹窗 -->
    <el-dialog v-model="qrVisible" :title="qrRow?.assetCode + ' 二维码'" width="340px" align-center>
      <div v-if="qrRow" class="qr-dialog-body">
        <img :src="`/qr/${qrRow.assetCode}.png`" :alt="qrRow.assetName" class="qr-image" />
        <div class="qr-info">
          <div class="qi-name">{{ qrRow.assetName }}</div>
          <div class="qi-code">{{ qrRow.assetCode }}</div>
          <span :class="['qi-status', `s-${qrRow.status}`]">{{ statusLabel(qrRow.status) }}</span>
        </div>
      </div>
    </el-dialog>

    <!-- 借用弹窗 -->
    <el-dialog v-model="borrowVisible" title="发起借用申请" width="460px" align-center>
      <div class="borrow-asset-info">
        <div class="ba-avatar" :style="{ background: nameColor(borrowTarget?.assetName || '') }">
          {{ borrowTarget?.assetName?.charAt(0) }}
        </div>
        <div>
          <div class="ba-name">{{ borrowTarget?.assetName }}</div>
          <div class="ba-code mono-text">{{ borrowTarget?.assetCode }}</div>
        </div>
      </div>
      <el-form :model="borrowForm" label-width="110px" class="mt-4">
        <el-form-item label="借用用途">
          <el-input v-model="borrowForm.purpose" type="textarea" :rows="2" placeholder="说明借用目的" />
        </el-form-item>
        <el-form-item label="预计归还时间">
          <el-date-picker v-model="borrowForm.expectedReturnTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="borrowVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBorrow">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAssetList, addAsset, updateAsset, deleteAsset, borrowAsset } from '@/api/asset'
import { getDeptTree } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Camera } from '@element-plus/icons-vue'
import QrScanner from '@/components/QrScanner.vue'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const formVisible = ref(false)
const showScanner = ref(false)
const qrVisible = ref(false)
const qrRow = ref<any>(null)
const borrowVisible = ref(false)
const borrowTarget = ref<any>(null)
const flatDepts = ref<any[]>([])
const formRef = ref()
const viewMode = ref<'table' | 'card'>('table')

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: '' })
const formData = reactive<any>({ id: null, assetName: '', brand: '', model: '', serialNo: '', purchaseDate: null, purchasePrice: null, depreciationYears: null, location: '', deptId: null, remark: '' })
const borrowForm = reactive({ purpose: '', expectedReturnTime: '' })

const statusOptions = [
  { label: '闲置', value: 'IDLE', color: '#10b981' },
  { label: '在用', value: 'IN_USE', color: '#6366f1' },
  { label: '维修中', value: 'REPAIR', color: '#f59e0b' },
  { label: '已报废', value: 'SCRAPPED', color: '#94a3b8' }
]
const formRules = {
  assetName: [{ required: true, message: '资产名称不能为空' }],
  deptId: [{ required: true, message: '请选择归属部门' }]
}

function showQr(row: any) {
  qrRow.value = row
  qrVisible.value = true
}

function statusLabel(s: string) {
  return { IDLE: '闲置', IN_USE: '在用', REPAIR: '维修中', SCRAPPED: '已报废' }[s] || s
}

const nameColors = ['#6366f1','#10b981','#f59e0b','#ef4444','#0ea5e9','#8b5cf6','#ec4899']
function nameColor(name: string) {
  return nameColors[(name?.charCodeAt(0) || 0) % nameColors.length]
}

async function loadList() {
  loading.value = true
  try {
    const res: any = await getAssetList(query)
    list.value = res.data?.rows || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function resetQuery() { query.keyword = ''; query.status = ''; query.pageNum = 1; loadList() }
function viewDetail(id: number) { router.push(`/asset/detail/${id}`) }

function openForm(row: any) {
  Object.assign(formData, { id: null, assetName: '', brand: '', model: '', serialNo: '', purchaseDate: null, purchasePrice: null, depreciationYears: null, location: '', deptId: null, remark: '' })
  if (row) Object.assign(formData, row)
  formVisible.value = true
}

async function handleSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (formData.id) await updateAsset(formData)
    else await addAsset(formData)
    ElMessage.success('保存成功')
    formVisible.value = false
    loadList()
  } finally { saving.value = false }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定要删除这条资产记录吗？此操作不可撤销。', '删除确认', { type: 'warning', confirmButtonText: '确认删除', confirmButtonClass: 'el-button--danger' })
  await deleteAsset(id)
  ElMessage.success('已删除')
  loadList()
}

async function onScan(code: string) {
  showScanner.value = false
  const assetCode = code.startsWith('ASSET:') ? code.replace('ASSET:', '') : code
  const res: any = await getAssetList({ keyword: assetCode, pageNum: 1, pageSize: 1 })
  const asset = res.data?.rows?.[0]
  if (asset) { borrowTarget.value = asset; borrowVisible.value = true }
  else ElMessage.warning('未找到该资产')
}

async function submitBorrow() {
  await borrowAsset({ assetId: borrowTarget.value?.id, ...borrowForm })
  ElMessage.success('借用申请已提交，等待审批')
  borrowVisible.value = false
}

function flattenDepts(depts: any[], result: any[] = []) {
  for (const d of depts) { result.push(d); if (d.children) flattenDepts(d.children, result) }
  return result
}

onMounted(async () => {
  loadList()
  try { const res: any = await getDeptTree(); flatDepts.value = flattenDepts(res.data || []) } catch {}
})
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }

/* 页头 */
.page-header { display: flex; justify-content: space-between; align-items: flex-start; }
.page-title { font-size: 22px; font-weight: 800; color: #0f172a; letter-spacing: -0.3px; }
.page-desc { font-size: 13px; color: #94a3b8; margin-top: 4px; }
.page-actions { display: flex; gap: 10px; }

/* 搜索栏 */
.search-bar {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  background: white;
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid #f0f1f6;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.search-input { width: 260px; }
.filter-select { width: 130px; }

.status-opt { display: flex; align-items: center; gap: 6px; }
.opt-dot { width: 7px; height: 7px; border-radius: 50%; flex-shrink: 0; }

.view-toggle {
  margin-left: auto;
  display: flex;
  gap: 2px;
  background: #f1f5f9;
  padding: 3px;
  border-radius: 8px;
}
.toggle-btn {
  padding: 5px 8px;
  border-radius: 6px;
  border: none;
  background: transparent;
  color: #94a3b8;
  cursor: pointer;
  display: flex;
  transition: all 0.15s;
}
.toggle-btn.active { background: white; color: #374151; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }

/* 表格 */
.table-panel {
  background: white;
  border-radius: 12px;
  border: 1px solid #f0f1f6;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  overflow: hidden;
}
.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-top: 1px solid #f8f9fc;
}
.total-text { font-size: 12px; color: #94a3b8; }

:deep(.table-row) { cursor: pointer; }
:deep(.el-table__row:hover td) { background: #fafbff !important; }

.mono-text { font-family: ui-monospace, monospace; font-size: 12px; color: #64748b; }

.asset-name-cell { display: flex; align-items: center; gap: 10px; }
.asset-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  color: white;
  font-size: 13px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.asset-name { font-size: 13px; font-weight: 600; color: #1e293b; }
.asset-brand { font-size: 11px; color: #94a3b8; margin-top: 1px; }

.price-text { font-size: 13px; font-weight: 600; color: #1e293b; }
.text-muted { color: #d1d5db; }

/* 状态标签 */
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}
.status-IDLE { background: #ecfdf5; color: #059669; }
.status-IN_USE { background: #eef2ff; color: #4f46e5; }
.status-REPAIR { background: #fffbeb; color: #d97706; }
.status-SCRAPPED { background: #f1f5f9; color: #64748b; }

/* 行操作按钮 */
.row-actions { display: flex; gap: 4px; }
.action-btn {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  border: none;
  background: #f1f5f9;
  color: #64748b;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}
.action-btn:hover { background: #e2e8f0; color: #374151; }
.action-btn.blue:hover { background: #eef2ff; color: #4f46e5; }
.action-btn.purple:hover { background: #f5f3ff; color: #a855f7; }

.qr-dialog-body { display: flex; flex-direction: column; align-items: center; gap: 16px; padding: 8px 0; }
.qr-image { width: 280px; height: 340px; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.1); }
.qr-info { display: flex; flex-direction: column; align-items: center; gap: 6px; }
.qi-name { font-size: 16px; font-weight: 700; color: #1e293b; }
.qi-code { font-size: 12px; color: #94a3b8; font-family: ui-monospace, monospace; }
.qi-status { display: inline-flex; padding: 3px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.qi-status.s-IDLE { background: #ecfdf5; color: #059669; }
.qi-status.s-IN_USE { background: #eef2ff; color: #4f46e5; }
.qi-status.s-REPAIR { background: #fffbeb; color: #d97706; }
.qi-status.s-SCRAPPED { background: #f1f5f9; color: #64748b; }
.action-btn.red:hover { background: #fef2f2; color: #ef4444; }

/* 卡片视图 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 14px;
}
.asset-card {
  background: white;
  border-radius: 14px;
  padding: 18px;
  border: 1px solid #f0f1f6;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  cursor: pointer;
  transition: all 0.2s;
}
.asset-card:hover { border-color: #c7d2fe; box-shadow: 0 4px 16px rgba(99,102,241,0.1); transform: translateY(-2px); }

.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.card-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  color: white;
  font-size: 16px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.card-name { font-size: 14px; font-weight: 700; color: #1e293b; margin-bottom: 4px; }
.card-code { font-size: 11px; color: #94a3b8; margin-bottom: 8px; }
.card-meta { font-size: 12px; color: #64748b; margin-bottom: 8px; }
.card-price { font-size: 16px; font-weight: 700; color: #4f46e5; }
.card-empty { grid-column: 1/-1; text-align: center; padding: 60px; color: #94a3b8; }
.card-footer { grid-column: 1/-1; display: flex; justify-content: flex-end; }

/* 借用信息 */
.borrow-asset-info {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: #f8f9fc;
  border-radius: 10px;
  margin-bottom: 4px;
}
.ba-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  color: white;
  font-size: 16px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.ba-name { font-size: 14px; font-weight: 700; color: #1e293b; }
.ba-code { font-size: 12px; color: #94a3b8; margin-top: 2px; }

/* 表单双列 */
.asset-form .form-row { margin-bottom: 0; }
.asset-form .two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 0 16px; }
</style>
