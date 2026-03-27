<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">资产盘点</h1>
        <p class="page-desc">创建盘点任务，通过扫码对比账面与实物</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="showCreate = true">新建盘点任务</el-button>
    </div>

    <div class="check-layout">
      <!-- 左侧任务列表 -->
      <div class="task-list-panel">
        <div class="panel-hd">
          <span>盘点任务</span>
          <span class="task-count">{{ tasks.length }}</span>
        </div>
        <div class="task-list">
          <div
            v-for="task in tasks"
            :key="task.id"
            :class="['task-item', activeTask?.id === task.id && 'active']"
            @click="selectTask(task)"
          >
            <div class="task-item-top">
              <span class="task-name">{{ task.taskName }}</span>
              <span :class="['task-status', `ts-${task.status}`]">{{ taskStatusLabel(task.status) }}</span>
            </div>
            <div class="task-no mono">{{ task.taskNo }}</div>
          </div>
          <div v-if="!tasks.length" class="task-empty">
            <svg width="32" height="32" viewBox="0 0 32 32" fill="none"><rect x="4" y="4" width="24" height="24" rx="4" stroke="#e2e8f0" stroke-width="2"/><path d="M10 16h12M16 10v12" stroke="#cbd5e1" stroke-width="2" stroke-linecap="round"/></svg>
            <p>暂无盘点任务</p>
          </div>
        </div>
      </div>

      <!-- 右侧详情 -->
      <div class="task-detail-panel">
        <template v-if="activeTask">
          <div class="detail-hd">
            <div>
              <div class="detail-title">{{ activeTask.taskName }}</div>
              <div class="detail-no mono">{{ activeTask.taskNo }}</div>
            </div>
            <div class="detail-actions">
              <el-button
                v-if="activeTask.status !== 'COMPLETED'"
                type="primary" :icon="Camera"
                @click="showScanner = true"
              >
                扫码盘点
              </el-button>
              <el-button
                v-if="activeTask.status !== 'COMPLETED'"
                type="success"
                @click="handleComplete"
              >
                完成盘点
              </el-button>
            </div>
          </div>

          <!-- 汇总统计 -->
          <div class="summary-bar">
            <div class="sum-item">
              <span class="sum-num">{{ details.length }}</span>
              <span class="sum-label">账面总数</span>
            </div>
            <div class="sum-divider" />
            <div class="sum-item">
              <span class="sum-num ok">{{ details.filter(d => d.checkResult === 'NORMAL').length }}</span>
              <span class="sum-label">正常</span>
            </div>
            <div class="sum-divider" />
            <div class="sum-item">
              <span class="sum-num warn">{{ details.filter(d => d.checkResult === 'SURPLUS').length }}</span>
              <span class="sum-label">盘盈</span>
            </div>
            <div class="sum-divider" />
            <div class="sum-item">
              <span class="sum-num danger">{{ details.filter(d => d.checkResult === 'DEFICIT').length }}</span>
              <span class="sum-label">盘亏</span>
            </div>
            <div class="sum-divider" />
            <div class="sum-item">
              <span class="sum-num muted">{{ details.filter(d => !d.checkResult).length }}</span>
              <span class="sum-label">未盘点</span>
            </div>
          </div>

          <!-- 明细表 -->
          <el-table :data="details" class="detail-table">
            <el-table-column label="资产编码" width="160">
              <template #default="{ row }"><span class="mono">{{ row.assetCode }}</span></template>
            </el-table-column>
            <el-table-column prop="assetName" label="资产名称" />
            <el-table-column label="盘点结果" width="110">
              <template #default="{ row }">
                <span v-if="row.checkResult" :class="['result-pill', `r-${row.checkResult}`]">
                  {{ checkResultLabel(row.checkResult) }}
                </span>
                <span v-else class="result-pending">待盘点</span>
              </template>
            </el-table-column>
            <el-table-column prop="checkTime" label="盘点时间" width="160">
              <template #default="{ row }">
                <span class="time-text">{{ row.checkTime || '—' }}</span>
              </template>
            </el-table-column>
          </el-table>
        </template>

        <div v-else class="detail-empty">
          <svg width="56" height="56" viewBox="0 0 56 56" fill="none">
            <circle cx="28" cy="28" r="26" stroke="#f0f1f6" stroke-width="2"/>
            <path d="M20 28h16M28 20v16" stroke="#d1d5db" stroke-width="2.5" stroke-linecap="round"/>
          </svg>
          <h3>选择盘点任务</h3>
          <p>从左侧选择一个任务开始盘点操作</p>
        </div>
      </div>
    </div>

    <el-dialog v-model="showCreate" title="新建盘点任务" width="480px" align-center>
      <div class="create-form">
        <div class="cf-item">
          <label>任务名称 <span class="req">*</span></label>
          <el-input v-model="createForm.taskName" placeholder="如：2024年度年底全校盘点" />
        </div>
        <div class="cf-item">
          <label>盘点部门</label>
          <el-select v-model="createForm.deptId" placeholder="不选则全校盘点" clearable style="width:100%">
            <el-option v-for="d in flatDepts" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
          </el-select>
          <span class="cf-hint">选择特定部门或留空进行全校盘点</span>
        </div>
        <div class="cf-item">
          <label>备注</label>
          <el-input v-model="createForm.remark" type="textarea" :rows="2" placeholder="选填" />
        </div>
      </div>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">创建任务</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showScanner" title="扫码盘点" width="400px" align-center>
      <QrScanner v-if="showScanner" @scan="onScan" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getCheckTasks, createCheckTask, scanCheck, completeCheckTask, getCheckDetails } from '@/api/check'
import { getDeptTree } from '@/api/system'
import { ElMessage } from 'element-plus'
import { Plus, Camera } from '@element-plus/icons-vue'
import QrScanner from '@/components/QrScanner.vue'

const tasks = ref<any[]>([])
const activeTask = ref<any>(null)
const details = ref<any[]>([])
const showCreate = ref(false)
const showScanner = ref(false)
const flatDepts = ref<any[]>([])
const createForm = reactive({ taskName: '', deptId: null as number | null, remark: '' })

function taskStatusLabel(s: string) { return { DRAFT: '草稿', IN_PROGRESS: '进行中', COMPLETED: '已完成' }[s] || s }
function checkResultLabel(s: string) { return { NORMAL: '正常', SURPLUS: '盘盈', DEFICIT: '盘亏' }[s] || s }

async function loadTasks() {
  const res: any = await getCheckTasks()
  tasks.value = res.data || []
}

async function selectTask(task: any) {
  activeTask.value = task
  const res: any = await getCheckDetails(task.id)
  details.value = res.data || []
}

async function handleCreate() {
  if (!createForm.taskName.trim()) return ElMessage.warning('请输入任务名称')
  await createCheckTask(createForm)
  ElMessage.success('盘点任务已创建')
  showCreate.value = false
  await loadTasks()
}

async function onScan(code: string) {
  showScanner.value = false
  if (!activeTask.value) return
  const assetCode = code.startsWith('ASSET:') ? code.replace('ASSET:', '') : code
  await scanCheck(activeTask.value.id, assetCode)
  ElMessage.success(`已盘点: ${assetCode}`)
  await selectTask(activeTask.value)
}

async function handleComplete() {
  await completeCheckTask(activeTask.value.id)
  ElMessage.success('盘点任务已完成，已标记未盘点资产为盘亏')
  await loadTasks()
  await selectTask({ ...activeTask.value, status: 'COMPLETED' })
}

function flattenDepts(depts: any[], result: any[] = []) {
  for (const d of depts) { result.push(d); if (d.children) flattenDepts(d.children, result) }
  return result
}

onMounted(async () => {
  loadTasks()
  try { const res: any = await getDeptTree(); flatDepts.value = flattenDepts(res.data || []) } catch {}
})
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; }
.page-title { font-size: 22px; font-weight: 800; color: #0f172a; letter-spacing: -0.3px; }
.page-desc { font-size: 13px; color: #94a3b8; margin-top: 4px; }

.check-layout { display: grid; grid-template-columns: 260px 1fr; gap: 16px; min-height: 500px; }
@media (max-width: 768px) { .check-layout { grid-template-columns: 1fr; } }

.task-list-panel {
  background: white; border-radius: 14px; border: 1px solid #f0f1f6;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04); overflow: hidden; display: flex; flex-direction: column;
}
.panel-hd {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 16px; border-bottom: 1px solid #f8f9fc;
  font-size: 13px; font-weight: 700; color: #374151;
}
.task-count { background: #f1f5f9; color: #64748b; font-size: 12px; padding: 2px 8px; border-radius: 20px; }

.task-list { flex: 1; overflow-y: auto; padding: 6px 0; }
.task-item {
  padding: 12px 16px; cursor: pointer; transition: background 0.1s;
  border-left: 3px solid transparent;
}
.task-item:hover { background: #f8f9fc; }
.task-item.active { background: #eef2ff; border-left-color: #6366f1; }

.task-item-top { display: flex; justify-content: space-between; align-items: flex-start; gap: 8px; margin-bottom: 4px; }
.task-name { font-size: 13px; font-weight: 600; color: #1e293b; flex: 1; }
.task-no { font-size: 11px; color: #94a3b8; }

.task-status { font-size: 11px; font-weight: 600; padding: 2px 8px; border-radius: 10px; white-space: nowrap; }
.ts-DRAFT { background: #f1f5f9; color: #64748b; }
.ts-IN_PROGRESS { background: #fffbeb; color: #d97706; }
.ts-COMPLETED { background: #ecfdf5; color: #059669; }

.task-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 10px; padding: 40px; color: #94a3b8; font-size: 13px; }

.task-detail-panel {
  background: white; border-radius: 14px; border: 1px solid #f0f1f6;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04); overflow: hidden; display: flex; flex-direction: column;
}

.detail-hd {
  display: flex; justify-content: space-between; align-items: flex-start;
  padding: 16px 20px; border-bottom: 1px solid #f8f9fc; flex-shrink: 0;
}
.detail-title { font-size: 16px; font-weight: 700; color: #1e293b; margin-bottom: 4px; }
.detail-no { font-size: 12px; color: #94a3b8; }
.detail-actions { display: flex; gap: 8px; }

.summary-bar {
  display: flex; align-items: center; padding: 14px 20px;
  border-bottom: 1px solid #f8f9fc; background: #fafbfc; gap: 0; flex-shrink: 0;
}
.sum-item { display: flex; flex-direction: column; align-items: center; gap: 3px; flex: 1; }
.sum-num { font-size: 24px; font-weight: 800; color: #1e293b; line-height: 1; font-variant-numeric: tabular-nums; }
.sum-num.ok { color: #10b981; }
.sum-num.warn { color: #f59e0b; }
.sum-num.danger { color: #ef4444; }
.sum-num.muted { color: #94a3b8; }
.sum-label { font-size: 11px; color: #94a3b8; font-weight: 500; }
.sum-divider { width: 1px; height: 32px; background: #f0f1f6; flex-shrink: 0; }

.detail-table { flex: 1; }

.result-pill { display: inline-flex; padding: 2px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.r-NORMAL { background: #ecfdf5; color: #059669; }
.r-SURPLUS { background: #fffbeb; color: #d97706; }
.r-DEFICIT { background: #fef2f2; color: #dc2626; }
.result-pending { font-size: 12px; color: #d1d5db; }
.time-text { font-size: 12px; color: #94a3b8; }
.mono { font-family: ui-monospace, monospace; font-size: 12px; }

.detail-empty {
  flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 14px; color: #94a3b8;
}
.detail-empty h3 { font-size: 16px; font-weight: 700; color: #1e293b; margin: 0; }
.detail-empty p { font-size: 13px; margin: 0; }

.create-form { display: flex; flex-direction: column; gap: 16px; }
.cf-item { display: flex; flex-direction: column; gap: 7px; }
.cf-item label { font-size: 13px; font-weight: 600; color: #374151; }
.cf-hint { font-size: 12px; color: #94a3b8; }
.req { color: #ef4444; }
</style>
