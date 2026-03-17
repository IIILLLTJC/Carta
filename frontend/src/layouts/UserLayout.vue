<template>
  <el-container class="shell-layout user-shell">
    <el-header class="shell-header user-header">
      <div>
        <p class="page-kicker">用户端</p>
        <h2>{{ currentTitle }}</h2>
      </div>
      <div class="header-actions">
        <span class="user-badge">{{ authStore.displayName || '未登录用户' }}</span>
        <el-button text @click="router.push('/admin/home')">查看管理端</el-button>
        <el-button type="danger" plain @click="handleLogout">退出</el-button>
      </div>
    </el-header>

    <el-main class="shell-main user-main">
      <div class="user-menu-strip">
        <el-button
          v-for="item in userMenus"
          :key="item.path"
          :type="route.path === item.path ? 'primary' : 'default'"
          round
          @click="router.push(item.path)"
        >
          {{ item.title }}
        </el-button>
      </div>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userMenus } from '@/constants/menus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const currentTitle = computed(() => {
  const current = userMenus.find((item) => item.path === route.path)
  return current?.title || '用户端'
})

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>