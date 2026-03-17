<template>
  <section class="crud-page">
    <div class="crud-card search-card">
      <div class="crud-card__header">
        <div>
          <p class="page-kicker">Return Management</p>
          <h3>归还管理</h3>
        </div>
        <el-button type="primary" @click="openCreate">新增归还记录</el-button>
      </div>

      <el-form :model="searchForm" inline label-width="80px" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="车辆关键字">
          <el-input v-model="searchForm.carKeyword" placeholder="车辆编号 / 车牌号" clearable />
        </el-form-item>
        <el-form-item label="用户关键字">
          <el-input v-model="searchForm.userKeyword" placeholder="用户名 / 姓名 / 手机号" clearable />
        </el-form-item>
        <el-form-item label="归还区域">
          <el-select v-model="searchForm.returnRegionId" placeholder="全部区域" clearable style="width: 180px">
            <el-option v-for="item in regionOptions" :key="item.id" :label="item.regionName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 160px">
            <el-option v-for="item in returnStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column label="用户信息" min-width="180">
          <template #default="scope">
            <div>{{ scope.row.username }} / {{ scope.row.realName || '未实名' }}</div>
            <div class="table-secondary">{{ scope.row.phone || '--' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="车辆信息" min-width="220">
          <template #default="scope">
            <div>{{ scope.row.carCode }} / {{ scope.row.licensePlate }}</div>
            <div class="table-secondary">{{ scope.row.brand }} {{ scope.row.model }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="returnRegionName" label="归还区域" min-width="140" />
        <el-table-column label="费用结算" min-width="170">
          <template #default="scope">
            <div>结算 ￥{{ scope.row.finalAmount }}</div>
            <div class="table-secondary">损坏 ￥{{ scope.row.damageCost }} / 逾期 ￥{{ scope.row.lateFee }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="160">
          <template #default="scope">
            <el-select :model-value="scope.row.status" size="small" @change="(value) => handleStatusChange(scope.row, value)">
              <el-option v-for="item in returnStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="processedByName" label="处理管理员" min-width="140" />
        <el-table-column prop="returnTime" label="归还时间" min-width="180" />
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

    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新增归还记录' : '编辑归还记录'" width="860px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="form-grid">
          <el-form-item label="订单" prop="rentalOrderId" class="full-row">
            <el-select v-model="form.rentalOrderId" placeholder="请选择订单" filterable style="width: 100%">
              <el-option v-for="item in orderOptions" :key="item.id" :label="item.displayName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="归还区域" prop="returnRegionId">
            <el-select v-model="form.returnRegionId" placeholder="请选择归还区域" style="width: 100%">
              <el-option v-for="item in regionOptions" :key="item.id" :label="item.regionName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="处理管理员">
            <el-select v-model="form.processedBy" placeholder="请选择管理员" clearable style="width: 100%">
              <el-option v-for="item in adminOptions" :key="item.id" :label="(item.realName || item.username) + ' / ' + item.username" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="归还时间" prop="returnTime">
            <el-date-picker v-model="form.returnTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择归还时间" style="width: 100%" />
          </el-form-item>
          <el-form-item label="归还状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择归还状态" style="width: 100%">
              <el-option v-for="item in returnStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="车辆状况" class="full-row">
            <el-input v-model="form.vehicleCondition" placeholder="请输入车辆状况说明" />
          </el-form-item>
          <el-form-item label="损坏费用" prop="damageCost">
            <el-input-number v-model="form.damageCost" :min="0" :precision="2" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="逾期费用" prop="lateFee">
            <el-input-number v-model="form.lateFee" :min="0" :precision="2" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="结算金额" prop="finalAmount">
            <el-input-number v-model="form.finalAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
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
import { createReturn, deleteReturn, fetchReturnDetail, fetchReturnFormOptions, fetchReturnPage, updateReturn, updateReturnStatus } from '@/api/returnRecord'

const returnStatusOptions = [
  { label: '待确认', value: 'PENDING' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '已结算', value: 'SETTLED' },
  { label: '已完成', value: 'COMPLETED' }
]

const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const tableData = ref([])
const orderOptions = ref([])
const regionOptions = ref([])
const adminOptions = ref([])
const searchForm = reactive({ orderNo: '', carKeyword: '', userKeyword: '', returnRegionId: undefined, status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const dialog = reactive({ visible: false, mode: 'create', id: null })
const form = reactive(createDefaultForm())

const rules = {
  rentalOrderId: [{ required: true, message: '请选择订单', trigger: 'change' }],
  returnRegionId: [{ required: true, message: '请选择归还区域', trigger: 'change' }],
  returnTime: [{ required: true, message: '请选择归还时间', trigger: 'change' }],
  status: [{ required: true, message: '请选择归还状态', trigger: 'change' }],
  damageCost: [{ required: true, message: '请输入损坏费用', trigger: 'change' }],
  lateFee: [{ required: true, message: '请输入逾期费用', trigger: 'change' }],
  finalAmount: [{ required: true, message: '请输入结算金额', trigger: 'change' }]
}

function createDefaultForm() {
  return {
    rentalOrderId: null,
    returnRegionId: null,
    status: 'PENDING',
    vehicleCondition: '',
    damageCost: 0,
    lateFee: 0,
    finalAmount: 0,
    processedBy: null,
    returnTime: '',
    remark: ''
  }
}

function resetForm() { Object.assign(form, createDefaultForm()) }

async function loadFormOptions() {
  try {
    const { data } = await fetchReturnFormOptions()
    orderOptions.value = data.orders
    regionOptions.value = data.regions
    adminOptions.value = data.admins
  } catch (error) {
    ElMessage.error(error.message || '归还表单选项加载失败')
  }
}

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchReturnPage({ ...searchForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize })
    tableData.value = data.records
    pagination.total = data.total
  } catch (error) {
    ElMessage.error(error.message || '归还记录加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() { pagination.pageNum = 1; loadData() }
function handleReset() {
  Object.assign(searchForm, { orderNo: '', carKeyword: '', userKeyword: '', returnRegionId: undefined, status: '' })
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
    const { data } = await fetchReturnDetail(id)
    dialog.visible = true
    dialog.mode = 'edit'
    dialog.id = id
    Object.assign(form, {
      rentalOrderId: data.rentalOrderId,
      returnRegionId: data.returnRegionId,
      status: data.status,
      vehicleCondition: data.vehicleCondition || '',
      damageCost: Number(data.damageCost ?? 0),
      lateFee: Number(data.lateFee ?? 0),
      finalAmount: Number(data.finalAmount ?? 0),
      processedBy: data.processedBy,
      returnTime: normalizeDateTime(data.returnTime),
      remark: data.remark || ''
    })
    formRef.value?.clearValidate()
  } catch (error) {
    ElMessage.error(error.message || '归还详情加载失败')
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (dialog.mode === 'create') {
      await createReturn(form)
      ElMessage.success('归还记录新增成功')
    } else {
      await updateReturn(dialog.id, form)
      ElMessage.success('归还记录修改成功')
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
    await deleteReturn(id)
    ElMessage.success('归还记录删除成功')
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
    await updateReturnStatus(row.id, value)
    ElMessage.success('归还状态已更新')
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