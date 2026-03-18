<template>
  <section v-loading="loading" class="dashboard-grid home-dashboard-grid">
    <article class="hero-panel">
      <p class="page-kicker">Admin Dashboard</p>
      <h3>运营看板</h3>
      <p>聚合今日订单、归还待确认、车辆可用性和最近业务动态，便于管理员优先处理当前运营链路。</p>
    </article>

    <article v-for="card in statCards" :key="card.key" class="module-card stat-card">
      <p class="page-kicker">{{ card.kicker }}</p>
      <h3>{{ card.value }}</h3>
      <p class="module-copy">{{ card.label }}</p>
    </article>

    <article class="module-card recent-card">
      <div class="module-card__head">
        <div>
          <p class="page-kicker">Recent Orders</p>
          <h3>最近订单</h3>
        </div>
        <el-tag type="info">{{ dashboard.recentOrders.length }} 条</el-tag>
      </div>
      <div v-if="dashboard.recentOrders.length" class="feed-list">
        <div v-for="item in dashboard.recentOrders" :key="item.id" class="feed-item">
          <div>
            <strong>{{ item.orderNo }}</strong>
            <p>{{ item.realName || item.username || '未知用户' }} / {{ item.carCode || '-' }} {{ item.licensePlate || '' }}</p>
          </div>
          <div class="feed-meta">
            <el-tag size="small">{{ orderLabel(item.orderStatus) }}</el-tag>
            <span>{{ item.createTime || '-' }}</span>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无最近订单" :image-size="72" />
    </article>

    <article class="module-card recent-card">
      <div class="module-card__head">
        <div>
          <p class="page-kicker">Recent Returns</p>
          <h3>最近归还记录</h3>
        </div>
        <el-tag type="warning">{{ dashboard.recentReturns.length }} 条</el-tag>
      </div>
      <div v-if="dashboard.recentReturns.length" class="feed-list">
        <div v-for="item in dashboard.recentReturns" :key="item.id" class="feed-item">
          <div>
            <strong>{{ item.orderNo || '-' }}</strong>
            <p>{{ item.realName || item.username || '未知用户' }} / {{ item.carCode || '-' }} {{ item.licensePlate || '' }}</p>
            <p class="table-secondary">{{ item.vehicleCondition || '待补充验车结论' }}</p>
          </div>
          <div class="feed-meta">
            <el-tag size="small" :type="returnTagType(item.status)">{{ returnLabel(item.status) }}</el-tag>
            <span>{{ item.returnTime || '-' }}</span>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无最近归还记录" :image-size="72" />
    </article>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAdminDashboard } from '@/api/portal'

const loading = ref(false)
const dashboard = reactive({
  todayOrderCount: 0,
  usingOrderCount: 0,
  pendingReturnCount: 0,
  availableCarCount: 0,
  maintenanceCarCount: 0,
  regionCount: 0,
  recentOrders: [],
  recentReturns: []
})

const statCards = computed(() => [
  { key: 'today', kicker: 'Today', label: '今日订单数', value: dashboard.todayOrderCount ?? 0 },
  { key: 'using', kicker: 'Using', label: '使用中订单数', value: dashboard.usingOrderCount ?? 0 },
  { key: 'pending', kicker: 'Returns', label: '待确认归还数', value: dashboard.pendingReturnCount ?? 0 },
  { key: 'available', kicker: 'Fleet', label: '可用车辆数', value: dashboard.availableCarCount ?? 0 },
  { key: 'maintenance', kicker: 'Care', label: '维修中车辆数', value: dashboard.maintenanceCarCount ?? 0 },
  { key: 'regions', kicker: 'Regions', label: '区域数量', value: dashboard.regionCount ?? 0 }
])

const orderStatusMap = {
  CREATED: '已创建',
  PAID: '已支付',
  USING: '使用中',
  RETURNING: '归还中',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

const returnStatusMap = {
  PENDING: '待确认',
  CONFIRMED: '已确认',
  SETTLED: '已结算',
  COMPLETED: '已完成'
}

function orderLabel(status) {
  return orderStatusMap[status] || status || '-'
}

function returnLabel(status) {
  return returnStatusMap[status] || status || '-'
}

function returnTagType(status) {
  if (status === 'COMPLETED') return 'success'
  if (status === 'SETTLED') return 'warning'
  if (status === 'CONFIRMED') return 'info'
  return ''
}

async function loadDashboard() {
  loading.value = true
  try {
    const { data } = await fetchAdminDashboard()
    Object.assign(dashboard, {
      todayOrderCount: Number(data?.todayOrderCount || 0),
      usingOrderCount: Number(data?.usingOrderCount || 0),
      pendingReturnCount: Number(data?.pendingReturnCount || 0),
      availableCarCount: Number(data?.availableCarCount || 0),
      maintenanceCarCount: Number(data?.maintenanceCarCount || 0),
      regionCount: Number(data?.regionCount || 0),
      recentOrders: data?.recentOrders || [],
      recentReturns: data?.recentReturns || []
    })
  } catch (error) {
    ElMessage.error(error.message || '运营看板加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDashboard()
})
</script>