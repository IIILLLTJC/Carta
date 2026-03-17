<template>
  <section class="crud-page">
    <div class="crud-card search-card">
      <div class="crud-card__header">
        <div>
          <p class="page-kicker">Recovery Management</p>
          <h3>车辆回收管理</h3>
        </div>
        <el-button type="primary" @click="openCreate">新增回收记录</el-button>
      </div>

      <el-form :model="searchForm" inline label-width="80px" class="search-form">
        <el-form-item label="车辆关键字">
          <el-input v-model="searchForm.carKeyword" placeholder="车辆编号 / 车牌号" clearable />
        </el-form-item>
        <el-form-item label="所属区域">
          <el-select v-model="searchForm.regionId" placeholder="全部区域" clearable style="width: 180px">
            <el-option v-for="item in regionOptions" :key="item.id" :label="item.regionName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 160px">
            <el-option v-for="item in recoveryStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="管理员">
          <el-select v-model="searchForm.operatorAdminId" placeholder="全部管理员" clearable style="width: 180px">
            <el-option v-for="item in adminOptions" :key="item.id" :label="item.realName || item.username" :value="item.id" />
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
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="车辆信息" min-width="220">
          <template #default="scope">
            <div>{{ scope.row.carCode }} / {{ scope.row.licensePlate }}</div>
            <div class="table-secondary">{{ scope.row.brand }} {{ scope.row.model }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="regionName" label="区域信息" min-width="140" />
        <el-table-column prop="operatorAdminName" label="操作管理员" min-width="140" />
        <el-table-column prop="recoveryReason" label="回收原因" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="160">
          <template #default="scope">
            <el-select :model-value="scope.row.status" size="small" @change="(value) => handleStatusChange(scope.row, value)">
              <el-option v-for="item in recoveryStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="recoveryTime" label="回收时间" min-width="180" />
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

    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新增回收记录' : '编辑回收记录'" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="form-grid">
          <el-form-item label="车辆" prop="carId" class="full-row">
            <el-select v-model="form.carId" placeholder="请选择车辆" filterable style="width: 100%">
              <el-option v-for="item in carOptions" :key="item.id" :label="item.displayName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="所属区域">
            <el-select v-model="form.regionId" placeholder="请选择区域" clearable style="width: 100%">
              <el-option v-for="item in regionOptions" :key="item.id" :label="item.regionName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="操作管理员" prop="operatorAdminId">
            <el-select v-model="form.operatorAdminId" placeholder="请选择管理员" style="width: 100%">
              <el-option v-for="item in adminOptions" :key="item.id" :label="(item.realName || item.username) + ' / ' + item.username" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="回收时间" prop="recoveryTime">
            <el-date-picker v-model="form.recoveryTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择回收时间" style="width: 100%" />
          </el-form-item>
          <el-form-item label="回收状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择回收状态" style="width: 100%">
              <el-option v-for="item in recoveryStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="回收原因" class="full-row">
            <el-input v-model="form.recoveryReason" type="textarea" :rows="3" placeholder="请输入回收原因" />
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
import { createRecovery, deleteRecovery, fetchRecoveryDetail, fetchRecoveryFormOptions, fetchRecoveryPage, updateRecovery, updateRecoveryStatus } from '@/api/recovery'

const recoveryStatusOptions = [
  { label: '待回收', value: 'PENDING' },
  { label: '已回收', value: 'RECOVERED' },
  { label: '维修中', value: 'MAINTENANCE' },
  { label: '已取消', value: 'CANCELLED' }
]

const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const tableData = ref([])
const carOptions = ref([])
const regionOptions = ref([])
const adminOptions = ref([])
const searchForm = reactive({ carKeyword: '', regionId: undefined, status: '', operatorAdminId: undefined })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const dialog = reactive({ visible: false, mode: 'create', id: null })
const form = reactive(createDefaultForm())

const rules = {
  carId: [{ required: true, message: '请选择车辆', trigger: 'change' }],
  operatorAdminId: [{ required: true, message: '请选择操作管理员', trigger: 'change' }],
  recoveryTime: [{ required: true, message: '请选择回收时间', trigger: 'change' }],
  status: [{ required: true, message: '请选择回收状态', trigger: 'change' }]
}

function createDefaultForm() {
  return {
    carId: null,
    regionId: null,
    operatorAdminId: null,
    recoveryTime: '',
    recoveryReason: '',
    status: 'PENDING',
    remark: ''
  }
}

function resetForm() { Object.assign(form, createDefaultForm()) }

async function loadFormOptions() {
  try {
    const { data } = await fetchRecoveryFormOptions()
    carOptions.value = data.cars
    regionOptions.value = data.regions
    adminOptions.value = data.admins
  } catch (error) {
    ElMessage.error(error.message || '回收表单选项加载失败')
  }
}

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchRecoveryPage({ ...searchForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize })
    tableData.value = data.records
    pagination.total = data.total
  } catch (error) {
    ElMessage.error(error.message || '回收记录加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() { pagination.pageNum = 1; loadData() }
function handleReset() {
  Object.assign(searchForm, { carKeyword: '', regionId: undefined, status: '', operatorAdminId: undefined })
  pagination.pageNum = 1
  loadData()
}
function handleSizeChange(size) { pagination.pageSize = size; pagination.pageNum = 1; loadData() }

function openCreate() {
  dialog.visible = true
  dialog.mode = 'create'
  dialog.id = null
  resetForm()
  formRef.value?.clearValidate()
}

async function openEdit(id) {
  try {
    const { data } = await fetchRecoveryDetail(id)
    dialog.visible = true
    dialog.mode = 'edit'
    dialog.id = id
    Object.assign(form, {
      carId: data.carId,
      regionId: data.regionId,
      operatorAdminId: data.operatorAdminId,
      recoveryTime: normalizeDateTime(data.recoveryTime),
      recoveryReason: data.recoveryReason || '',
      status: data.status,
      remark: data.remark || ''
    })
    formRef.value?.clearValidate()
  } catch (error) {
    ElMessage.error(error.message || '回收详情加载失败')
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (dialog.mode === 'create') {
      await createRecovery(form)
      ElMessage.success('回收记录新增成功')
    } else {
      await updateRecovery(dialog.id, form)
      ElMessage.success('回收记录修改成功')
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
    await deleteRecovery(id)
    ElMessage.success('回收记录删除成功')
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
    await updateRecoveryStatus(row.id, value)
    ElMessage.success('回收状态已更新')
  } catch (error) {
    row.status = previous
    ElMessage.error(error.message || '状态更新失败')
  }
}

function normalizeDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : ''
}

onMounted(async () => {
  await loadFormOptions()
  loadData()
})
</script>