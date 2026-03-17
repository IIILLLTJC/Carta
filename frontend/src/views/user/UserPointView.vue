<template>
  <section class="crud-page">
    <div class="crud-card search-card">
      <div class="crud-card__header">
        <div>
          <p class="page-kicker">Pickup Points</p>
          <h3>车辆投放地点</h3>
        </div>
        <el-button type="primary" plain @click="router.push('/user/orders')">前往下单</el-button>
      </div>
      <el-form :model="searchForm" inline label-width="80px" class="search-form">
        <el-form-item label="区域名称">
          <el-input v-model="searchForm.regionName" placeholder="请输入区域名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="point-grid">
      <article v-for="item in pointList" :key="item.id" class="point-card">
        <div class="point-card__head">
          <div>
            <p class="page-kicker">{{ item.regionCode }}</p>
            <h3>{{ item.regionName }}</h3>
          </div>
          <el-tag type="success">可租车辆 {{ item.availableCarCount }}</el-tag>
        </div>
        <p class="point-address">{{ item.address }}</p>
        <p class="point-meta">坐标：{{ formatCoordinate(item) }}</p>

        <div v-if="item.availableCars.length" class="point-car-list">
          <article v-for="car in item.availableCars" :key="car.id" class="point-car-item">
            <div>
              <strong>{{ car.carCode }} / {{ car.licensePlate }}</strong>
              <p>{{ car.brand }} {{ car.model }} · {{ car.color || '未填写颜色' }}</p>
            </div>
            <div class="point-car-price">
              <span>￥{{ car.dailyRent }}/天</span>
              <small>押金 ￥{{ car.deposit }}</small>
            </div>
          </article>
        </div>
        <el-empty v-else description="当前区域暂无可租车辆" :image-size="72" />
      </article>
    </div>

    <div class="pagination-wrap point-pagination">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[6, 12, 18]"
        :total="pagination.total"
        @current-change="loadData"
        @size-change="handleSizeChange"
      />
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchUserPointPage } from '@/api/userPoint'

const router = useRouter()
const pointList = ref([])
const searchForm = reactive({ regionName: '' })
const pagination = reactive({ pageNum: 1, pageSize: 6, total: 0 })

async function loadData() {
  try {
    const { data } = await fetchUserPointPage({ ...searchForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize })
    pointList.value = data.records
    pagination.total = data.total
  } catch (error) {
    ElMessage.error(error.message || '投放地点加载失败')
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadData()
}

function handleReset() {
  searchForm.regionName = ''
  pagination.pageNum = 1
  loadData()
}

function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadData()
}

function formatCoordinate(item) {
  if (item.longitude == null || item.latitude == null) {
    return '--'
  }
  return `${item.longitude}, ${item.latitude}`
}

onMounted(() => {
  loadData()
})
</script>