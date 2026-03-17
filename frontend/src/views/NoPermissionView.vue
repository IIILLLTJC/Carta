<template>
  <main class="login-shell forbidden-shell">
    <section class="login-panel">
      <div class="login-card forbidden-card">
        <div class="login-card__head">
          <p class="page-kicker">Access Control</p>
          <h2>无权访问</h2>
        </div>
        <p class="module-copy">当前账号没有访问该页面的权限，请返回首页或退出登录后重新进入正确的业务端。</p>
        <div class="forbidden-actions">
          <el-button type="primary" @click="goHome">返回首页</el-button>
          <el-button @click="goLogin">退出登录</el-button>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

function goHome() {
  router.push(authStore.homeRoute)
}

function goLogin() {
  authStore.logout()
  router.push('/login')
}
</script>