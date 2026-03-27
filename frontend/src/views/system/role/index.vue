<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">角色管理</h1>
        <p class="page-desc">配置系统角色、权限菜单与数据访问范围</p>
      </div>
      <el-button v-permission="['sys:role:manage']" type="primary" :icon="Plus" @click="openForm(null)">新增角色</el-button>
    </div>

    <div class="role-grid" v-loading="loading">
      <div v-for="item in list" :key="item.roleId" class="role-card">
        <div class="role-card-top">
          <div class="role-avatar">{{ item.roleName.charAt(0) }}</div>
          <div class="role-badge">{{ dataScopeLabel(item.dataScope) }}</div>
        </div>
        <div class="role-name">{{ item.roleName }}</div>
        <div class="role-key mono">{{ item.roleKey }}</div>
        <div v-if="item.remark" class="role-remark">{{ item.remark }}</div>
        <div class="role-actions">
          <button class="role-btn edit" @click="openForm(item)">
            <svg width="13" height="13" viewBox="0 0 13 13" fill="none"><path d="M8.5 2l2.5 2.5-6.5 6.5H2V8.5L8.5 2z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/></svg>
            编辑权限
          </button>
          <button class="role-btn del" @click="handleDelete(item.roleId)">
            <svg width="13" height="13" viewBox="0 0 13 13" fill="none"><path d="M1.5 3.5h10M4.5 3.5V2h4v1.5M5 5.5v4M8 5.5v4M2.5 3.5l.5 7h7l.5-7" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg>
          </button>
        </div>
      </div>
      <div v-if="!loading && !list.length" class="empty-grid">暂无角色数据</div>
    </div>

    <el-dialog v-model="formVisible" :title="form.roleId ? '编辑角色' : '新增角色'" width="640px" align-center>
      <div class="dialog-form">
        <div class="df-row two">
          <div class="df-item">
            <label>角色名称</label>
            <el-input v-model="form.roleName" placeholder="如：院级管理员" />
          </div>
          <div class="df-item">
            <label>角色标识</label>
            <el-input v-model="form.roleKey" placeholder="如：dept_admin" />
          </div>
        </div>
        <div class="df-item">
          <label>数据范围</label>
          <el-select v-model="form.dataScope" style="width:100%">
            <el-option label="全部数据（校级）" value="1" />
            <el-option label="本部门及下级" value="2" />
            <el-option label="仅本部门" value="3" />
            <el-option label="仅本人" value="4" />
          </el-select>
        </div>
        <div class="df-item">
          <label>权限菜单</label>
          <div class="tree-wrap">
            <el-tree
              ref="treeRef"
              :data="menuTree"
              show-checkbox
              node-key="menuId"
              :props="{ label: 'menuName', children: 'children' }"
              :default-checked-keys="checkedMenuIds"
            />
          </div>
        </div>
        <div class="df-item">
          <label>备注</label>
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填" />
        </div>
      </div>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存角色</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getRoleList, addRole, updateRole, deleteRole, getRoleMenuIds, getMenuTree } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false); const saving = ref(false); const formVisible = ref(false)
const list = ref<any[]>([]); const menuTree = ref<any[]>([]); const checkedMenuIds = ref<number[]>([])
const treeRef = ref()
const form = reactive<any>({ roleId: null, roleName: '', roleKey: '', dataScope: '1', remark: '' })

function dataScopeLabel(s: string) {
  return { '1': '全部数据', '2': '部门及下级', '3': '本部门', '4': '仅本人' }[s] || s
}

async function loadList() {
  loading.value = true
  try { const res: any = await getRoleList(); list.value = res.data || [] }
  finally { loading.value = false }
}

async function openForm(row: any) {
  Object.assign(form, { roleId: null, roleName: '', roleKey: '', dataScope: '1', remark: '' })
  if (row) {
    Object.assign(form, row)
    const res: any = await getRoleMenuIds(row.roleId)
    checkedMenuIds.value = res.data || []
  } else { checkedMenuIds.value = [] }
  formVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    const menuIds = treeRef.value?.getCheckedKeys(false) || []
    const data = { ...form, menuIds }
    if (form.roleId) await updateRole(data)
    else await addRole(data)
    ElMessage.success('保存成功'); formVisible.value = false; loadList()
  } finally { saving.value = false }
}

async function handleDelete(roleId: number) {
  await ElMessageBox.confirm('确定删除该角色？关联用户将失去此角色权限。', '删除确认', { type: 'warning' })
  await deleteRole(roleId); ElMessage.success('已删除'); loadList()
}

onMounted(async () => {
  loadList()
  const res: any = await getMenuTree()
  menuTree.value = res.data || []
})
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; }
.page-title { font-size: 22px; font-weight: 800; color: #0f172a; letter-spacing: -0.3px; }
.page-desc { font-size: 13px; color: #94a3b8; margin-top: 4px; }

.role-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 14px; }

.role-card {
  background: white; border-radius: 14px; border: 1px solid #f0f1f6;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04); padding: 20px;
  display: flex; flex-direction: column; gap: 8px;
  transition: all 0.2s;
}
.role-card:hover { border-color: #c7d2fe; box-shadow: 0 4px 16px rgba(99,102,241,0.08); }

.role-card-top { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 4px; }
.role-avatar {
  width: 40px; height: 40px; border-radius: 10px;
  background: linear-gradient(135deg, #6366f1, #a855f7);
  color: white; font-size: 18px; font-weight: 800;
  display: flex; align-items: center; justify-content: center;
}
.role-badge { background: #f1f5f9; color: #64748b; font-size: 11px; font-weight: 600; padding: 3px 8px; border-radius: 6px; }
.role-name { font-size: 15px; font-weight: 700; color: #1e293b; }
.role-key { font-size: 12px; color: #94a3b8; font-family: ui-monospace, monospace; }
.role-remark { font-size: 12px; color: #64748b; }

.role-actions { display: flex; gap: 8px; margin-top: 4px; padding-top: 12px; border-top: 1px solid #f8f9fc; }
.role-btn { display: flex; align-items: center; gap: 5px; padding: 6px 12px; border-radius: 7px; border: none; font-size: 12px; font-weight: 600; cursor: pointer; transition: all 0.15s; }
.role-btn.edit { background: #eef2ff; color: #4f46e5; flex: 1; justify-content: center; }
.role-btn.edit:hover { background: #6366f1; color: white; }
.role-btn.del { background: #fef2f2; color: #ef4444; padding: 6px 10px; }
.role-btn.del:hover { background: #ef4444; color: white; }

.empty-grid { grid-column: 1/-1; text-align: center; padding: 60px; color: #94a3b8; font-size: 13px; }

.dialog-form { display: flex; flex-direction: column; gap: 16px; }
.df-row.two { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.df-item { display: flex; flex-direction: column; gap: 7px; }
.df-item label { font-size: 13px; font-weight: 600; color: #374151; }
.tree-wrap { border: 1px solid #e5e7ef; border-radius: 10px; padding: 12px; max-height: 220px; overflow-y: auto; background: #fafbfc; }
.mono { font-family: ui-monospace, monospace; }
</style>
