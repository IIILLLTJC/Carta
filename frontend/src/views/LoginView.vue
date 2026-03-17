<template>
  <main class="login-shell">
    <section class="login-showcase">
      <p class="page-kicker">CarTaxi</p>
      <h1>车辆租赁管理系统</h1>
      <p class="showcase-copy">
        面向用户租车与运营管理的双端系统骨架。第一阶段已完成认证入口、路由框架、模块布局与接口基础能力。
      </p>
      <div class="showcase-list">
        <article>
          <strong>用户端</strong>
          <span>个人中心、投放地点、订单管理、归还管理</span>
        </article>
        <article>
          <strong>管理端</strong>
          <span>用户、区域、车辆、投放、回收、订单、归还、管理员</span>
        </article>
      </div>
    </section>

    <section class="login-panel">
      <div class="login-card">
        <div class="login-card__head">
          <p class="page-kicker">Phase 1 Login</p>
          <h2>进入系统</h2>
        </div>

        <el-tabs v-model="activeTab">
          <el-tab-pane label="管理端登录" name="admin" />
          <el-tab-pane label="用户端登录" name="user" />
        </el-tabs>

        <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @submit.prevent>
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" size="large" />
          </el-form-item>
          <el-button class="submit-btn" type="primary" size="large" :loading="submitting" @click="handleLogin">
            登录并进入{{ activeTab === 'admin' ? '管理端' : '用户端' }}
          </el-button>
        </el-form>

        <div class="demo-box">
          <div>
            <p class="demo-label">演示账号</p>
            <span>{{ activeTab === 'admin' ? 'admin / 123456' : 'user01 / 123456' }}</span>
          </div>
          <el-button text @click="fillDemoAccount">一键填充</el-button>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const submitting = ref(false)
const activeTab = ref('admin')
const form = reactive({
  username: 'admin',
  password: '123456'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

watch(activeTab, (value) => {
  if (value === 'admin') {
    form.username = 'admin'
    form.password = '123456'
  } else {
    form.username = 'user01'
    form.password = '123456'
  }
})

const fillDemoAccount = () => {
  if (activeTab.value === 'admin') {
    form.username = 'admin'
    form.password = '123456'
  } else {
    form.username = 'user01'
    form.password = '123456'
  }
}

const handleLogin = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await authStore.login({
      endpoint: activeTab.value === 'admin' ? '/api/auth/admin/login' : '/api/auth/user/login',
      form
    })
    ElMessage.success('登录成功')
    router.push(activeTab.value === 'admin' ? '/admin/home' : '/user/home')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    submitting.value = false
  }
}
</script>
