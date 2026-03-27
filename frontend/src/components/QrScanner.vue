<template>
  <div class="qr-scanner-wrap">
    <div v-if="!hasCamera" class="scanner-no-cam">
      <div class="cam-icon">
        <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
          <circle cx="24" cy="24" r="20" stroke="#cbd5e1" stroke-width="2"/>
          <path d="M20 20l8 0M20 28l8 0M16 16h2M16 32h2M30 16h2M30 32h2" stroke="#cbd5e1" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
      <p>未检测到摄像头，请检查设备权限</p>
      <el-button type="primary" plain @click="startScan">重试</el-button>
    </div>

    <div v-show="hasCamera" class="scanner-main">
      <div class="scanner-viewport">
        <div id="qr-reader" ref="readerEl" class="qr-reader" />
        <div v-if="scanning" class="scanner-overlay">
          <div class="scan-line" />
          <div class="corner tl" /><div class="corner tr" />
          <div class="corner bl" /><div class="corner br" />
        </div>
      </div>
      <div class="scanner-tip">
        <span v-if="scanning">将二维码放入框内扫描</span>
        <span v-else-if="errorMsg" class="error">{{ errorMsg }}</span>
        <span v-else>正在启动摄像头...</span>
      </div>
    </div>

    <el-divider>或手动输入</el-divider>
    <div class="manual-input">
      <el-input
        v-model="manualCode"
        placeholder="输入资产编码"
        clearable
        @keyup.enter="submitManual"
      >
        <template #prefix>
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none" style="margin-top:1px">
            <rect x="1" y="1" width="12" height="12" rx="2" stroke="#94a3b8" stroke-width="1.2"/>
            <path d="M4 5h6M4 7h6M4 9h3" stroke="#94a3b8" stroke-width="1.2" stroke-linecap="round"/>
          </svg>
        </template>
      </el-input>
      <el-button type="primary" :disabled="!manualCode.trim()" @click="submitManual">确认</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

const emit = defineEmits<{ scan: [code: string] }>()

const readerEl = ref<HTMLElement | null>(null)
const manualCode = ref('')
const scanning = ref(false)
const hasCamera = ref(true)
const errorMsg = ref('')

let html5QrCode: any = null

async function startScan() {
  errorMsg.value = ''
  try {
    const { Html5Qrcode } = await import('html5-qrcode')

    if (!readerEl.value) {
      errorMsg.value = '扫码组件未就绪，请稍后重试'
      return
    }

    html5QrCode = new Html5Qrcode('qr-reader')
    scanning.value = true

    await html5QrCode.start(
      { facingMode: 'environment' },
      { fps: 10, qrbox: { width: 220, height: 220 }, aspectRatio: 1 },
      (decodedText: string) => {
        stopScan()
        emit('scan', decodedText)
      },
      () => {}
    )
  } catch (err: any) {
    scanning.value = false
    hasCamera.value = false
    if (err?.message?.includes('permission')) {
      errorMsg.value = '摄像头权限被拒绝，请在浏览器设置中开启摄像头权限'
    } else if (err?.message?.includes('NotFoundError')) {
      errorMsg.value = '未检测到可用摄像头'
    } else {
      errorMsg.value = '摄像头启动失败，请检查设备'
    }
    console.error('QR Scanner error:', err)
  }
}

async function stopScan() {
  scanning.value = false
  if (html5QrCode) {
    try {
      await html5QrCode.stop()
    } catch {}
    html5QrCode = null
  }
}

function submitManual() {
  if (!manualCode.value.trim()) return
  emit('scan', manualCode.value.trim())
  manualCode.value = ''
}

onMounted(() => {
  setTimeout(startScan, 200)
})

onUnmounted(() => stopScan())
</script>

<style scoped>
.qr-scanner-wrap { display: flex; flex-direction: column; gap: 16px; }

.scanner-no-cam {
  display: flex; flex-direction: column; align-items: center; gap: 12px;
  padding: 24px; color: #64748b; font-size: 13px;
}
.cam-icon { opacity: 0.5; }

.scanner-main { display: flex; flex-direction: column; align-items: center; gap: 12px; }

.scanner-viewport {
  position: relative; width: 260px; height: 260px;
  border-radius: 16px; overflow: hidden; background: #0f172a;
}

.qr-reader { width: 100% !important; height: 100% !important; }
.qr-reader :deep(video) { width: 100% !important; height: 100% !important; object-fit: cover; border-radius: 16px; }

.scanner-overlay {
  position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; pointer-events: none;
}
.scan-line {
  position: absolute; width: 60%; height: 2px; background: linear-gradient(90deg, transparent, #6366f1, transparent);
  animation: scanMove 2s linear infinite; top: 50%; transform: translateY(-50%);
  box-shadow: 0 0 8px rgba(99,102,241,0.6);
}
@keyframes scanMove {
  0% { top: 10%; opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { top: 90%; opacity: 0; }
}

.corner { position: absolute; width: 20px; height: 20px; border-color: #6366f1; }
.corner.tl { top: 20px; left: 20px; border-top: 3px solid; border-left: 3px solid; border-radius: 4px 0 0 0; }
.corner.tr { top: 20px; right: 20px; border-top: 3px solid; border-right: 3px solid; border-radius: 0 4px 0 0; }
.corner.bl { bottom: 20px; left: 20px; border-bottom: 3px solid; border-left: 3px solid; border-radius: 0 0 0 4px; }
.corner.br { bottom: 20px; right: 20px; border-bottom: 3px solid; border-right: 3px solid; border-radius: 0 0 4px 0; }

.scanner-tip { font-size: 13px; color: #64748b; text-align: center; min-height: 20px; }
.scanner-tip .error { color: #ef4444; }

.manual-input { display: flex; gap: 8px; }
</style>
