<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">报修申请</h1>
        <p class="page-desc">选择资产并描述故障情况，提交后将通知维修人员处理</p>
      </div>
    </div>

    <div class="form-layout">
      <!-- 左侧表单 -->
      <div class="form-main">
        <div class="form-section">
          <div class="section-label">选择报修资产</div>
          <div v-if="!selectedAsset" class="asset-picker">
            <button class="pick-btn scan" @click="showScanner = true">
              <div class="pick-icon" style="background:#eef2ff">
                <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
                  <rect x="2" y="2" width="7" height="7" rx="1.5" stroke="#6366f1" stroke-width="1.5"/>
                  <rect x="13" y="2" width="7" height="7" rx="1.5" stroke="#6366f1" stroke-width="1.5"/>
                  <rect x="2" y="13" width="7" height="7" rx="1.5" stroke="#6366f1" stroke-width="1.5"/>
                  <path d="M13 15h2m2 0h2M16 13v2m0 2v2" stroke="#6366f1" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
              </div>
              <div>
                <div class="pick-title">扫描二维码</div>
                <div class="pick-sub">使用摄像头扫描资产标签</div>
              </div>
            </button>
            <button class="pick-btn search" @click="showSearch = true">
              <div class="pick-icon" style="background:#f0fdf4">
                <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
                  <circle cx="10" cy="10" r="7" stroke="#10b981" stroke-width="1.5"/>
                  <path d="M15 15l4 4" stroke="#10b981" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
              </div>
              <div>
                <div class="pick-title">搜索资产</div>
                <div class="pick-sub">按名称或编码查找资产</div>
              </div>
            </button>
          </div>

          <div v-else class="selected-asset">
            <div class="sa-avatar" :style="{ background: nameColor(selectedAsset.assetName) }">
              {{ selectedAsset.assetName.charAt(0) }}
            </div>
            <div class="sa-info">
              <div class="sa-name">{{ selectedAsset.assetName }}</div>
              <div class="sa-code mono">{{ selectedAsset.assetCode }}</div>
              <div v-if="selectedAsset.location" class="sa-loc">
                <svg width="12" height="12" viewBox="0 0 12 12" fill="none"><path d="M6 1a3.5 3.5 0 00-3.5 3.5C2.5 7.5 6 11 6 11s3.5-3.5 3.5-6.5A3.5 3.5 0 006 1z" stroke="#94a3b8" stroke-width="1.2"/><circle cx="6" cy="4.5" r="1" fill="#94a3b8"/></svg>
                {{ selectedAsset.location }}
              </div>
            </div>
            <button class="sa-change" @click="selectedAsset = null; form.assetId = null">更换</button>
          </div>
        </div>

        <div class="form-section">
          <div class="section-label">故障描述 <span class="required">*</span></div>
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            placeholder="请详细描述故障现象、发生时间、对使用的影响等..."
            resize="none"
          />
          <div class="char-count">{{ form.description.length }} 字</div>
        </div>

        <div class="form-actions">
          <el-button type="primary" size="large" :loading="submitting" :disabled="!form.assetId" @click="handleSubmit" style="min-width:120px">
            提交报修
          </el-button>
          <el-button size="large" @click="resetForm">重置</el-button>
        </div>
      </div>

      <!-- 右侧提示 -->
      <div class="form-aside">
        <div class="tips-card">
          <div class="tips-title">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><circle cx="8" cy="8" r="7" stroke="#f59e0b" stroke-width="1.5"/><path d="M8 5v4M8 10.5v.5" stroke="#f59e0b" stroke-width="1.5" stroke-linecap="round"/></svg>
            报修流程说明
          </div>
          <div class="tips-steps">
            <div v-for="(s, i) in steps" :key="i" class="tip-step">
              <div class="step-num">{{ i + 1 }}</div>
              <div class="step-text">{{ s }}</div>
            </div>
          </div>
        </div>
        <div v-if="selectedAsset" class="asset-status-card">
          <div class="asc-title">资产当前状态</div>
          <span :class="['status-pill', `st-${selectedAsset.status}`]">{{ statusLabel(selectedAsset.status) }}</span>
          <div v-if="selectedAsset.status === 'REPAIR'" class="asc-warn">该资产已在维修中，提交后将创建新工单</div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showScanner" title="扫码识别资产" width="400px" align-center>
      <QrScanner v-if="showScanner" @scan="onScan" />
    </el-dialog>

    <el-dialog v-model="showSearch" title="搜索资产" width="600px" align-center>
      <div class="search-wrap">
        <el-input v-model="searchKeyword" placeholder="输入资产名称或编码..." clearable :prefix-icon="Search" @keyup.enter="searchAsset" />
        <el-button type="primary" @click="searchAsset">搜索</el-button>
      </div>
      <el-table :data="searchResults" class="mt-3" @row-click="selectAsset" row-class-name="cursor-pointer">
        <el-table-column prop="assetCode" label="资产编码" width="140">
          <template #default="{ row }"><span class="mono">{{ row.assetCode }}</span></template>
        </el-table-column>
        <el-table-column prop="assetName" label="资产名称" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <span :class="['status-pill', `st-${row.status}`]">{{ statusLabel(row.status) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!searchResults.length && searchKeyword" class="search-empty">未找到匹配的资产</div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { getAssetList, getAssetByCode } from '@/api/asset'
import { applyRepair } from '@/api/repair'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import QrScanner from '@/components/QrScanner.vue'

const submitting = ref(false)
const showScanner = ref(false)
const showSearch = ref(false)
const searchKeyword = ref('')
const searchResults = ref<any[]>([])
const selectedAsset = ref<any>(null)
const form = reactive({ assetId: null as number | null, description: '', images: '' })

const steps = ['选择需要报修的资产（扫码或搜索）', '填写故障详情，越详细越有助于维修', '提交后系统通知维修人员处理', '维修完成后资产状态自动更新']

const colors = ['#6366f1','#10b981','#f59e0b','#ef4444','#0ea5e9','#8b5cf6']
function nameColor(name: string) { return colors[(name?.charCodeAt(0) || 0) % colors.length] }
function statusLabel(s: string) { return { IDLE: '闲置', IN_USE: '在用', REPAIR: '维修中', SCRAPPED: '已报废' }[s] || s }

async function onScan(code: string) {
  showScanner.value = false
  try {
    const assetCode = code.startsWith('ASSET:') ? code.replace('ASSET:', '') : code
    const res: any = await getAssetByCode(assetCode)
    selectAsset(res.data)
    ElMessage.success('资产识别成功')
  } catch { ElMessage.error('未找到对应资产') }
}

async function searchAsset() {
  if (!searchKeyword.value.trim()) return
  const res: any = await getAssetList({ keyword: searchKeyword.value, pageNum: 1, pageSize: 10 })
  searchResults.value = res.data?.rows || []
}

function selectAsset(asset: any) {
  selectedAsset.value = asset
  form.assetId = asset.id
  showSearch.value = false
}

async function handleSubmit() {
  if (!form.assetId) return ElMessage.warning('请先选择报修资产')
  if (!form.description.trim()) return ElMessage.warning('请填写故障描述')
  submitting.value = true
  try {
    await applyRepair(form)
    ElMessage.success('报修申请已提交，维修人员将尽快处理')
    resetForm()
  } finally { submitting.value = false }
}

function resetForm() {
  selectedAsset.value = null
  form.assetId = null
  form.description = ''
}
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; }
.page-title { font-size: 22px; font-weight: 800; color: #0f172a; letter-spacing: -0.3px; }
.page-desc { font-size: 13px; color: #94a3b8; margin-top: 4px; }

.form-layout { display: grid; grid-template-columns: 1fr; gap: 20px; }
@media (min-width: 1024px) { .form-layout { grid-template-columns: 1fr 280px; } }

.form-main { display: flex; flex-direction: column; gap: 16px; }

.form-section {
  background: white; border-radius: 14px; border: 1px solid #f0f1f6;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04); padding: 20px;
}
.section-label { font-size: 13px; font-weight: 700; color: #374151; margin-bottom: 14px; }
.required { color: #ef4444; }

.asset-picker { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.pick-btn {
  display: flex; align-items: center; gap: 12px; padding: 16px;
  border-radius: 10px; border: 1.5px dashed #e5e7ef;
  background: #fafbfc; cursor: pointer; transition: all 0.15s; text-align: left;
}
.pick-btn:hover { border-color: #6366f1; background: #fafbff; }
.pick-icon { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.pick-title { font-size: 13px; font-weight: 700; color: #1e293b; }
.pick-sub { font-size: 12px; color: #94a3b8; margin-top: 2px; }

.selected-asset {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 16px; background: #f8f9ff;
  border-radius: 10px; border: 1px solid #c7d2fe;
}
.sa-avatar { width: 40px; height: 40px; border-radius: 10px; color: white; font-size: 16px; font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.sa-info { flex: 1; min-width: 0; }
.sa-name { font-size: 14px; font-weight: 700; color: #1e293b; }
.sa-code { font-size: 11px; color: #94a3b8; margin-top: 2px; }
.sa-loc { display: flex; align-items: center; gap: 4px; font-size: 11px; color: #94a3b8; margin-top: 3px; }
.sa-change { padding: 5px 12px; border-radius: 6px; border: 1px solid #e5e7ef; background: white; color: #64748b; font-size: 12px; cursor: pointer; white-space: nowrap; }
.sa-change:hover { border-color: #6366f1; color: #6366f1; }

.char-count { font-size: 11px; color: #94a3b8; text-align: right; margin-top: 6px; }

.form-actions { display: flex; gap: 10px; }

/* 右侧 */
.form-aside { display: flex; flex-direction: column; gap: 14px; }

.tips-card { background: white; border-radius: 14px; border: 1px solid #f0f1f6; padding: 18px; box-shadow: 0 1px 3px rgba(0,0,0,0.04); }
.tips-title { display: flex; align-items: center; gap: 7px; font-size: 13px; font-weight: 700; color: #374151; margin-bottom: 16px; }
.tips-steps { display: flex; flex-direction: column; gap: 12px; }
.tip-step { display: flex; align-items: flex-start; gap: 10px; }
.step-num { width: 20px; height: 20px; border-radius: 50%; background: #fef3c7; color: #d97706; font-size: 11px; font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; margin-top: 1px; }
.step-text { font-size: 12px; color: #64748b; line-height: 1.5; }

.asset-status-card { background: white; border-radius: 14px; border: 1px solid #f0f1f6; padding: 16px; box-shadow: 0 1px 3px rgba(0,0,0,0.04); }
.asc-title { font-size: 12px; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.06em; margin-bottom: 10px; }
.asc-warn { font-size: 12px; color: #d97706; margin-top: 10px; padding: 8px 10px; background: #fffbeb; border-radius: 6px; }

.status-pill { display: inline-flex; align-items: center; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.st-IDLE { background: #ecfdf5; color: #059669; }
.st-IN_USE { background: #eef2ff; color: #4f46e5; }
.st-REPAIR { background: #fffbeb; color: #d97706; }
.st-SCRAPPED { background: #f1f5f9; color: #64748b; }

.mono { font-family: ui-monospace, monospace; font-size: 12px; }
.search-wrap { display: flex; gap: 8px; }
.search-empty { text-align: center; padding: 24px; color: #94a3b8; font-size: 13px; }
.mt-3 { margin-top: 12px; }
.cursor-pointer { cursor: pointer; }
</style>
