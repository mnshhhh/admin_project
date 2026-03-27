<template>
  <div class="detail-page" v-loading="loading">
    <div class="detail-nav">
      <button class="back-btn" @click="router.back()">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M10 12L6 8l4-4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        返回列表
      </button>
    </div>

    <div v-if="asset" class="detail-grid">
      <!-- 左侧主信息 -->
      <div class="detail-main">
        <!-- 资产标题卡 -->
        <div class="asset-hero">
          <div class="hero-avatar" :style="{ background: nameColor(asset.assetName) }">
            {{ asset.assetName.charAt(0) }}
          </div>
          <div class="hero-info">
            <h1 class="hero-name">{{ asset.assetName }}</h1>
            <div class="hero-meta">
              <span class="hero-code mono-text">{{ asset.assetCode }}</span>
              <span :class="['status-badge', `status-${asset.status}`]">{{ statusLabel(asset.status) }}</span>
            </div>
            <div v-if="asset.brand || asset.model" class="hero-brand">{{ asset.brand }} · {{ asset.model }}</div>
          </div>
          <div class="hero-actions">
            <el-button v-permission="['asset:borrow']" type="primary" :disabled="asset.status !== 'IDLE'" @click="showBorrow = true">
              申请借用
            </el-button>
            <el-button v-permission="['repair:add']" @click="showRepair = true">申请报修</el-button>
            <el-button v-permission="['asset:edit']" plain>编辑</el-button>
          </div>
        </div>

        <!-- 基本信息 -->
        <div class="info-section">
          <div class="section-title">基本信息</div>
          <div class="info-grid">
            <div v-for="item in infoFields" :key="item.label" class="info-item">
              <div class="info-label">{{ item.label }}</div>
              <div class="info-value" :class="item.class">{{ item.value || '—' }}</div>
            </div>
          </div>
        </div>

        <!-- 财务信息 -->
        <div class="info-section">
          <div class="section-title">财务信息</div>
          <div class="finance-grid">
            <div class="finance-card">
              <div class="finance-label">购置金额</div>
              <div class="finance-value num">{{ asset.purchasePrice ? `¥${asset.purchasePrice.toLocaleString()}` : '—' }}</div>
            </div>
            <div class="finance-card">
              <div class="finance-label">当前账面价值</div>
              <div class="finance-value num" style="color:#6366f1">{{ asset.currentValue ? `¥${asset.currentValue.toLocaleString()}` : '—' }}</div>
            </div>
            <div class="finance-card">
              <div class="finance-label">折旧年限</div>
              <div class="finance-value num">{{ asset.depreciationYears ? `${asset.depreciationYears} 年` : '—' }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧二维码 -->
      <div class="detail-side">
        <div class="qr-panel">
          <div class="qr-title">资产二维码</div>
          <div class="qr-wrap">
            <img :src="`/qr/${asset.assetCode}.png`" class="qr-img" alt="二维码" @error="handleQrError" />
            <div v-if="qrError" class="qr-empty">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <rect x="4" y="4" width="18" height="18" rx="3" stroke="#d1d5db" stroke-width="2"/>
                <rect x="10" y="10" width="6" height="6" fill="#d1d5db" rx="1"/>
                <rect x="26" y="4" width="18" height="18" rx="3" stroke="#d1d5db" stroke-width="2"/>
                <rect x="32" y="10" width="6" height="6" fill="#d1d5db" rx="1"/>
                <rect x="4" y="26" width="18" height="18" rx="3" stroke="#d1d5db" stroke-width="2"/>
                <rect x="10" y="32" width="6" height="6" fill="#d1d5db" rx="1"/>
              </svg>
              <p>暂无二维码</p>
            </div>
          </div>
          <p class="qr-code mono-text">{{ asset.assetCode }}</p>
          <el-button size="small" class="download-btn" @click="downloadQr">下载二维码</el-button>
        </div>

        <!-- 备注 -->
        <div v-if="asset.remark" class="remark-panel">
          <div class="section-title">备注</div>
          <p class="remark-text">{{ asset.remark }}</p>
        </div>
      </div>
    </div>

    <!-- 借用弹窗 -->
    <el-dialog v-model="showBorrow" title="发起借用申请" width="460px" align-center>
      <el-form :model="borrowForm" label-width="110px">
        <el-form-item label="借用用途">
          <el-input v-model="borrowForm.purpose" type="textarea" :rows="2" placeholder="说明借用目的" />
        </el-form-item>
        <el-form-item label="预计归还时间">
          <el-date-picker v-model="borrowForm.expectedReturnTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBorrow = false">取消</el-button>
        <el-button type="primary" @click="submitBorrow">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 报修弹窗 -->
    <el-dialog v-model="showRepair" title="提交报修申请" width="460px" align-center>
      <el-form :model="repairForm" label-width="90px">
        <el-form-item label="故障描述">
          <el-input v-model="repairForm.description" type="textarea" :rows="4" placeholder="请详细描述故障现象..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRepair = false">取消</el-button>
        <el-button type="primary" @click="submitRepair">提交报修</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAssetDetail, borrowAsset } from '@/api/asset'
import { applyRepair } from '@/api/repair'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const asset = ref<any>(null)
const showBorrow = ref(false)
const showRepair = ref(false)
const qrError = ref(false)
const borrowForm = reactive({ purpose: '', expectedReturnTime: '' })
const repairForm = reactive({ description: '' })

const nameColors = ['#6366f1','#10b981','#f59e0b','#ef4444','#0ea5e9','#8b5cf6','#ec4899']
function nameColor(name: string) { return nameColors[(name?.charCodeAt(0) || 0) % nameColors.length] }
function statusLabel(s: string) { return { IDLE: '闲置', IN_USE: '在用', REPAIR: '维修中', SCRAPPED: '已报废' }[s] || s }

const infoFields = computed(() => [
  { label: '序列号', value: asset.value?.serialNo, class: 'mono-text' },
  { label: '资产分类', value: asset.value?.categoryName },
  { label: '归属部门', value: asset.value?.deptName },
  { label: '资产责任人', value: asset.value?.managerName },
  { label: '存放位置', value: asset.value?.location },
  { label: '购置日期', value: asset.value?.purchaseDate },
])

async function loadDetail() {
  loading.value = true
  try { const res: any = await getAssetDetail(Number(route.params.id)); asset.value = res.data }
  finally { loading.value = false }
}
async function submitBorrow() {
  await borrowAsset({ assetId: asset.value.id, ...borrowForm })
  ElMessage.success('借用申请已提交，等待审批')
  showBorrow.value = false
}
async function submitRepair() {
  await applyRepair({ assetId: asset.value.id, ...repairForm })
  ElMessage.success('报修申请已提交')
  showRepair.value = false
  await loadDetail()
}
function downloadQr() {
  const link = document.createElement('a')
  link.href = `/qr/${asset.value.assetCode}.png`
  link.download = `${asset.value.assetCode}.png`
  link.click()
}

onMounted(loadDetail)
</script>

<style scoped>
.detail-page { display: flex; flex-direction: column; gap: 20px; }

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 8px;
  border: 1px solid #e5e7ef;
  background: white;
  color: #64748b;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}
.back-btn:hover { background: #f8f9fc; color: #1e293b; }

.detail-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}
@media (min-width: 1024px) { .detail-grid { grid-template-columns: 1fr 280px; } }

/* 主信息 */
.detail-main { display: flex; flex-direction: column; gap: 16px; }

.asset-hero {
  background: white;
  border-radius: 14px;
  border: 1px solid #f0f1f6;
  padding: 24px;
  display: flex;
  gap: 16px;
  align-items: flex-start;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  flex-wrap: wrap;
}
.hero-avatar {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  color: white;
  font-size: 22px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.hero-info { flex: 1; min-width: 0; }
.hero-name { font-size: 20px; font-weight: 800; color: #0f172a; letter-spacing: -0.3px; margin-bottom: 8px; }
.hero-meta { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; flex-wrap: wrap; }
.hero-code { font-size: 12px; }
.hero-brand { font-size: 13px; color: #64748b; }
.hero-actions { display: flex; gap: 8px; flex-wrap: wrap; align-self: center; }

/* info section */
.info-section {
  background: white;
  border-radius: 14px;
  border: 1px solid #f0f1f6;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.section-title { font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em; color: #94a3b8; margin-bottom: 16px; }

.info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
@media (min-width: 640px) { .info-grid { grid-template-columns: repeat(3, 1fr); } }

.info-label { font-size: 12px; color: #94a3b8; margin-bottom: 4px; font-weight: 500; }
.info-value { font-size: 14px; color: #1e293b; font-weight: 500; }

.finance-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.finance-card { background: #f8f9fc; border-radius: 10px; padding: 14px 16px; }
.finance-label { font-size: 12px; color: #94a3b8; margin-bottom: 6px; }
.finance-value { font-size: 20px; font-weight: 800; color: #0f172a; letter-spacing: -0.5px; }

/* 右侧 */
.detail-side { display: flex; flex-direction: column; gap: 16px; }

.qr-panel {
  background: white;
  border-radius: 14px;
  border: 1px solid #f0f1f6;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.qr-title { font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em; color: #94a3b8; align-self: flex-start; }
.qr-wrap { width: 180px; height: 180px; display: flex; align-items: center; justify-content: center; background: #f8f9fc; border-radius: 12px; overflow: hidden; }
.qr-img { width: 100%; height: 100%; object-fit: contain; }
.qr-empty { display: flex; flex-direction: column; align-items: center; gap: 10px; color: #d1d5db; font-size: 12px; }
.qr-code { font-size: 12px; color: #94a3b8; }
.download-btn { border-radius: 8px !important; }

.remark-panel {
  background: white;
  border-radius: 14px;
  border: 1px solid #f0f1f6;
  padding: 16px 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.remark-text { font-size: 13px; color: #64748b; line-height: 1.6; }

/* 状态标签 */
.status-badge { display: inline-flex; align-items: center; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.status-IDLE { background: #ecfdf5; color: #059669; }
.status-IN_USE { background: #eef2ff; color: #4f46e5; }
.status-REPAIR { background: #fffbeb; color: #d97706; }
.status-SCRAPPED { background: #f1f5f9; color: #64748b; }
.mono-text { font-family: ui-monospace, monospace; }
</style>
