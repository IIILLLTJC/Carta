<template>
  <section class="module-card">
    <div class="module-card__head">
      <div>
        <p class="page-kicker">User Returns</p>
        <h3>归还管理</h3>
      </div>
      <el-button type="primary" @click="openCreateDialog">提交归还申请</el-button>
    </div>

    <el-form :inline="true" :model="queryForm" class="search-form">
      <el-form-item label="订单号">
        <el-input v-model="queryForm.orderNo" placeholder="请输入订单号" clearable />
      </el-form-item>
      <el-form-item label="归还状态">
        <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 160px">
          <el-option v-for="item in returnStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" stripe>
      <el-table-column prop="orderNo" label="订单号" min-width="180" />
      <el-table-column label="车辆信息" min-width="220">
        <template #default="scope">
          <div>{{ scope.row.carCode || '-' }}</div>
          <div class="table-sub-copy">{{ scope.row.licensePlate || '-' }} / {{ scope.row.brand || '-' }} {{ scope.row.model || '' }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="returnRegionName" label="归还区域" min-width="140" />
      <el-table-column prop="vehicleCondition" label="自报情况" min-width="160" />
      <el-table-column label="费用结算" min-width="180">
        <template #default="scope">
          <div>损坏: {{ formatMoney(scope.row.damageCost) }}</div>
          <div>逾期: {{ formatMoney(scope.row.lateFee) }}</div>
          <div>结算: {{ formatMoney(scope.row.finalAmount) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="状态" min-width="120">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="returnTime" label="归还时间" min-width="180" />
      <el-table-column label="备注" min-width="180">
        <template #default="scope">{{ scope.row.remark || '-' }}</template>
      </el-table-column>
    </el-table>

    <div class="table-pagination">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="pagination.total"
        :current-page="pagination.pageNum"
        :page-size="pagination.pageSize"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog v-model="dialogVisible" title="提交归还申请" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="formModel" :rules="rules" label-width="130px">
        <el-form-item label="租赁订单" prop="rentalOrderId">
          <el-select v-model="formModel.rentalOrderId" placeholder="请选择租赁订单" filterable style="width: 100%" @change="handleOrderChange">
            <el-option
              v-for="item in orderOptions"
              :key="item.id"
              :label="`${item.orderNo} / ${item.carCode || '-'} / ${item.licensePlate || '-'}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="归还区域" prop="returnRegionId">
          <el-select v-model="formModel.returnRegionId" placeholder="请选择归还区域" filterable style="width: 100%">
            <el-option v-for="item in regionOptions" :key="item.id" :label="`${item.regionName} (${item.regionCode})`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="车辆自报情况" prop="vehicleCondition">
          <el-select v-model="formModel.vehicleCondition" placeholder="请选择车辆自报情况" style="width: 100%">
            <el-option label="无明显异常" value="无明显异常" />
            <el-option label="轻微异常，待验车确认" value="轻微异常，待验车确认" />
            <el-option label="存在明显异常，待验车确认" value="存在明显异常，待验车确认" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单信息">
          <div class="dialog-tip" v-if="selectedOrder">
            <div>车辆：{{ selectedOrder.carCode || '-' }} / {{ selectedOrder.licensePlate || '-' }}</div>
            <div>取车区域：{{ selectedOrder.pickupRegionName || '-' }}</div>
            <div>预计归还时间：{{ selectedOrder.expectedReturnTime || '-' }}</div>
          </div>
          <span v-else class="table-sub-copy">请选择订单后查看关联信息</span>
        </el-form-item>
        <el-form-item label="异常说明 / 备注">
          <el-input v-model="formModel.remark" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="请输入用户自报的异常说明或归还备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交归还</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createUserReturn, fetchUserReturnFormOptions, fetchUserReturnPage } from '../../api/userReturn'

const returnStatusOptions = [
  { label: '待确认', value: 'PENDING' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '已结算', value: 'SETTLED' },
  { label: '已完成', value: 'COMPLETED' },
]

const queryForm = reactive({
  orderNo: '',
  status: '',
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const orderOptions = ref([])
const regionOptions = ref([])

const formModel = reactive({
  rentalOrderId: undefined,
  returnRegionId: undefined,
  vehicleCondition: '',
  remark: '',
})

const rules = {
  rentalOrderId: [{ required: true, message: '请选择租赁订单', trigger: 'change' }],
  returnRegionId: [{ required: true, message: '请选择归还区域', trigger: 'change' }],
  vehicleCondition: [{ required: true, message: '请选择车辆自报情况', trigger: 'change' }],
}

const selectedOrder = computed(() => orderOptions.value.find((item) => item.id === formModel.rentalOrderId))

async function loadData() {
  loading.value = true
  try {
    const { data: pageData } = await fetchUserReturnPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...queryForm,
    })
    tableData.value = pageData?.records || []
    pagination.total = Number(pageData?.total || 0)
    pagination.pageNum = Number(pageData?.pageNum || pagination.pageNum)
    pagination.pageSize = Number(pageData?.pageSize || pagination.pageSize)
  } catch (error) {
    ElMessage.error(error.message || '归还记录加载失败')
  } finally {
    loading.value = false
  }
}

async function loadFormOptions() {
  const { data: payload } = await fetchUserReturnFormOptions()
  orderOptions.value = payload?.orders || []
  regionOptions.value = payload?.regions || []
}

async function openCreateDialog() {
  try {
    await loadFormOptions()
    resetForm()
    dialogVisible.value = true
    if (!orderOptions.value.length) {
      ElMessage.warning('当前没有可归还订单')
    }
  } catch (error) {
    ElMessage.error(error.message || '归还表单选项加载失败')
  }
}

function handleOrderChange(orderId) {
  const target = orderOptions.value.find((item) => item.id === orderId)
  if (target?.returnRegionId) {
    formModel.returnRegionId = target.returnRegionId
  }
}

function resetForm() {
  formModel.rentalOrderId = undefined
  formModel.returnRegionId = undefined
  formModel.vehicleCondition = ''
  formModel.remark = ''
  formRef.value?.clearValidate()
}

function handleSearch() {
  pagination.pageNum = 1
  loadData()
}

function handleReset() {
  queryForm.orderNo = ''
  queryForm.status = ''
  pagination.pageNum = 1
  loadData()
}

function handlePageChange(page) {
  pagination.pageNum = page
  loadData()
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await createUserReturn({ ...formModel })
    ElMessage.success('归还申请提交成功')
    dialogVisible.value = false
    await Promise.all([loadData(), loadFormOptions()])
  } catch (error) {
    ElMessage.error(error.message || '归还申请提交失败')
  } finally {
    submitLoading.value = false
  }
}

function statusLabel(status) {
  return returnStatusOptions.find((item) => item.value === status)?.label || status || '-'
}

function statusTagType(status) {
  if (status === 'COMPLETED') return 'success'
  if (status === 'SETTLED') return 'warning'
  if (status === 'CONFIRMED') return 'info'
  return ''
}

function formatMoney(value) {
  if (value === null || value === undefined || value === '') {
    return '￥0.00'
  }
  return `￥${Number(value).toFixed(2)}`
}

onMounted(() => {
  loadData()
  loadFormOptions().catch(() => {
    orderOptions.value = []
    regionOptions.value = []
  })
})
</script>