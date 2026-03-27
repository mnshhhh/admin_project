<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">审计日志</h1>
        <p class="page-desc">记录所有用户的敏感操作行为，用于安全审计</p>
      </div>
    </div>

    <div class="search-bar">
      <el-input v-model="filterUsername" placeholder="操作用户..." clearable style="width:180px" @keyup.enter="loadList" />
      <el-input v-model="filterOperation" placeholder="操作描述..." clearable style="width:200px" @keyup.enter="loadList" />
      <el-button type="primary" :icon="Search" @click="loadList">搜索</el-button>
      <el-button @click="resetFilter">重置</el-button>
    </div>

    <div class="table-panel">
      <el-table :data="list" v-loading="loading">
        <el-table-column label="操作用户" width="130">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="user-dot" />
              <span class="user-name">{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="operation" label="操作描述" min-width="140" />
        <el-table-column prop="requestUrl" label="请求路径" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="mono url-text">{{ row.requestUrl }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="requestIp" label="IP 地址" width="130">
          <template #default="{ row }">
            <span class="mono ip-text">{{ row.requestIp || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <span :class="['status-pill', row.status === 0 ? 's-ok' : 's-fail']">
              {{ row.status === 0 ? '成功' : '失败' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="165">
          <template #default="{ row }">
            <span class="time-text">{{ row.createTime }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer">
        <span class="total-text">共 {{ total }} 条日志</span>
        <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="prev, pager, next" @change="loadList" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getLogList } from '@/api/system'
import { Search } from '@element-plus/icons-vue'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(15)
const filterUsername = ref('')
const filterOperation = ref('')

async function loadList() {
  loading.value = true
  try {
    const res: any = await getLogList({ pageNum: pageNum.value, pageSize: pageSize.value, username: filterUsername.value, operation: filterOperation.value })
    list.value = res.data?.rows || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function resetFilter() {
  filterUsername.value = ''
  filterOperation.value = ''
  pageNum.value = 1
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; }
.page-title { font-size: 22px; font-weight: 800; color: #0f172a; letter-spacing: -0.3px; }
.page-desc { font-size: 13px; color: #94a3b8; margin-top: 4px; }

.search-bar { display: flex; gap: 10px; flex-wrap: wrap; align-items: center; background: white; padding: 14px 16px; border-radius: 12px; border: 1px solid #f0f1f6; box-shadow: 0 1px 3px rgba(0,0,0,0.04); }

.table-panel { background: white; border-radius: 12px; border: 1px solid #f0f1f6; box-shadow: 0 1px 3px rgba(0,0,0,0.04); overflow: hidden; }
.table-footer { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; border-top: 1px solid #f8f9fc; }
.total-text { font-size: 12px; color: #94a3b8; }

.user-cell { display: flex; align-items: center; gap: 8px; }
.user-dot { width: 7px; height: 7px; border-radius: 50%; background: #6366f1; flex-shrink: 0; }
.user-name { font-size: 13px; font-weight: 600; color: #1e293b; }

.mono { font-family: ui-monospace, monospace; }
.url-text { font-size: 12px; color: #64748b; }
.ip-text { font-size: 12px; color: #94a3b8; }
.time-text { font-size: 12px; color: #64748b; }

.status-pill { display: inline-flex; align-items: center; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.s-ok { background: #ecfdf5; color: #059669; }
.s-fail { background: #fef2f2; color: #dc2626; }
</style>
