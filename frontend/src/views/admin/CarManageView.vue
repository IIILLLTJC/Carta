<template>
  <section class="crud-page">
    <div class="crud-card search-card">
      <div class="crud-card__header">
        <div>
          <p class="page-kicker">Vehicle Management</p>
          <h3>车辆信息管理</h3>
        </div>
        <el-button type="primary" @click="openCreate">新增车辆</el-button>
      </div>

      <el-form :model="searchForm" inline label-width="80px" class="search-form">
        <el-form-item label="车辆编号">
          <el-input v-model="searchForm.carCode" placeholder="请输入车辆编号" clearable />
        </el-form-item>
        <el-form-item label="车牌号">
          <el-input v-model="searchForm.licensePlate" placeholder="请输入车牌号" clearable />
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="searchForm.brand" placeholder="请输入品牌" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 160px">
            <el-option v-for="item in carStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属区域">
          <el-select v-model="searchForm.currentRegionId" placeholder="全部区域" clearable style="width: 180px">
            <el-option v-for="item in regionOptions" :key="item.id" :label="item.regionName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="crud-card table-card">
      <el-table v-loading="loading" :data="tableData" border class="crud-table">
        <el-table-column prop="carCode" label="车辆编号" min-width="120" />
        <el-table-column prop="licensePlate" label="车牌号" min-width="120" />
        <el-table-column label="车辆信息" min-width="220">
          <template #default="scope">
            <div>{{ scope.row.brand }} {{ scope.row.model }}</div>
            <div class="table-secondary">{{ scope.row.color || '--' }} / {{ scope.row.energyType || '--' }} / {{ scope.row.seatCount }} 座</div>
          </template>
        </el-table-column>
        <el-table-column prop="currentRegionName" label="所属区域" min-width="140" />
        <el-table-column label="租金 / 押金" min-width="160">
          <template #default="scope">
            <div>￥{{ scope.row.dailyRent }}</div>
            <div class="table-secondary">押金 ￥{{ scope.row.deposit }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="160">
          <template #default="scope">
            <el-select :model-value="scope.row.status" size="small" @change="(value) => handleStatusChange(scope.row, value)">
              <el-option v-for="item in carStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="里程 / 电量" min-width="150">
          <template #default="scope">
            <div>{{ scope.row.mileage ?? 0 }} km</div>
            <div class="table-secondary">{{ scope.row.batteryLevel ?? '--' }}%</div>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openEdit(scope.row.id)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
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

    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新增车辆' : '编辑车辆'" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="form-grid">
          <el-form-item label="车辆编号" prop="carCode">
            <el-input v-model="form.carCode" placeholder="请输入车辆编号" />
          </el-form-item>
          <el-form-item label="车牌号" prop="licensePlate">
            <el-input v-model="form.licensePlate" placeholder="请输入车牌号" />
          </el-form-item>
          <el-form-item label="品牌" prop="brand">
            <el-input v-model="form.brand" placeholder="请输入品牌" />
          </el-form-item>
          <el-form-item label="型号" prop="model">
            <el-input v-model="form.model" placeholder="请输入型号" />
          </el-form-item>
          <el-form-item label="颜色">
            <el-input v-model="form.color" placeholder="请输入颜色" />
          </el-form-item>
          <el-form-item label="座位数" prop="seatCount">
            <el-input-number v-model="form.seatCount" :min="1" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="能源类型">
            <el-input v-model="form.energyType" placeholder="例如 EV / 油电混合" />
          </el-form-item>
          <el-form-item label="所属区域">
            <el-select v-model="form.currentRegionId" placeholder="请选择区域" clearable style="width: 100%">
              <el-option v-for="item in regionOptions" :key="item.id" :label="item.regionName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="日租金" prop="dailyRent">
            <el-input-number v-model="form.dailyRent" :min="0" :precision="2" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="押金" prop="deposit">
            <el-input-number v-model="form.deposit" :min="0" :precision="2" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="车辆状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
              <el-option v-for="item in carStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="里程">
            <el-input-number v-model="form.mileage" :min="0" :precision="2" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="电量">
            <el-input-number v-model="form.batteryLevel" :min="0" :max="100" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="备注" class="full-row">
            <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createCar, deleteCar, fetchCarDetail, fetchCarPage, updateCar, updateCarStatus } from '@/api/car'
import { fetchRegionOptions } from '@/api/region'

const carStatusOptions = [
  { label: '空闲', value: 'IDLE' },
  { label: '已投放', value: 'DEPLOYED' },
  { label: '租赁中', value: 'RENTING' },
  { label: '待归还确认', value: 'RETURN_PENDING' },
  { label: '已回收', value: 'RECOVERED' },
  { label: '维修中', value: 'MAINTENANCE' }
]

const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const tableData = ref([])
const regionOptions = ref([])
const searchForm = reactive({
  carCode: '',
  licensePlate: '',
  brand: '',
  status: '',
  currentRegionId: undefined
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

const rules = {
  carCode: [{ required: true, message: '请输入车辆编号', trigger: 'blur' }],
  licensePlate: [{ required: true, message: '请输入车牌号', trigger: 'blur' }],
  brand: [{ required: true, message: '请输入品牌', trigger: 'blur' }],
  model: [{ required: true, message: '请输入型号', trigger: 'blur' }],
  seatCount: [{ required: true, message: '请输入座位数', trigger: 'change' }],
  dailyRent: [{ required: true, message: '请输入日租金', trigger: 'change' }],
  deposit: [{ required: true, message: '请输入押金', trigger: 'change' }],
  status: [{ required: true, message: '请选择车辆状态', trigger: 'change' }]
}

function createDefaultForm() {
  return {
    carCode: '',
    licensePlate: '',
    brand: '',
    model: '',
    color: '',
    seatCount: 5,
    energyType: '',
    dailyRent: 0,
    deposit: 0,
    currentRegionId: null,
    status: 'IDLE',
    mileage: 0,
    batteryLevel: null,
    remark: ''
  }
}

function resetForm() {
  Object.assign(form, createDefaultForm())
}

async function loadRegionOptions() {
  try {
    const { data } = await fetchRegionOptions()
    regionOptions.value = data
  } catch (error) {
    ElMessage.error(error.message || '区域选项加载失败')
  }
}

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchCarPage({
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    tableData.value = data.records
    pagination.total = data.total
  } catch (error) {
    ElMessage.error(error.message || '车辆列表加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadData()
}

function handleReset() {
  Object.assign(searchForm, {
    carCode: '',
    licensePlate: '',
    brand: '',
    status: '',
    currentRegionId: undefined
  })
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
    const { data } = await fetchCarDetail(id)
    dialog.visible = true
    dialog.mode = 'edit'
    dialog.id = id
    Object.assign(form, {
      carCode: data.carCode,
      licensePlate: data.licensePlate,
      brand: data.brand,
      model: data.model,
      color: data.color || '',
      seatCount: data.seatCount,
      energyType: data.energyType || '',
      dailyRent: Number(data.dailyRent ?? 0),
      deposit: Number(data.deposit ?? 0),
      currentRegionId: data.currentRegionId,
      status: data.status,
      mileage: Number(data.mileage ?? 0),
      batteryLevel: data.batteryLevel,
      remark: data.remark || ''
    })
    formRef.value?.clearValidate()
  } catch (error) {
    ElMessage.error(error.message || '车辆详情加载失败')
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (dialog.mode === 'create') {
      await createCar(form)
      ElMessage.success('车辆新增成功')
    } else {
      await updateCar(dialog.id, form)
      ElMessage.success('车辆修改成功')
    }
    dialog.visible = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('删除后无法恢复，是否继续？', '删除确认', { type: 'warning' })
    await deleteCar(id)
    ElMessage.success('车辆删除成功')
    if (tableData.value.length === 1 && pagination.pageNum > 1) {
      pagination.pageNum -= 1
    }
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

async function handleStatusChange(row, value) {
  const previous = row.status
  row.status = value
  try {
    await updateCarStatus(row.id, value)
    ElMessage.success('车辆状态已更新')
  } catch (error) {
    row.status = previous
    ElMessage.error(error.message || '状态更新失败')
  }
}

onMounted(async () => {
  await loadRegionOptions()
  loadData()
})
</script>