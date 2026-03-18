<template>
  <section class="crud-page point-page">
    <div class="crud-card search-card">
      <div class="crud-card__header">
        <div>
          <p class="page-kicker">Pickup Points</p>
          <h3>{{ ui.pageTitle }}</h3>
        </div>
        <el-button type="primary" plain @click="router.push('/user/orders')">{{ ui.gotoOrder }}</el-button>
      </div>
      <el-form :model="searchForm" inline label-width="80px" class="search-form">
        <el-form-item :label="ui.regionName">
          <el-input v-model="searchForm.regionName" :placeholder="ui.regionNamePlaceholder" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ ui.search }}</el-button>
          <el-button @click="handleReset">{{ ui.reset }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="point-map-layout">
      <div class="crud-card point-map-card">
        <div class="point-map-card__header">
          <div>
            <p class="page-kicker">Region Map</p>
            <h3>{{ ui.mapTitle }}</h3>
          </div>
          <el-tag type="success">{{ mapPoints.length }} {{ ui.regionUnit }}</el-tag>
        </div>
        <CoordinateMapBoard
          :points="mapBoardPoints"
          :selected-id="activeRegionId"
          :height="380"
          :empty-text="ui.mapEmpty"
          @pick="handleMapPick"
        />
      </div>

      <div class="crud-card point-focus-card">
        <div class="point-map-card__header">
          <div>
            <p class="page-kicker">Selected Region</p>
            <h3>{{ activeRegion?.regionName || ui.pickRegion }}</h3>
          </div>
          <el-tag v-if="activeRegion" type="warning">{{ ui.availableCars }} {{ activeRegion.availableCarCount ?? 0 }}</el-tag>
        </div>

        <template v-if="activeRegion">
          <p class="point-focus-card__code">{{ activeRegion.regionCode || '--' }}</p>
          <p class="point-focus-card__address">{{ activeRegion.address || ui.noAddress }}</p>
          <div class="point-focus-card__meta">
            <span>{{ ui.longitude }}{{ formatSingleCoordinate(activeRegion.longitude) }}</span>
            <span>{{ ui.latitude }}{{ formatSingleCoordinate(activeRegion.latitude) }}</span>
          </div>
          <el-button type="primary" plain @click="router.push('/user/orders')">{{ ui.orderFromRegion }}</el-button>
        </template>
        <el-empty v-else :description="ui.pickRegionHint" :image-size="72" />

        <div class="point-focus-card__list">
          <button
            v-for="item in mapPoints"
            :key="item.id"
            type="button"
            class="point-focus-card__item"
            :class="{ 'point-focus-card__item--active': item.id === activeRegionId }"
            @click="activeRegionId = item.id"
          >
            <div>
              <strong>{{ item.regionName }}</strong>
              <p>{{ formatCoordinate(item) }}</p>
            </div>
            <span>{{ item.availableCarCount }} {{ ui.carUnit }}</span>
          </button>
        </div>
      </div>
    </div>

    <div class="point-grid">
      <article
        v-for="item in pointList"
        :key="item.id"
        class="point-card"
        :class="{ 'point-card--active': item.id === activeRegionId }"
        @click="activeRegionId = item.id"
      >
        <div class="point-card__head">
          <div>
            <p class="page-kicker">{{ item.regionCode }}</p>
            <h3>{{ item.regionName }}</h3>
          </div>
          <el-tag type="success">{{ ui.availableCars }} {{ item.availableCarCount }}</el-tag>
        </div>
        <p class="point-address">{{ item.address }}</p>
        <p class="point-meta">{{ ui.coordinate }}{{ formatCoordinate(item) }}</p>

        <div v-if="item.availableCars.length" class="point-car-list">
          <article v-for="car in item.availableCars" :key="car.id" class="point-car-item">
            <div>
              <strong>{{ car.carCode }} / {{ car.licensePlate }}</strong>
              <p>{{ car.brand }} {{ car.model }} ? {{ car.color || ui.noColor }}</p>
            </div>
            <div class="point-car-price">
              <span>{{ ui.dailyRentPrefix }}{{ car.dailyRent }}{{ ui.perDay }}</span>
              <small>{{ ui.depositPrefix }}{{ car.deposit }}</small>
            </div>
          </article>
        </div>
        <el-empty v-else :description="ui.noCarsInRegion" :image-size="72" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import CoordinateMapBoard from '@/components/CoordinateMapBoard.vue'
import { fetchUserPointMap, fetchUserPointPage } from '@/api/userPoint'

const ui = {
  pageTitle: '\u8f66\u8f86\u6295\u653e\u5730\u70b9',
  mapTitle: '\u533a\u57df\u70b9\u4f4d\u5730\u56fe',
  gotoOrder: '\u524d\u5f80\u4e0b\u5355',
  regionName: '\u533a\u57df\u540d\u79f0',
  regionNamePlaceholder: '\u8bf7\u8f93\u5165\u533a\u57df\u540d\u79f0',
  search: '\u67e5\u8be2',
  reset: '\u91cd\u7f6e',
  regionUnit: '\u4e2a\u533a\u57df',
  availableCars: '\u53ef\u79df\u8f66\u8f86',
  pickRegion: '\u8bf7\u9009\u62e9\u5730\u56fe\u70b9\u4f4d',
  noAddress: '\u6682\u65e0\u5730\u5740',
  longitude: '\u7ecf\u5ea6\uff1a',
  latitude: '\u7eac\u5ea6\uff1a',
  orderFromRegion: '\u5230\u8be5\u533a\u57df\u4e0b\u5355',
  pickRegionHint: '\u70b9\u51fb\u5730\u56fe\u4e0a\u7684\u533a\u57df\u70b9\u4f4d\u67e5\u770b\u8be6\u60c5',
  carUnit: '\u8f86',
  coordinate: '\u5750\u6807\uff1a',
  noColor: '\u672a\u586b\u5199\u989c\u8272',
  dailyRentPrefix: '\uffe5',
  perDay: '/\u5929',
  depositPrefix: '\u62bc\u91d1 \uffe5',
  noCarsInRegion: '\u5f53\u524d\u533a\u57df\u6682\u65e0\u53ef\u79df\u8f66\u8f86',
  mapEmpty: '\u5f53\u524d\u7b5b\u9009\u6761\u4ef6\u4e0b\u6682\u65e0\u53ef\u5c55\u793a\u5750\u6807',
  loadError: '\u6295\u653e\u5730\u70b9\u52a0\u8f7d\u5931\u8d25',
  cardLabelSep: ' ? ',
  cardLabelSuffix: '\u8f86'
}

const router = useRouter()
const pointList = ref([])
const mapPoints = ref([])
const activeRegionId = ref(null)
const searchForm = reactive({ regionName: '' })
const pagination = reactive({ pageNum: 1, pageSize: 6, total: 0 })

const mapBoardPoints = computed(() => {
  return mapPoints.value.map((item) => ({
    ...item,
    label: `${item.regionName}${ui.cardLabelSep}${item.availableCarCount ?? 0}${ui.cardLabelSuffix}`
  }))
})

const activeRegion = computed(() => {
  return mapPoints.value.find((item) => item.id === activeRegionId.value)
    || pointList.value.find((item) => item.id === activeRegionId.value)
    || null
})

async function loadData() {
  try {
    const [pageResult, mapResult] = await Promise.all([
      fetchUserPointPage({ ...searchForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize }),
      fetchUserPointMap({ regionName: searchForm.regionName })
    ])
    const pageData = pageResult.data || {}
    pointList.value = pageData.records || []
    pagination.total = pageData.total || 0
    mapPoints.value = mapResult.data || []

    if (!mapPoints.value.some((item) => item.id === activeRegionId.value)) {
      activeRegionId.value = mapPoints.value[0]?.id ?? pointList.value[0]?.id ?? null
    }
  } catch (error) {
    ElMessage.error(error.message || ui.loadError)
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadData()
}

function handleReset() {
  searchForm.regionName = ''
  pagination.pageNum = 1
  activeRegionId.value = null
  loadData()
}

function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadData()
}

function handleMapPick(point) {
  if (point?.id != null) {
    activeRegionId.value = point.id
  }
}

function formatCoordinate(item) {
  if (item.longitude == null || item.latitude == null) {
    return '--'
  }
  return `${Number(item.longitude).toFixed(6)}, ${Number(item.latitude).toFixed(6)}`
}

function formatSingleCoordinate(value) {
  if (value == null) {
    return '--'
  }
  return Number(value).toFixed(6)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.point-page {
  gap: 24px;
}

.point-map-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(320px, 0.9fr);
  gap: 24px;
}

.point-map-card,
.point-focus-card {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.point-map-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.point-focus-card__code {
  margin: 0;
  color: #2b6d79;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.point-focus-card__address {
  margin: 0;
  color: #304959;
  line-height: 1.7;
}

.point-focus-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 20px;
  color: #607884;
  font-size: 13px;
}

.point-focus-card__list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 240px;
  overflow: auto;
}

.point-focus-card__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
  padding: 14px 16px;
  border: 1px solid rgba(39, 92, 120, 0.12);
  border-radius: 18px;
  background: rgba(248, 251, 252, 0.9);
  color: #1f3a4a;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
}

.point-focus-card__item p {
  margin: 6px 0 0;
  color: #69808c;
  font-size: 12px;
}

.point-focus-card__item--active {
  border-color: rgba(239, 140, 40, 0.34);
  background: rgba(255, 244, 230, 0.92);
  box-shadow: 0 14px 32px rgba(239, 140, 40, 0.12);
}

.point-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 20px;
}

.point-card {
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.point-card--active {
  border-color: rgba(239, 140, 40, 0.28);
  box-shadow: 0 18px 36px rgba(239, 140, 40, 0.12);
  transform: translateY(-2px);
}

.point-pagination {
  margin-top: 0;
}

@media (max-width: 1080px) {
  .point-map-layout {
    grid-template-columns: 1fr;
  }
}
</style>
