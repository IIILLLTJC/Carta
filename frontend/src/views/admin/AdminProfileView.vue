<template>
  <section v-loading="loading" class="module-card profile-shell">
    <div class="module-card__head">
      <div>
        <p class="page-kicker">Admin Profile</p>
        <h3>个人中心</h3>
      </div>
      <el-button @click="loadProfile">重新加载</el-button>
    </div>

    <div class="profile-grid">
      <div class="profile-overview">
        <div class="profile-avatar">{{ profileInitial }}</div>
        <h4>{{ profile.displayName || profile.username || '-' }}</h4>
        <p>{{ profile.username || '-' }}</p>
        <el-tag :type="profile.role === 'SUPER_ADMIN' ? 'danger' : 'success'">{{ roleLabel }}</el-tag>
      </div>

      <div class="profile-form-wrap">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="管理员 ID">{{ profile.userId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ profile.username || '-' }}</el-descriptions-item>
          <el-descriptions-item label="显示名称">{{ profile.displayName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ roleLabel }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ profile.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ profile.email || '-' }}</el-descriptions-item>
        </el-descriptions>
        <p class="table-secondary" style="margin-top: 16px;">
          当前页面展示登录管理员的账户信息；如需维护管理员账号列表，请前往“管理员管理”。
        </p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAuthProfile } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const profile = reactive({
  userId: undefined,
  username: '',
  displayName: '',
  role: '',
  phone: '',
  email: ''
})

const profileInitial = computed(() => {
  const source = profile.displayName || profile.username || 'A'
  return source.slice(0, 1).toUpperCase()
})

const roleLabel = computed(() => {
  if (profile.role === 'SUPER_ADMIN') return '超级管理员'
  if (profile.role === 'ADMIN') return '普通管理员'
  return profile.role || '-'
})

async function loadProfile() {
  loading.value = true
  try {
    const { data } = await fetchAuthProfile()
    Object.assign(profile, data || {})
    authStore.setDisplayName(profile.displayName || profile.username || '')
  } catch (error) {
    ElMessage.error(error.message || '管理员资料加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>