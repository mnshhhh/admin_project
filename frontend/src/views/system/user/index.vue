<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">用户管理</h1>
        <p class="page-desc">管理系统用户账号、角色与权限</p>
      </div>
      <el-button v-permission="['sys:user:add']" type="primary" :icon="Plus" @click="openForm(null)">
        新增用户
      </el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索用户名 / 昵称..."
        clearable
        class="search-input"
        :prefix-icon="Search"
        @keyup.enter="loadList"
      />
      <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width:130px">
        <el-option label="正常" value="0" />
        <el-option label="停用" value="1" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadList">搜索</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
    </div>

    <div class="table-panel">
      <el-table :data="list" v-loading="loading" row-class-name="table-row">
        <el-table-column prop="userId" label="ID" width="60" />
        <el-table-column label="用户" min-width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="user-avatar" :style="{ background: avatarColor(row.nickname || row.username) }">
                {{ (row.nickname || row.username).charAt(0) }}
              </div>
              <div>
                <div class="user-name">{{ row.nickname || row.username }}</div>
                <div class="user-login">@{{ row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130">
          <template #default="{ row }">
            <span class="mono-text">{{ row.phone || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" show-overflow-tooltip>
          <template #default="{ row }">{{ row.email || '—' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <span :class="['status-badge', row.status === '0' ? 'status-ok' : 'status-off']">
              {{ row.status === '0' ? '正常' : '停用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <button v-permission="['sys:user:edit']" class="action-btn blue" title="编辑" @click="openForm(row)">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                  <path d="M9 2.5l2.5 2.5-6.5 6.5H2.5V9L9 2.5z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/>
                </svg>
              </button>
              <button v-permission="['sys:user:edit']" class="action-btn" title="重置密码" @click="handleResetPwd(row)">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                  <path d="M7 2a5 5 0 100 10A5 5 0 007 2zM5 7h4M7 5v4" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
                </svg>
              </button>
              <button v-permission="['sys:user:remove']" class="action-btn red" title="删除" @click="handleDelete(row.userId)">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                  <path d="M2 4h10M5 4V2.5h4V4M5.5 6.5v4M8.5 6.5v4M3 4l.5 7.5h7L11 4" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
                </svg>
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <span class="total-text">共 {{ total }} 位用户</span>
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="10"
          :total="total"
          layout="prev, pager, next"
          @change="loadList"
        />
      </div>
    </div>

    <el-dialog v-model="formVisible" :title="form.userId ? '编辑用户' : '新增用户'" width="520px" align-center>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.userId" placeholder="登录账号" />
        </el-form-item>
        <el-form-item v-if="!form.userId" label="初始密码">
          <el-input v-model="form.password" type="password" placeholder="留空则默认 Admin@123" show-password />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="显示名称" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="选填" />
        </el-form-item>
        <el-form-item label="归属部门" prop="deptId">
          <el-select v-model="form.deptId" placeholder="请选择部门" style="width:100%">
            <el-option v-for="d in flatDepts" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width:100%">
            <el-option v-for="r in roles" :key="r.roleId" :label="r.roleName" :value="r.roleId" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保 存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getUserList, addUser, updateUser, deleteUser, resetPassword, getDeptTree, getRoleList } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'

const loading = ref(false)
const saving = ref(false)
const formVisible = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const keyword = ref('')
const filterStatus = ref('')
const flatDepts = ref<any[]>([])
const roles = ref<any[]>([])
const formRef = ref()

const form = reactive<any>({
  userId: null, username: '', password: '', nickname: '',
  phone: '', email: '', deptId: null, roleIds: [], status: '0'
})
const rules = {
  username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择归属部门', trigger: 'change' }]
}

const colors = ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#0ea5e9', '#8b5cf6', '#ec4899']
function avatarColor(name: string) {
  return colors[(name?.charCodeAt(0) || 0) % colors.length]
}

async function loadList() {
  loading.value = true
  try {
    const res: any = await getUserList({ pageNum: pageNum.value, pageSize: 10, keyword: keyword.value, status: filterStatus.value })
    list.value = res.data?.rows || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  keyword.value = ''
  filterStatus.value = ''
  pageNum.value = 1
  loadList()
}

function openForm(row: any) {
  Object.assign(form, { userId: null, username: '', password: '', nickname: '', phone: '', email: '', deptId: null, roleIds: [], status: '0' })
  if (row) Object.assign(form, row)
  formVisible.value = true
}

async function handleSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (form.userId) await updateUser(form)
    else await addUser(form)
    ElMessage.success('保存成功')
    formVisible.value = false
    loadList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(userId: number) {
  await ElMessageBox.confirm('确定要删除该用户吗？', '删除确认', { type: 'warning', confirmButtonText: '确认删除', confirmButtonClass: 'el-button--danger' })
  await deleteUser(userId)
  ElMessage.success('已删除')
  loadList()
}

async function handleResetPwd(row: any) {
  const newPwd = 'Admin@123'
  await ElMessageBox.confirm(`确定将 "${row.nickname || row.username}" 的密码重置为 Admin@123？`, '重置密码', { type: 'warning' })
  await resetPassword(row.userId, newPwd)
  ElMessage.success('密码已重置为 Admin@123')
}

function flattenDepts(depts: any[], result: any[] = []) {
  for (const d of depts) {
    result.push(d)
    if (d.children) flattenDepts(d.children, result)
  }
  return result
}

onMounted(async () => {
  loadList()
  const [deptRes, roleRes]: any[] = await Promise.all([getDeptTree(), getRoleList()])
  flatDepts.value = flattenDepts(deptRes.data || [])
  roles.value = roleRes.data || []
})
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }

.page-header { display: flex; justify-content: space-between; align-items: flex-start; }
.page-title { font-size: 22px; font-weight: 800; color: #0f172a; letter-spacing: -0.3px; }
.page-desc { font-size: 13px; color: #94a3b8; margin-top: 4px; }

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
.search-input { width: 240px; }

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

.user-cell { display: flex; align-items: center; gap: 10px; }
.user-avatar {
  width: 32px; height: 32px; border-radius: 8px;
  color: white; font-size: 13px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.user-name { font-size: 13px; font-weight: 600; color: #1e293b; }
.user-login { font-size: 11px; color: #94a3b8; margin-top: 1px; }

.mono-text { font-family: ui-monospace, monospace; font-size: 12px; }

.status-badge {
  display: inline-flex; align-items: center;
  padding: 3px 10px; border-radius: 20px;
  font-size: 12px; font-weight: 600;
}
.status-ok { background: #ecfdf5; color: #059669; }
.status-off { background: #fef2f2; color: #dc2626; }

.row-actions { display: flex; gap: 4px; }
.action-btn {
  width: 28px; height: 28px; border-radius: 6px; border: none;
  background: #f1f5f9; color: #64748b; cursor: pointer;
  display: flex; align-items: center; justify-content: center; transition: all 0.15s;
}
.action-btn:hover { background: #e2e8f0; color: #374151; }
.action-btn.blue:hover { background: #eef2ff; color: #4f46e5; }
.action-btn.red:hover { background: #fef2f2; color: #ef4444; }
</style>
