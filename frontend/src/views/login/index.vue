<template>
  <div class="login-root">
    <!-- 左侧品牌区 -->
    <div class="login-brand">
      <div class="brand-content">
        <div class="brand-logo">
          <svg width="36" height="36" viewBox="0 0 36 36" fill="none">
            <rect width="36" height="36" rx="10" fill="white" fill-opacity="0.15"/>
            <path d="M8 14l10-6 10 6v8l-10 6-10-6V14z" stroke="white" stroke-width="2" stroke-linejoin="round"/>
            <path d="M8 14l10 6 10-6" stroke="white" stroke-width="2" stroke-linejoin="round"/>
            <path d="M18 20v8" stroke="white" stroke-width="2"/>
          </svg>
          <span>AssetMS</span>
        </div>
        <h1 class="brand-title">高校固定资产<br/>全生命周期管理系统</h1>
        <p class="brand-sub">统一管控 · 全程追踪 · 智能盘点</p>
        <div class="brand-features">
          <div v-for="f in features" :key="f.text" class="feature-item">
            <div class="feature-icon">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M3 8l3.5 3.5L13 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <span>{{ f.text }}</span>
          </div>
        </div>
      </div>
      <div class="brand-deco">
        <div class="deco-circle c1"/>
        <div class="deco-circle c2"/>
        <div class="deco-circle c3"/>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="login-form-area">
      <div class="login-card">
        <div class="login-header">
          <h2>欢迎回来</h2>
          <p>请登录您的账户以继续</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
          <el-form-item prop="username">
            <div class="input-label">用户名</div>
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <div class="input-label">密码</div>
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form>

        <div class="demo-accounts">
          <div class="demo-title">
            <span class="line"/><span>演示账号</span><span class="line"/>
          </div>
          <div class="demo-list">
            <button
              v-for="a in accounts"
              :key="a.username"
              class="demo-item"
              @click="fillAccount(a)"
            >
              <div class="demo-avatar" :style="{ background: a.color }">{{ a.label[0] }}</div>
              <div>
                <div class="demo-name">{{ a.label }}</div>
                <div class="demo-user">{{ a.username }}</div>
              </div>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'Admin@123' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const features = [
  { text: 'RBAC 权限管控，多角色多数据域' },
  { text: '移动端扫码借用 / 报修 / 盘点' },
  { text: '资产全流程追踪与折旧管理' },
  { text: '多级审批流程，实时待办提醒' },
]

const accounts = [
  { label: '超级管理员', username: 'admin', color: '#6366f1' },
  { label: '部门管理员', username: 'dept_mgr', color: '#10b981' },
  { label: '普通师生', username: 'teacher01', color: '#f59e0b' },
  { label: '维修人员', username: 'repair01', color: '#ef4444' },
]

function fillAccount(a: { username: string }) {
  form.username = a.username
  form.password = 'Admin@123'
}

async function handleLogin() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await userStore.loginAction(form)
    router.push('/dashboard')
  } catch {
    ElMessage.error('用户名或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-root {
  min-height: 100vh;
  display: flex;
}

/* 左侧 */
.login-brand {
  display: none;
  width: 480px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 50%, #a855f7 100%);
  position: relative;
  overflow: hidden;
  padding: 56px 48px;
  flex-direction: column;
  justify-content: center;
}
@media (min-width: 1024px) { .login-brand { display: flex; } }

.brand-content { position: relative; z-index: 1; }

.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 48px;
  color: white;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.3px;
}

.brand-title {
  color: white;
  font-size: 32px;
  font-weight: 800;
  line-height: 1.25;
  letter-spacing: -0.5px;
  margin-bottom: 12px;
}

.brand-sub {
  color: rgba(255,255,255,0.65);
  font-size: 15px;
  margin-bottom: 48px;
}

.brand-features { display: flex; flex-direction: column; gap: 16px; }

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(255,255,255,0.9);
  font-size: 14px;
}
.feature-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: white;
}

/* 装饰圆 */
.brand-deco { position: absolute; inset: 0; }
.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.06);
}
.c1 { width: 360px; height: 360px; right: -120px; top: -120px; }
.c2 { width: 240px; height: 240px; right: -40px; bottom: 80px; }
.c3 { width: 120px; height: 120px; left: 40px; bottom: -40px; background: rgba(255,255,255,0.04); }

/* 右侧 */
.login-form-area {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 24px;
  background: #f8f9fc;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 20px;
  padding: 40px 36px;
  box-shadow: 0 8px 40px rgba(0,0,0,0.08);
}

.login-header { margin-bottom: 28px; }
.login-header h2 {
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.4px;
  margin-bottom: 6px;
}
.login-header p { color: #64748b; font-size: 14px; }

.login-form :deep(.el-form-item) { margin-bottom: 20px; }
.input-label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 6px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 10px !important;
  background: linear-gradient(135deg, #6366f1, #818cf8) !important;
  border: none !important;
  box-shadow: 0 4px 14px rgba(99,102,241,0.35) !important;
  letter-spacing: 0.05em;
  margin-top: 4px;
  transition: all 0.2s !important;
}
.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(99,102,241,0.45) !important;
}

/* 演示账号 */
.demo-accounts { margin-top: 28px; }
.demo-title {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #94a3b8;
  font-size: 12px;
  margin-bottom: 16px;
}
.demo-title .line { flex: 1; height: 1px; background: #e5e7ef; }

.demo-list { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.demo-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #f0f1f6;
  background: #f8f9fc;
  cursor: pointer;
  transition: all 0.15s;
  text-align: left;
}
.demo-item:hover { border-color: #6366f1; background: #eef2ff; }
.demo-avatar {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  color: white;
  font-size: 13px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.demo-name { font-size: 12px; font-weight: 600; color: #1e293b; }
.demo-user { font-size: 11px; color: #94a3b8; margin-top: 1px; }
</style>
