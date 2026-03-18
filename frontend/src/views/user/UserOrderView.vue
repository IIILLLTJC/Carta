<template>
  <section class="crud-page">
    <div class="crud-card search-card">
      <div class="crud-card__header">
        <div>
          <p class="page-kicker">My Orders</p>
          <h3>{{ ui.pageTitle }}</h3>
        </div>
        <el-button type="primary" @click="openCreate">{{ ui.createOrder }}</el-button>
      </div>

      <el-form :model="searchForm" inline label-width="80px" class="search-form">
        <el-form-item :label="ui.orderNo">
          <el-input v-model="searchForm.orderNo" :placeholder="ui.orderNoPlaceholder" clearable />
        </el-form-item>
        <el-form-item :label="ui.orderStatus">
          <el-select v-model="searchForm.orderStatus" :placeholder="ui.allStatus" clearable style="width: 160px">
            <el-option v-for="item in orderStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
        <el-table-column prop="orderNo" :label="ui.orderNo" min-width="180" />
        <el-table-column :label="ui.carInfo" min-width="220">
          <template #default="scope">
            <div>{{ scope.row.carCode }} / {{ scope.row.licensePlate }}</div>
            <div class="table-secondary">{{ scope.row.brand }} {{ scope.row.model }}</div>
          </template>
        </el-table-column>
        <el-table-column :label="ui.regionInfo" min-width="200">
          <template #default="scope">
            <div>{{ ui.pickupPrefix }}{{ scope.row.pickupRegionName || '--' }}</div>
            <div class="table-secondary">{{ ui.returnPrefix }}{{ scope.row.returnRegionName || '--' }}</div>
          </template>
        </el-table-column>
        <el-table-column :label="ui.orderStatus" width="120">
          <template #default="scope">
            <el-tag :type="orderTagType(scope.row.orderStatus)">{{ orderLabel(scope.row.orderStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="ui.paymentStatus" width="120">
          <template #default="scope">
            <el-tag :type="paymentTagType(scope.row.paymentStatus)">{{ paymentLabel(scope.row.paymentStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="ui.amountInfo" min-width="150">
          <template #default="scope">
            <div>{{ ui.orderAmountPrefix }}{{ scope.row.orderAmount }}</div>
            <div class="table-secondary">{{ ui.depositPrefix }}{{ scope.row.depositAmount }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" :label="ui.startTime" min-width="180" />
        <el-table-column prop="expectedReturnTime" :label="ui.expectedReturn" min-width="180" />
        <el-table-column :label="ui.action" width="140" fixed="right">
          <template #default="scope">
            <el-button v-if="canMockPay(scope.row)" type="primary" link @click="handleMockPay(scope.row)">{{ ui.mockPay }}</el-button>
            <span v-else class="table-secondary">--</span>
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

    <el-dialog v-model="dialog.visible" :title="ui.createOrder" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="form-grid">
          <el-form-item :label="ui.car" prop="carId" class="full-row">
            <el-select v-model="form.carId" :placeholder="ui.carPlaceholder" filterable style="width: 100%">
              <el-option v-for="item in carOptions" :key="item.id" :label="item.displayName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item :label="ui.returnRegion">
            <el-select v-model="form.returnRegionId" :placeholder="ui.returnRegionPlaceholder" clearable style="width: 100%">
              <el-option v-for="item in regionOptions" :key="item.id" :label="item.regionName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item :label="ui.expectedReturn" prop="expectedReturnTime">
            <el-date-picker v-model="form.expectedReturnTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" :placeholder="ui.expectedReturnPlaceholder" style="width: 100%" />
          </el-form-item>
          <el-form-item :label="ui.remark" class="full-row">
            <el-input v-model="form.remark" type="textarea" :rows="3" :placeholder="ui.remarkPlaceholder" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">{{ ui.cancel }}</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ ui.submitOrder }}</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createUserOrder, fetchUserOrderFormOptions, fetchUserOrderPage, mockPayUserOrder } from '@/api/userOrder'

const ui = {
  pageTitle: '\u8ba2\u5355\u7ba1\u7406',
  createOrder: '\u65b0\u589e\u8ba2\u5355',
  orderNo: '\u8ba2\u5355\u53f7',
  orderNoPlaceholder: '\u8bf7\u8f93\u5165\u8ba2\u5355\u53f7',
  orderStatus: '\u8ba2\u5355\u72b6\u6001',
  allStatus: '\u5168\u90e8\u72b6\u6001',
  search: '\u67e5\u8be2',
  reset: '\u91cd\u7f6e',
  carInfo: '\u8f66\u8f86\u4fe1\u606f',
  regionInfo: '\u53d6\u8fd8\u8f66\u533a\u57df',
  pickupPrefix: '\u53d6\u8f66\uff1a',
  returnPrefix: '\u8fd8\u8f66\uff1a',
  paymentStatus: '\u652f\u4ed8\u72b6\u6001',
  amountInfo: '\u91d1\u989d\u4fe1\u606f',
  orderAmountPrefix: '\u8ba2\u5355 \uffe5',
  depositPrefix: '\u62bc\u91d1 \uffe5',
  startTime: '\u5f00\u59cb\u65f6\u95f4',
  expectedReturn: '\u9884\u8ba1\u5f52\u8fd8',
  action: '\u64cd\u4f5c',
  mockPay: '\u6a21\u62df\u652f\u4ed8',
  car: '\u8f66\u8f86',
  carPlaceholder: '\u8bf7\u9009\u62e9\u8f66\u8f86',
  returnRegion: '\u5f52\u8fd8\u533a\u57df',
  returnRegionPlaceholder: '\u8bf7\u9009\u62e9\u5f52\u8fd8\u533a\u57df',
  expectedReturnPlaceholder: '\u8bf7\u9009\u62e9\u9884\u8ba1\u5f52\u8fd8\u65f6\u95f4',
  remark: '\u5907\u6ce8',
  remarkPlaceholder: '\u8bf7\u8f93\u5165\u5907\u6ce8',
  cancel: '\u53d6\u6d88',
  submitOrder: '\u63d0\u4ea4\u8ba2\u5355',
  selectCar: '\u8bf7\u9009\u62e9\u8f66\u8f86',
  selectExpectedReturn: '\u8bf7\u9009\u62e9\u9884\u8ba1\u5f52\u8fd8\u65f6\u95f4',
  formOptionsError: '\u8ba2\u5355\u9009\u9879\u52a0\u8f7d\u5931\u8d25',
  listError: '\u6211\u7684\u8ba2\u5355\u52a0\u8f7d\u5931\u8d25',
  createSuccess: '\u8ba2\u5355\u521b\u5efa\u6210\u529f\uff0c\u8bf7\u5b8c\u6210\u6a21\u62df\u652f\u4ed8',
  createError: '\u4e0b\u5355\u5931\u8d25',
  payTitle: '\u6a21\u62df\u652f\u4ed8',
  payConfirmPrefix: '\u786e\u8ba4\u6a21\u62df\u652f\u4ed8\u8ba2\u5355 ',
  payConfirmMiddle: '\uff1f\u652f\u4ed8\u91d1\u989d \uffe5',
  payConfirmDeposit: '\uff0c\u62bc\u91d1 \uffe5',
  paySuccess: '\u652f\u4ed8\u6210\u529f\uff0c\u8ba2\u5355\u5df2\u8fdb\u5165\u4f7f\u7528\u4e2d',
  payError: '\u652f\u4ed8\u5931\u8d25'
}

const orderStatusOptions = [
  { label: '\u5f85\u652f\u4ed8', value: 'CREATED' },
  { label: '\u4f7f\u7528\u4e2d', value: 'USING' },
  { label: '\u5f52\u8fd8\u4e2d', value: 'RETURNING' },
  { label: '\u5df2\u5b8c\u6210', value: 'COMPLETED' },
  { label: '\u5df2\u53d6\u6d88', value: 'CANCELLED' }
]

const paymentStatusOptions = [
  { label: '\u5f85\u652f\u4ed8', value: 'UNPAID' },
  { label: '\u5df2\u652f\u4ed8', value: 'PAID' },
  { label: '\u5df2\u9000\u6b3e', value: 'REFUNDED' }
]

const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const tableData = ref([])
const carOptions = ref([])
const regionOptions = ref([])
const searchForm = reactive({ orderNo: '', orderStatus: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const dialog = reactive({ visible: false })
const form = reactive(createDefaultForm())

const rules = {
  carId: [{ required: true, message: ui.selectCar, trigger: 'change' }],
  expectedReturnTime: [{ required: true, message: ui.selectExpectedReturn, trigger: 'change' }]
}

function createDefaultForm() {
  return {
    carId: null,
    returnRegionId: null,
    expectedReturnTime: '',
    remark: ''
  }
}

function resetForm() {
  Object.assign(form, createDefaultForm())
}

async function loadFormOptions() {
  try {
    const { data } = await fetchUserOrderFormOptions()
    carOptions.value = data.cars
    regionOptions.value = data.regions
  } catch (error) {
    ElMessage.error(error.message || ui.formOptionsError)
  }
}

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchUserOrderPage({ ...searchForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize })
    tableData.value = data.records
    pagination.total = data.total
  } catch (error) {
    ElMessage.error(error.message || ui.listError)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadData()
}

function handleReset() {
  searchForm.orderNo = ''
  searchForm.orderStatus = ''
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
  resetForm()
  formRef.value?.clearValidate()
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await createUserOrder(form)
    ElMessage.success(ui.createSuccess)
    dialog.visible = false
    loadData()
    loadFormOptions()
  } catch (error) {
    ElMessage.error(error.message || ui.createError)
  } finally {
    submitting.value = false
  }
}

function canMockPay(row) {
  return row.orderStatus === 'CREATED' && row.paymentStatus === 'UNPAID'
}

async function handleMockPay(row) {
  try {
    await ElMessageBox.confirm(
      `${ui.payConfirmPrefix}${row.orderNo}${ui.payConfirmMiddle}${row.orderAmount}${ui.payConfirmDeposit}${row.depositAmount}`,
      ui.payTitle,
      { type: 'warning' }
    )
    await mockPayUserOrder(row.id)
    ElMessage.success(ui.paySuccess)
    loadData()
    loadFormOptions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || ui.payError)
    }
  }
}

function orderLabel(status) {
  return orderStatusOptions.find((item) => item.value === status)?.label || status
}

function orderTagType(status) {
  if (status === 'CREATED') return 'info'
  if (status === 'USING') return 'warning'
  if (status === 'RETURNING') return 'primary'
  if (status === 'COMPLETED') return 'success'
  if (status === 'CANCELLED') return 'info'
  return 'info'
}

function paymentLabel(status) {
  return paymentStatusOptions.find((item) => item.value === status)?.label || status
}

function paymentTagType(status) {
  if (status === 'PAID') return 'success'
  if (status === 'UNPAID') return 'warning'
  if (status === 'REFUNDED') return 'info'
  return 'info'
}

onMounted(async () => {
  await loadFormOptions()
  loadData()
})
</script>
