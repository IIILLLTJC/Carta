<template>
  <section class="crud-page">
    <div class="crud-card search-card">
      <div class="crud-card__header">
        <div>
          <p class="page-kicker">Region Management</p>
          <h3>{{ ui.pageTitle }}</h3>
        </div>
        <el-button type="primary" @click="openCreate">{{ ui.create }}</el-button>
      </div>

      <el-form :model="searchForm" inline label-width="80px" class="search-form">
        <el-form-item :label="ui.regionName">
          <el-input v-model="searchForm.regionName" :placeholder="ui.regionNamePlaceholder" clearable />
        </el-form-item>
        <el-form-item :label="ui.regionCode">
          <el-input v-model="searchForm.regionCode" :placeholder="ui.regionCodePlaceholder" clearable />
        </el-form-item>
        <el-form-item :label="ui.status">
          <el-select v-model="searchForm.status" :placeholder="ui.allStatus" clearable style="width: 140px">
            <el-option :label="ui.enabled" :value="1" />
            <el-option :label="ui.disabled" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ ui.search }}</el-button>
          <el-button @click="handleReset">{{ ui.reset }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="crud-card table-card">
      <el-table v-loading="loading" :data="tableData" border class="crud-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="regionName" :label="ui.regionName" min-width="160" />
        <el-table-column prop="regionCode" :label="ui.regionCode" min-width="120" />
        <el-table-column prop="address" :label="ui.address" min-width="220" show-overflow-tooltip />
        <el-table-column :label="ui.coordinate" min-width="180">
          <template #default="scope">
            <span>{{ formatCoordinate(scope.row) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="ui.status" width="110">
          <template #default="scope">
            <el-switch
              :model-value="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(value) => handleStatusChange(scope.row, value)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="remark" :label="ui.remark" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" :label="ui.createTime" min-width="180" />
        <el-table-column :label="ui.action" width="180" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openEdit(scope.row.id)">{{ ui.edit }}</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row.id)">{{ ui.delete }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          @current-change="loadData"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <el-dialog v-model="dialog.visible" :title="dialogTitle" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="region-form-grid">
          <el-form-item :label="ui.regionName" prop="regionName">
            <el-input v-model="form.regionName" :placeholder="ui.regionNamePlaceholder" />
          </el-form-item>
          <el-form-item :label="ui.regionCode" prop="regionCode">
            <el-input v-model="form.regionCode" :placeholder="ui.regionCodePlaceholder" />
          </el-form-item>
          <el-form-item :label="ui.longitude" prop="longitude">
            <el-input-number
              v-model="form.longitude"
              :precision="6"
              :step="0.000001"
              :min="-180"
              :max="180"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item :label="ui.latitude" prop="latitude">
            <el-input-number
              v-model="form.latitude"
              :precision="6"
              :step="0.000001"
              :min="-90"
              :max="90"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item :label="ui.address" prop="address" class="region-form-grid__full">
            <el-input v-model="form.address" type="textarea" :rows="3" :placeholder="ui.addressPlaceholder" />
          </el-form-item>
          <el-form-item :label="ui.mapPick" class="region-form-grid__full">
            <div class="region-map-panel">
              <div class="region-map-panel__meta">
                <span>{{ ui.mapHint }}</span>
                <el-button link type="primary" @click="clearCoordinate">{{ ui.clearCoordinate }}</el-button>
              </div>
              <CoordinateMapBoard
                interactive
                :height="320"
                :model-value="currentCoordinate"
                :empty-text="ui.mapEmpty"
                @update:model-value="handleCoordinatePick"
              />
            </div>
          </el-form-item>
          <el-form-item :label="ui.status" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio :value="1">{{ ui.enabled }}</el-radio>
              <el-radio :value="0">{{ ui.disabled }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="ui.remark">
            <el-input v-model="form.remark" :placeholder="ui.remarkPlaceholder" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">{{ ui.cancel }}</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ ui.save }}</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CoordinateMapBoard from '@/components/CoordinateMapBoard.vue'
import {
  createRegion,
  deleteRegion,
  fetchRegionDetail,
  fetchRegionPage,
  updateRegion,
  updateRegionStatus
} from '@/api/region'

const ui = {
  pageTitle: '\u533a\u57df\u7ba1\u7406',
  create: '\u65b0\u589e\u533a\u57df',
  regionName: '\u533a\u57df\u540d\u79f0',
  regionCode: '\u533a\u57df\u7f16\u7801',
  address: '\u8be6\u7ec6\u5730\u5740',
  coordinate: '\u5750\u6807',
  longitude: '\u7ecf\u5ea6',
  latitude: '\u7eac\u5ea6',
  status: '\u72b6\u6001',
  remark: '\u5907\u6ce8',
  createTime: '\u521b\u5efa\u65f6\u95f4',
  action: '\u64cd\u4f5c',
  search: '\u67e5\u8be2',
  reset: '\u91cd\u7f6e',
  edit: '\u7f16\u8f91',
  delete: '\u5220\u9664',
  save: '\u4fdd\u5b58',
  cancel: '\u53d6\u6d88',
  enabled: '\u542f\u7528',
  disabled: '\u505c\u7528',
  allStatus: '\u5168\u90e8\u72b6\u6001',
  regionNamePlaceholder: '\u8bf7\u8f93\u5165\u533a\u57df\u540d\u79f0',
  regionCodePlaceholder: '\u8bf7\u8f93\u5165\u533a\u57df\u7f16\u7801',
  addressPlaceholder: '\u8bf7\u8f93\u5165\u8be6\u7ec6\u5730\u5740',
  remarkPlaceholder: '\u8bf7\u8f93\u5165\u5907\u6ce8',
  mapPick: '\u5730\u56fe\u9009\u70b9',
  mapHint: '\u70b9\u51fb\u5730\u56fe\u53ef\u81ea\u52a8\u56de\u586b longitude / latitude',
  clearCoordinate: '\u6e05\u7a7a\u5750\u6807',
  mapEmpty: '\u70b9\u51fb\u5730\u56fe\u9009\u62e9\u533a\u57df\u7ecf\u7eac\u5ea6',
  createTitle: '\u65b0\u589e\u533a\u57df',
  editTitle: '\u7f16\u8f91\u533a\u57df',
  regionNameRequired: '\u8bf7\u8f93\u5165\u533a\u57df\u540d\u79f0',
  regionCodeRequired: '\u8bf7\u8f93\u5165\u533a\u57df\u7f16\u7801',
  addressRequired: '\u8bf7\u8f93\u5165\u8be6\u7ec6\u5730\u5740',
  statusRequired: '\u8bf7\u9009\u62e9\u72b6\u6001',
  loadError: '\u533a\u57df\u5217\u8868\u52a0\u8f7d\u5931\u8d25',
  detailError: '\u533a\u57df\u8be6\u60c5\u52a0\u8f7d\u5931\u8d25',
  createSuccess: '\u533a\u57df\u65b0\u589e\u6210\u529f',
  updateSuccess: '\u533a\u57df\u4fee\u6539\u6210\u529f',
  saveError: '\u4fdd\u5b58\u5931\u8d25',
  deleteConfirm: '\u5220\u9664\u540e\u65e0\u6cd5\u6062\u590d\uff0c\u662f\u5426\u7ee7\u7eed\uff1f',
  deleteTitle: '\u5220\u9664\u786e\u8ba4',
  deleteSuccess: '\u533a\u57df\u5220\u9664\u6210\u529f',
  deleteError: '\u5220\u9664\u5931\u8d25',
  statusSuccess: '\u533a\u57df\u72b6\u6001\u5df2\u66f4\u65b0',
  statusError: '\u72b6\u6001\u66f4\u65b0\u5931\u8d25',
  between: '\u5fc5\u987b\u5728'
}

const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const tableData = ref([])
const searchForm = reactive({
  regionName: '',
  regionCode: '',
  status: undefined
})
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
const dialog = reactive({
  visible: false,
  mode: 'create',
  id: null
})
const form = reactive(createDefaultForm())

const currentCoordinate = computed(() => ({
  longitude: form.longitude,
  latitude: form.latitude
}))

const dialogTitle = computed(() => (dialog.mode === 'create' ? ui.createTitle : ui.editTitle))

const rules = {
  regionName: [{ required: true, message: ui.regionNameRequired, trigger: 'blur' }],
  regionCode: [{ required: true, message: ui.regionCodeRequired, trigger: 'blur' }],
  address: [{ required: true, message: ui.addressRequired, trigger: 'blur' }],
  longitude: [{ validator: (_, value, callback) => validateCoordinate(value, -180, 180, ui.longitude, callback), trigger: 'change' }],
  latitude: [{ validator: (_, value, callback) => validateCoordinate(value, -90, 90, ui.latitude, callback), trigger: 'change' }],
  status: [{ required: true, message: ui.statusRequired, trigger: 'change' }]
}

function createDefaultForm() {
  return {
    regionName: '',
    regionCode: '',
    address: '',
    longitude: null,
    latitude: null,
    status: 1,
    remark: ''
  }
}

function validateCoordinate(value, min, max, label, callback) {
  if (value == null || value === '') {
    callback()
    return
  }
  if (Number(value) < min || Number(value) > max) {
    callback(new Error(`${label}${ui.between} ${min} 到 ${max} 之间`))
    return
  }
  callback()
}

function resetForm() {
  Object.assign(form, createDefaultForm())
}

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchRegionPage({
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    tableData.value = data.records
    pagination.total = data.total
  } catch (error) {
    ElMessage.error(error.message || ui.loadError)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadData()
}

function handleReset() {
  searchForm.regionName = ''
  searchForm.regionCode = ''
  searchForm.status = undefined
  pagination.pageNum = 1
  loadData()
}

function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadData()
}

function openCreate() {
  dialog.visible = true
  dialog.mode = 'create'
  dialog.id = null
  resetForm()
  formRef.value?.clearValidate()
}

async function openEdit(id) {
  try {
    const { data } = await fetchRegionDetail(id)
    dialog.visible = true
    dialog.mode = 'edit'
    dialog.id = id
    Object.assign(form, {
      regionName: data.regionName,
      regionCode: data.regionCode,
      address: data.address,
      longitude: data.longitude,
      latitude: data.latitude,
      status: data.status,
      remark: data.remark || ''
    })
    formRef.value?.clearValidate()
  } catch (error) {
    ElMessage.error(error.message || ui.detailError)
  }
}

function handleCoordinatePick(coordinate) {
  form.longitude = coordinate.longitude
  form.latitude = coordinate.latitude
  formRef.value?.validateField?.('longitude').catch(() => {})
  formRef.value?.validateField?.('latitude').catch(() => {})
}

function clearCoordinate() {
  form.longitude = null
  form.latitude = null
  formRef.value?.clearValidate?.(['longitude', 'latitude'])
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (dialog.mode === 'create') {
      await createRegion(form)
      ElMessage.success(ui.createSuccess)
    } else {
      await updateRegion(dialog.id, form)
      ElMessage.success(ui.updateSuccess)
    }
    dialog.visible = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || ui.saveError)
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm(ui.deleteConfirm, ui.deleteTitle, {
      type: 'warning'
    })
    await deleteRegion(id)
    ElMessage.success(ui.deleteSuccess)
    if (tableData.value.length === 1 && pagination.pageNum > 1) {
      pagination.pageNum -= 1
    }
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || ui.deleteError)
    }
  }
}

async function handleStatusChange(row, value) {
  const previous = row.status
  row.status = value
  try {
    await updateRegionStatus(row.id, value)
    ElMessage.success(ui.statusSuccess)
  } catch (error) {
    row.status = previous
    ElMessage.error(error.message || ui.statusError)
  }
}

function formatCoordinate(row) {
  if (row.longitude == null || row.latitude == null) {
    return '--'
  }
  return `${Number(row.longitude).toFixed(6)}, ${Number(row.latitude).toFixed(6)}`
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.region-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.region-form-grid :deep(.el-form-item) {
  margin-bottom: 18px;
}

.region-form-grid__full {
  grid-column: 1 / -1;
}

.region-map-panel {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.region-map-panel__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #617884;
  font-size: 13px;
}

@media (max-width: 900px) {
  .region-form-grid {
    grid-template-columns: 1fr;
  }

  .region-map-panel__meta {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
