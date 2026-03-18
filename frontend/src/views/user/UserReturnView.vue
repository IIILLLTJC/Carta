<template>
  <section class="module-card">
    <div class="module-card__head">
      <div>
        <p class="page-kicker">User Returns</p>
        <h3>{{ ui.pageTitle }}</h3>
      </div>
      <el-button type="primary" @click="openCreateDialog">{{ ui.createButton }}</el-button>
    </div>

    <el-form :inline="true" :model="queryForm" class="search-form">
      <el-form-item :label="ui.orderNoLabel">
        <el-input v-model="queryForm.orderNo" :placeholder="ui.orderNoPlaceholder" clearable />
      </el-form-item>
      <el-form-item :label="ui.statusLabel">
        <el-select v-model="queryForm.status" :placeholder="ui.statusAllPlaceholder" clearable style="width: 160px">
          <el-option v-for="item in returnStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">{{ ui.searchButton }}</el-button>
        <el-button @click="handleReset">{{ ui.resetButton }}</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" stripe>
      <el-table-column prop="orderNo" :label="ui.orderNoLabel" min-width="180" />
      <el-table-column :label="ui.carInfoLabel" min-width="220">
        <template #default="scope">
          <div>{{ scope.row.carCode || '-' }}</div>
          <div class="table-sub-copy">{{ scope.row.licensePlate || '-' }} / {{ scope.row.brand || '-' }} {{ scope.row.model || '' }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="returnRegionName" :label="ui.returnRegionLabel" min-width="140" />
      <el-table-column :label="ui.timeInfoLabel" min-width="220">
        <template #default="scope">
          <div>{{ ui.expectedReturnTimeLabel }}: {{ formatDateTime(scope.row.expectedReturnTime) }}</div>
          <div class="table-sub-copy">{{ ui.actualReturnTimeLabel }}: {{ formatDateTime(scope.row.actualReturnTime || scope.row.returnTime) }}</div>
        </template>
      </el-table-column>
      <el-table-column :label="ui.settlementLabel" min-width="220">
        <template #default="scope">
          <div>{{ ui.baseRentLabel }}: {{ formatMoney(scope.row.orderAmount) }}</div>
          <div>{{ ui.depositLabel }}: {{ formatMoney(scope.row.depositAmount) }}</div>
          <div class="table-sub-copy">{{ ui.lateFeeLabel }}: {{ formatMoney(scope.row.lateFee) }} / {{ ui.damageCostLabel }}: {{ formatMoney(scope.row.damageCost) }}</div>
          <div class="table-sub-copy">{{ ui.finalAmountLabel }}: {{ formatMoney(scope.row.finalAmount) }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="vehicleCondition" :label="ui.vehicleConditionLabel" min-width="180" />
      <el-table-column :label="ui.statusLabel" min-width="120">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="ui.remarkLabel" min-width="180">
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

    <el-dialog v-model="dialogVisible" :title="ui.dialogTitle" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="formModel" :rules="rules" label-width="130px">
        <el-form-item :label="ui.orderSelectLabel" prop="rentalOrderId">
          <el-select v-model="formModel.rentalOrderId" :placeholder="ui.orderSelectPlaceholder" filterable style="width: 100%" @change="handleOrderChange">
            <el-option
              v-for="item in orderOptions"
              :key="item.id"
              :label="`${item.orderNo} / ${item.carCode || '-'} / ${item.licensePlate || '-'}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="ui.returnRegionLabel" prop="returnRegionId">
          <el-select v-model="formModel.returnRegionId" :placeholder="ui.returnRegionPlaceholder" filterable style="width: 100%">
            <el-option v-for="item in regionOptions" :key="item.id" :label="`${item.regionName} (${item.regionCode})`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="ui.vehicleConditionLabel" prop="vehicleCondition">
          <el-select v-model="formModel.vehicleCondition" :placeholder="ui.vehicleConditionPlaceholder" style="width: 100%">
            <el-option v-for="item in vehicleConditionOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="ui.orderInfoLabel">
          <div class="dialog-tip" v-if="selectedOrder">
            <div>{{ ui.carLabel }}: {{ selectedOrder.carCode || '-' }} / {{ selectedOrder.licensePlate || '-' }}</div>
            <div>{{ ui.pickupRegionLabel }}: {{ selectedOrder.pickupRegionName || '-' }}</div>
            <div>{{ ui.expectedReturnTimeLabel }}: {{ formatDateTime(selectedOrder.expectedReturnTime) }}</div>
          </div>
          <span v-else class="table-sub-copy">{{ ui.orderInfoEmpty }}</span>
        </el-form-item>
        <el-form-item :label="ui.remarkInputLabel">
          <el-input v-model="formModel.remark" type="textarea" :rows="3" maxlength="200" show-word-limit :placeholder="ui.remarkPlaceholder" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ ui.cancelButton }}</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">{{ ui.submitButton }}</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createUserReturn, fetchUserReturnFormOptions, fetchUserReturnPage } from '../../api/userReturn'

const ui = {
  pageTitle: '归还管理',
  createButton: '提交归还申请',
  orderNoLabel: '订单号',
  orderNoPlaceholder: '请输入订单号',
  statusLabel: '归还状态',
  statusAllPlaceholder: '全部状态',
  searchButton: '查询',
  resetButton: '重置',
  carInfoLabel: '车辆信息',
  returnRegionLabel: '归还区域',
  timeInfoLabel: '时间信息',
  expectedReturnTimeLabel: '预计归还',
  actualReturnTimeLabel: '实际归还',
  settlementLabel: '结算明细',
  baseRentLabel: '订单金额',
  depositLabel: '押金',
  lateFeeLabel: '逾期费',
  damageCostLabel: '损坏费',
  finalAmountLabel: '最终结算',
  vehicleConditionLabel: '车辆自报情况',
  remarkLabel: '备注',
  dialogTitle: '提交归还申请',
  orderSelectLabel: '租赁订单',
  orderSelectPlaceholder: '请选择租赁订单',
  returnRegionPlaceholder: '请选择归还区域',
  vehicleConditionPlaceholder: '请选择车辆自报情况',
  orderInfoLabel: '订单信息',
  carLabel: '车辆',
  pickupRegionLabel: '取车区域',
  orderInfoEmpty: '请选择订单后查看关联信息',
  remarkInputLabel: '异常说明 / 备注',
  remarkPlaceholder: '请输入用户自报的异常说明或归还备注',
  cancelButton: '取消',
  submitButton: '提交归还'
}

const returnStatusOptions = [
  { label: '待确认', value: 'PENDING' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '已结算', value: 'SETTLED' },
  { label: '已完成', value: 'COMPLETED' },
]

const vehicleConditionOptions = [
  { label: '无明显异常', value: '无明显异常' },
  { label: '轻微异常，待验车确认', value: '轻微异常，待验车确认' },
  { label: '存在明显异常，待验车确认', value: '存在明显异常，待验车确认' },
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

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

onMounted(() => {
  loadData()
  loadFormOptions().catch(() => {
    orderOptions.value = []
    regionOptions.value = []
  })
})
</script>
