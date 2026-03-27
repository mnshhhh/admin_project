<template>
  <div class="flex flex-col items-center gap-4">
    <div v-if="!scannerStarted" class="text-center space-y-3">
      <div class="w-24 h-24 bg-gray-100 rounded-xl flex items-center justify-center mx-auto">
        <el-icon class="text-4xl text-gray-400"><Camera /></el-icon>
      </div>
      <p class="text-sm text-gray-600">点击下方按钮启动摄像头扫码</p>
      <el-button type="primary" :icon="Camera" @click="startScan">启动摄像头</el-button>
    </div>

    <div v-show="scannerStarted" class="w-full">
      <div id="qr-scanner-container" class="rounded-lg overflow-hidden" style="width: 100%; max-width: 320px; margin: 0 auto;" />
      <div class="flex justify-center mt-3">
        <el-button @click="stopScan">停止扫码</el-button>
      </div>
    </div>

    <el-divider>或手动输入</el-divider>
    <div class="flex gap-2 w-full">
      <el-input v-model="manualCode" placeholder="输入资产编码" clearable @keyup.enter="submitManual" />
      <el-button type="primary" @click="submitManual">确认</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { Camera } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const emit = defineEmits<{ scan: [code: string] }>()

const scannerStarted = ref(false)
const manualCode = ref('')
let html5QrCode: any = null

async function startScan() {
  try {
    const { Html5Qrcode } = await import('html5-qrcode')
    html5QrCode = new Html5Qrcode('qr-scanner-container')
    await html5QrCode.start(
      { facingMode: 'environment' },
      { fps: 10, qrbox: { width: 250, height: 250 } },
      (decodedText: string) => {
        stopScan()
        emit('scan', decodedText)
      },
      undefined
    )
    scannerStarted.value = true
  } catch (err: any) {
    ElMessage.error('无法启动摄像头，请检查权限或使用HTTPS环境')
    console.error(err)
  }
}

async function stopScan() {
  if (html5QrCode && scannerStarted.value) {
    await html5QrCode.stop().catch(() => {})
    scannerStarted.value = false
  }
}

function submitManual() {
  if (!manualCode.value.trim()) return
  emit('scan', manualCode.value.trim())
  manualCode.value = ''
}

onUnmounted(() => stopScan())
</script>
