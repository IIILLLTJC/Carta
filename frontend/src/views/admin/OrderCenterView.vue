<template>
  <section class="crud-page">
    <div class="crud-card search-card">
      <div class="crud-card__header">
        <div>
          <p class="page-kicker">Order Center</p>
          <h3>订单中心</h3>
        </div>
        <el-button type="primary" @click="openCreate">新增订单</el-button>
      </div>

      <el-form :model="searchForm" inline label-width="80px" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="用户关键字">
          <el-input v-model="searchForm.userKeyword" placeholder="用户名 / 姓名 / 手机号" clearable />
        </el-form-item>
        <el-form-item label="车辆关键字">
          <el-input v-model="searchForm.carKeyword" placeholder="车辆编号 / 车牌号" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.orderStatus" placeholder="全部状态" clearable style="width: 160px">
            <el-option v-for="item in orderStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="searchForm.paymentStatus" placeholder="全部状态" clearable style="width: 140px">
            <el-option v-for="item in paymentStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
        <el-table-column label="取还车区域" min-width="200">
          <template #default="scope">
            <div>取车：{{ scope.row.pickupRegionName || '--' }}</div>
            <div class="table-secondary">还车：{{ scope.row.returnRegionName || '--' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="金额信息" min-width="150">
          <template #default="scope">
            <div>订单 ￥{{ scope.row.orderAmount }}</div>
            <div class="table-secondary">押金 ￥{{ scope.row.depositAmount }}</div>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="160">
          <template #default="scope">
            <el-select :model-value="scope.row.orderStatus" size="small" @change="(value) => handleStatusChange(scope.row, value)">
              <el-option v-for="item in orderStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="支付状态" width="120">
          <template #default="scope">
            <el-tag :type="paymentTagType(scope.row.paymentStatus)">{{ paymentLabel(scope.row.paymentStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" min-width="180" />
        <el-table-column prop="expectedReturnTime" label="预计归还" min-width="180" />
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

    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新增订单' : '编辑订单'" width="860px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="form-grid">
          <el-form-item v-if="dialog.mode === 'edit'" label="订单号" class="full-row">
            <el-input :model-value="dialog.orderNo" disabled />
          </el-form-item>
          <el-form-item label="用户" prop="userId">
            <el-select v-model="form.userId" placeholder="请选择用户" filterable style="width: 100%">
              <el-option v-for="item in userOptions" :key="item.id" :label="item.displayName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="车辆" prop="carId">
            <el-select v-model="form.carId" placeholder="请选择车辆" filterable style="width: 100%">
              <el-option v-for="item in carOptions" :key="item.id" :label="item.displayName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="取车区域" prop="pickupRegionId">
            <el-select v-model="form.pickupRegionId" placeholder="请选择取车区域" style="width: 100%">
              <el-option v-for="item in regionOptions" :key="item.id" :label="item.regionName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="还车区域">
            <el-select v-model="form.returnRegionId" placeholder="请选择还车区域" clearable style="width: 100%">
              <el-option v-for="item in regionOptions" :key="item.id" :label="item.regionName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="订单金额" prop="orderAmount">
            <el-input-number v-model="form.orderAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="押金金额" prop="depositAmount">
            <el-input-number v-model="form.depositAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择开始时间" style="width: 100%" />
          </el-form-item>
          <el-form-item label="预计归还" prop="expectedReturnTime">
            <el-date-picker v-model="form.expectedReturnTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择预计归还时间" style="width: 100%" />
          </el-form-item>
          <el-form-item label="实际归还">
            <el-date-picker v-model="form.actualReturnTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择实际归还时间" clearable style="width: 100%" />
          </el-form-item>
          <el-form-item label="订单状态" prop="orderStatus">
            <el-select v-model="form.orderStatus" placeholder="请选择订单状态" style="width: 100%">
              <el-option v-for="item in orderStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="支付状态" prop="paymentStatus">
            <el-select v-model="form.paymentStatus" placeholder="请选择支付状态" style="width: 100%">
              <el-option v-for="item in paymentStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
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
import { createOrder, deleteOrder, fetchOrderDetail, fetchOrderFormOptions, fetchOrderPage, updateOrder, updateOrderStatus } from '@/api/order'

const orderStatusOptions = [
  { label: '已创建', value: 'CREATED' },
  { label: '已支付', value: 'PAID' },
  { label: '使用中', value: 'USING' },
  { label: '归还中', value: 'RETURNING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

const paymentStatusOptions = [
  { label: '未支付', value: 'UNPAID' },
  { label: '已支付', value: 'PAID' },
  { label: '已退款', value: 'REFUNDED' }
]

const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const tableData = ref([])
const userOptions = ref([])
const carOptions = ref([])
const regionOptions = ref([])
const searchForm = reactive({
  orderNo: '',
  userKeyword: '',
  carKeyword: '',
  orderStatus: '',
  paymentStatus: ''
})
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const dialog = reactive({ visible: false, mode: 'create', id: null, orderNo: '' })
const form = reactive(createDefaultForm())

const rules = {
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  carId: [{ required: true, message: '请选择车辆', trigger: 'change' }],
  pickupRegionId: [{ required: true, message: '请选择取车区域', trigger: 'change' }],
  orderAmount: [{ required: true, message: '请输入订单金额', trigger: 'change' }],
  depositAmount: [{ required: true, message: '请输入押金金额', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  expectedReturnTime: [{ required: true, message: '请选择预计归还时间', trigger: 'change' }],
  orderStatus: [{ required: true, message: '请选择订单状态', trigger: 'change' }],
  paymentStatus: [{ required: true, message: '请选择支付状态', trigger: 'change' }]
}

function createDefaultForm() {
  return {
    userId: null,
    carId: null,
    pickupRegionId: null,
    returnRegionId: null,
    orderStatus: 'CREATED',
    orderAmount: 0,
    depositAmount: 0,
    startTime: '',
    expectedReturnTime: '',
    actualReturnTime: '',
    paymentStatus: 'UNPAID',
    remark: ''
  }
}

function resetForm() { Object.assign(form, createDefaultForm()) }

async function loadFormOptions() {
  try {
    const { data } = await fetchOrderFormOptions()
    userOptions.value = data.users
    carOptions.value = data.cars
    regionOptions.value = data.regions
  } catch (error) {
    ElMessage.error(error.message || '订单表单选项加载失败')
  }
}

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchOrderPage({ ...searchForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize })
    tableData.value = data.records
    pagination.total = data.total
  } catch (error) {
    ElMessage.error(error.message || '订单列表加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() { pagination.pageNum = 1; loadData() }
function handleReset() {
  Object.assign(searchForm, { orderNo: '', userKeyword: '', carKeyword: '', orderStatus: '', paymentStatus: '' })
  pagination.pageNum = 1
  loadData()
}
function handleSizeChange(size) { pagination.pageSize = size; pagination.pageNum = 1; loadData() }

function openCreate() {
  dialog.visible = true
  dialog.mode = 'create'
  dialog.id = null
  dialog.orderNo = ''
  resetForm()
  formRef.value?.clearValidate()
}

async function openEdit(id) {
  try {
    const { data } = await fetchOrderDetail(id)
    dialog.visible = true
    dialog.mode = 'edit'
    dialog.id = id
    dialog.orderNo = data.orderNo
    Object.assign(form, {
      userId: data.userId,
      carId: data.carId,
      pickupRegionId: data.pickupRegionId,
      returnRegionId: data.returnRegionId,
      orderStatus: data.orderStatus,
      orderAmount: Number(data.orderAmount ?? 0),
      depositAmount: Number(data.depositAmount ?? 0),
      startTime: normalizeDateTime(data.startTime),
      expectedReturnTime: normalizeDateTime(data.expectedReturnTime),
      actualReturnTime: normalizeDateTime(data.actualReturnTime),
      paymentStatus: data.paymentStatus,
      remark: data.remark || ''
    })
    formRef.value?.clearValidate()
  } catch (error) {
    ElMessage.error(error.message || '订单详情加载失败')
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (dialog.mode === 'create') {
      await createOrder(form)
      ElMessage.success('订单新增成功')
    } else {
      await updateOrder(dialog.id, form)
      ElMessage.success('订单修改成功')
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
    await deleteOrder(id)
    ElMessage.success('订单删除成功')
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
  const previous = row.orderStatus
  row.orderStatus = value
  try {
    await updateOrderStatus(row.id, value)
    ElMessage.success('订单状态已更新')
  } catch (error) {
    row.orderStatus = previous
    ElMessage.error(error.message || '状态更新失败')
  }
}

function normalizeDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : ''
}

function paymentLabel(value) {
  return paymentStatusOptions.find((item) => item.value === value)?.label || value
}

function paymentTagType(value) {
  if (value === 'PAID') return 'success'
  if (value === 'REFUNDED') return 'warning'
  return 'info'
}

onMounted(async () => {
  await loadFormOptions()
  loadData()
})
</script>