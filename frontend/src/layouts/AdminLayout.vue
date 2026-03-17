<template>
  <el-container class="shell-layout admin-shell">
    <el-aside width="250px" class="shell-aside">
      <div class="brand-box">
        <p class="brand-kicker">CarTaxi</p>
        <h1>运营控制台</h1>
      </div>
      <el-menu :default-active="route.path" router class="nav-menu">
        <el-menu-item v-for="item in adminMenus" :key="item.path" :index="item.path">
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="shell-header">
        <div>
          <p class="page-kicker">管理端</p>
          <h2>{{ currentTitle }}</h2>
        </div>
        <div class="header-actions">
          <span class="user-badge">{{ authStore.displayName || '未登录管理员' }}</span>
          <el-button text @click="router.push('/user/home')">查看用户端</el-button>
          <el-button type="danger" plain @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="shell-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { adminMenus } from '@/constants/menus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const currentTitle = computed(() => {
  const current = adminMenus.find((item) => item.path === route.path)
  return current?.title || '管理端'
})

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>