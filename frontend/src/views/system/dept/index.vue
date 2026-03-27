<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">部门管理</h1>
        <p class="page-desc">维护学校组织架构树，支持多级部门</p>
      </div>
      <el-button v-permission="['sys:dept:manage']" type="primary" :icon="Plus" @click="openForm(null)">新增部门</el-button>
    </div>

    <div class="dept-layout">
      <!-- 左侧树状图 -->
      <div class="dept-tree-panel">
        <div class="panel-title">组织架构</div>
        <div class="tree-container" v-loading="loading">
          <div v-if="deptTree.length === 0 && !loading" class="tree-empty">暂无部门数据</div>
          <dept-node
            v-for="node in deptTree"
            :key="node.deptId"
            :node="node"
            :depth="0"
            @edit="openForm"
            @add-child="(id) => openForm(null, id)"
            @delete="handleDelete"
          />
        </div>
      </div>

      <!-- 右侧表格 -->
      <div class="dept-table-panel">
        <div class="panel-title">全量列表</div>
        <el-table :data="flatList" v-loading="loading" row-key="deptId">
          <el-table-column label="部门名称" min-width="160">
            <template #default="{ row }">
              <div class="dept-name-cell">
                <div class="dept-indent" :style="{ width: `${row._depth * 16}px` }" />
                <div class="dept-icon">
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <path d="M2 4h10v7a1 1 0 01-1 1H3a1 1 0 01-1-1V4zM4 4V3a1 1 0 011-1h4a1 1 0 011 1v1" stroke="#6366f1" stroke-width="1.3" stroke-linecap="round"/>
                  </svg>
                </div>
                <span class="dept-name">{{ row.deptName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="orderNum" label="排序" width="60" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <span :class="['status-pill', row.status === '0' ? 's-ok' : 's-off']">
                {{ row.status === '0' ? '正常' : '停用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="170">
            <template #default="{ row }">
              <div class="row-actions">
                <button class="act-btn blue" @click="openForm(row)" title="编辑">编辑</button>
                <button class="act-btn" @click="openForm(null, row.deptId)" title="添加子部门">添加子部门</button>
                <button class="act-btn red" @click="handleDelete(row.deptId)" title="删除">删除</button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="formVisible" :title="form.deptId ? '编辑部门' : (form.parentId ? '添加子部门' : '新增部门')" width="420px" align-center>
      <div class="dialog-form">
        <div class="df-item">
          <label>部门名称 <span class="req">*</span></label>
          <el-input v-model="form.deptName" placeholder="请输入部门名称" />
        </div>
        <div class="df-row">
          <div class="df-item">
            <label>排序</label>
            <el-input-number v-model="form.orderNum" :min="0" style="width:100%" />
          </div>
          <div class="df-item">
            <label>状态</label>
            <el-radio-group v-model="form.status">
              <el-radio value="0">正常</el-radio>
              <el-radio value="1">停用</el-radio>
            </el-radio-group>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, defineComponent, h } from 'vue'
import { getDeptTree, addDept, updateDept, deleteDept } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const formVisible = ref(false)
const deptTree = ref<any[]>([])
const form = reactive<any>({ deptId: null, parentId: 0, deptName: '', orderNum: 0, status: '0' })

const DeptNode = defineComponent({
  name: 'DeptNode',
  props: { node: Object, depth: Number },
  emits: ['edit', 'add-child', 'delete'],
  setup(props, { emit }) {
    const expanded = ref(true)
    return () => {
      const node = props.node as any
      const depth = props.depth as number
      const hasChildren = node.children && node.children.length > 0
      return h('div', { class: 'tree-node' }, [
        h('div', {
          class: 'tree-row',
          style: { paddingLeft: `${depth * 20 + 12}px` }
        }, [
          hasChildren ? h('button', {
            class: ['tree-toggle', expanded.value && 'expanded'],
            onClick: () => expanded.value = !expanded.value
          }, [
            h('svg', { width: 12, height: 12, viewBox: '0 0 12 12', fill: 'none' },
              h('path', { d: 'M3 4.5l3 3 3-3', stroke: 'currentColor', 'stroke-width': '1.5', 'stroke-linecap': 'round' }))
          ]) : h('span', { class: 'tree-leaf' }),
          h('div', { class: 'tree-icon' }),
          h('span', { class: 'tree-label' }, node.deptName),
          h('div', { class: 'tree-row-actions' }, [
            h('button', { class: 'tr-btn', onClick: () => emit('edit', node) }, '编辑'),
            h('button', { class: 'tr-btn', onClick: () => emit('add-child', node.deptId) }, '+子部门'),
            h('button', { class: 'tr-btn danger', onClick: () => emit('delete', node.deptId) }, '删除'),
          ])
        ]),
        expanded.value && hasChildren ? h('div', { class: 'tree-children' },
          node.children.map((child: any) => h(DeptNode, {
            key: child.deptId, node: child, depth: depth + 1,
            onEdit: (n: any) => emit('edit', n),
            onAddChild: (id: number) => emit('add-child', id),
            onDelete: (id: number) => emit('delete', id),
          }))
        ) : null
      ])
    }
  }
})

const flatList = computed(() => {
  const result: any[] = []
  function flatten(nodes: any[], depth: number) {
    for (const n of nodes) {
      result.push({ ...n, _depth: depth })
      if (n.children) flatten(n.children, depth + 1)
    }
  }
  flatten(deptTree.value, 0)
  return result
})

async function loadTree() {
  loading.value = true
  try { const res: any = await getDeptTree(); deptTree.value = res.data || [] }
  finally { loading.value = false }
}

function openForm(row: any, parentId?: number) {
  Object.assign(form, { deptId: null, parentId: parentId || 0, deptName: '', orderNum: 0, status: '0' })
  if (row) Object.assign(form, row)
  formVisible.value = true
}

async function handleSave() {
  if (!form.deptName.trim()) return ElMessage.warning('请输入部门名称')
  if (form.deptId) await updateDept(form)
  else await addDept(form)
  ElMessage.success('保存成功')
  formVisible.value = false
  loadTree()
}

async function handleDelete(deptId: number) {
  await ElMessageBox.confirm('确定删除该部门？若有子部门请先删除子部门。', '删除确认', { type: 'warning' })
  await deleteDept(deptId)
  ElMessage.success('已删除')
  loadTree()
}

onMounted(loadTree)
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; }
.page-title { font-size: 22px; font-weight: 800; color: #0f172a; letter-spacing: -0.3px; }
.page-desc { font-size: 13px; color: #94a3b8; margin-top: 4px; }

.dept-layout { display: grid; grid-template-columns: 1fr; gap: 16px; }
@media (min-width: 1024px) { .dept-layout { grid-template-columns: 280px 1fr; } }

.dept-tree-panel, .dept-table-panel {
  background: white; border-radius: 14px; border: 1px solid #f0f1f6;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04); overflow: hidden;
}

.panel-title { font-size: 13px; font-weight: 700; color: #374151; padding: 14px 16px; border-bottom: 1px solid #f8f9fc; }
.tree-container { padding: 8px 0; }
.tree-empty { text-align: center; padding: 32px; color: #94a3b8; font-size: 13px; }

:deep(.tree-node) { }
:deep(.tree-row) {
  display: flex; align-items: center; gap: 6px; padding: 7px 12px;
  cursor: pointer; transition: background 0.1s;
}
:deep(.tree-row:hover) { background: #f8f9fc; }
:deep(.tree-toggle) {
  width: 20px; height: 20px; border-radius: 4px; border: none;
  background: transparent; color: #94a3b8; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: transform 0.2s; flex-shrink: 0;
}
:deep(.tree-toggle.expanded svg) { transform: none; }
:deep(.tree-toggle svg) { transform: rotate(-90deg); }
:deep(.tree-toggle.expanded svg) { transform: rotate(0deg); }
:deep(.tree-leaf) { width: 20px; flex-shrink: 0; }
:deep(.tree-icon) {
  width: 18px; height: 18px; border-radius: 4px;
  background: #eef2ff; flex-shrink: 0;
}
:deep(.tree-label) { flex: 1; font-size: 13px; color: #1e293b; font-weight: 500; }
:deep(.tree-row-actions) { display: none; gap: 4px; }
:deep(.tree-row:hover .tree-row-actions) { display: flex; }
:deep(.tr-btn) {
  padding: 2px 8px; border-radius: 5px; border: none; font-size: 11px;
  font-weight: 600; cursor: pointer; background: #f1f5f9; color: #64748b; transition: all 0.15s;
}
:deep(.tr-btn:hover) { background: #eef2ff; color: #4f46e5; }
:deep(.tr-btn.danger:hover) { background: #fef2f2; color: #ef4444; }
:deep(.tree-children) { }

.dept-name-cell { display: flex; align-items: center; gap: 8px; }
.dept-indent { flex-shrink: 0; }
.dept-icon { width: 20px; height: 20px; border-radius: 5px; background: #eef2ff; flex-shrink: 0; }
.dept-name { font-size: 13px; font-weight: 600; color: #1e293b; }

.status-pill { display: inline-flex; align-items: center; padding: 3px 8px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.s-ok { background: #ecfdf5; color: #059669; }
.s-off { background: #fef2f2; color: #dc2626; }

.row-actions { display: flex; gap: 4px; }
.act-btn { padding: 4px 10px; border-radius: 6px; border: none; font-size: 12px; font-weight: 500; cursor: pointer; background: #f1f5f9; color: #64748b; transition: all 0.15s; }
.act-btn.blue { background: #eef2ff; color: #4f46e5; }
.act-btn.red { background: #fef2f2; color: #ef4444; }
.act-btn:hover { opacity: 0.8; }

.dialog-form { display: flex; flex-direction: column; gap: 16px; }
.df-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.df-item { display: flex; flex-direction: column; gap: 7px; }
.df-item label { font-size: 13px; font-weight: 600; color: #374151; }
.req { color: #ef4444; }
</style>
