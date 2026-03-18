<template>
  <section v-loading="loading" class="dashboard-grid user-dashboard-grid home-dashboard-grid">
    <article class="hero-panel user-hero">
      <p class="page-kicker">User Dashboard</p>
      <h3>我的看板</h3>
      <p>集中查看当前订单进度、待确认归还状态，并从快捷入口直达常用操作页面。</p>
    </article>

    <article v-for="card in statCards" :key="card.key" class="module-card stat-card">
      <p class="page-kicker">{{ card.kicker }}</p>
      <h3>{{ card.value }}</h3>
      <p class="module-copy">{{ card.label }}</p>
    </article>

    <article class="module-card shortcut-card-wrap">
      <div class="module-card__head">
        <div>
          <p class="page-kicker">Quick Links</p>
          <h3>快捷入口</h3>
        </div>
      </div>
      <div class="shortcut-grid">
        <router-link v-for="item in dashboard.quickEntries" :key="item.path" :to="item.path" class="shortcut-card">
          <strong>{{ item.name }}</strong>
          <span>{{ item.description }}</span>
        </router-link>
      </div>
    </article>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchUserDashboard } from '@/api/portal'

const loading = ref(false)
const dashboard = reactive({
  activeOrderCount: 0,
  pendingReturnCount: 0,
  historyOrderCount: 0,
  quickEntries: []
})

const statCards = computed(() => [
  { key: 'active', kicker: 'Ongoing', label: '我的进行中订单数', value: dashboard.activeOrderCount ?? 0 },
  { key: 'pending', kicker: 'Returns', label: '我的待确认归还数', value: dashboard.pendingReturnCount ?? 0 },
  { key: 'history', kicker: 'History', label: '我的历史订单数', value: dashboard.historyOrderCount ?? 0 }
])

async function loadDashboard() {
  loading.value = true
  try {
    const { data } = await fetchUserDashboard()
    Object.assign(dashboard, {
      activeOrderCount: Number(data?.activeOrderCount || 0),
      pendingReturnCount: Number(data?.pendingReturnCount || 0),
      historyOrderCount: Number(data?.historyOrderCount || 0),
      quickEntries: data?.quickEntries || []
    })
  } catch (error) {
    ElMessage.error(error.message || '用户看板加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDashboard()
})
</script>