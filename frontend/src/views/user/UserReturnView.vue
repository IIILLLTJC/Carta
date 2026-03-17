<template>
  <section class="module-card">
    <div class="module-card__head">
      <div>
        <p class="page-kicker">User Returns</p>
        <h3>????</h3>
      </div>
      <el-button type="primary" @click="openCreateDialog">??????</el-button>
    </div>

    <el-form :inline="true" :model="queryForm" class="search-form">
      <el-form-item label="???">
        <el-input v-model="queryForm.orderNo" placeholder="?????" clearable />
      </el-form-item>
      <el-form-item label="????">
        <el-select v-model="queryForm.status" placeholder="????" clearable style="width: 160px">
          <el-option v-for="item in returnStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">??</el-button>
        <el-button @click="handleReset">??</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" stripe>
      <el-table-column prop="orderNo" label="???" min-width="180" />
      <el-table-column label="????" min-width="220">
        <template #default="scope">
          <div>{{ scope.row.carCode || '-' }}</div>
          <div class="table-sub-copy">{{ scope.row.licensePlate || '-' }} / {{ scope.row.brand || '-' }} {{ scope.row.model || '' }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="returnRegionName" label="????" min-width="140" />
      <el-table-column prop="vehicleCondition" label="????" min-width="140" />
      <el-table-column label="????" min-width="180">
        <template #default="scope">
          <div>??: {{ formatMoney(scope.row.damageCost) }}</div>
          <div>??: {{ formatMoney(scope.row.lateFee) }}</div>
          <div>??: {{ formatMoney(scope.row.finalAmount) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="??" min-width="120">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="returnTime" label="????" min-width="180" />
      <el-table-column label="??" min-width="180">
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

    <el-dialog v-model="dialogVisible" title="??????" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="formModel" :rules="rules" label-width="110px">
        <el-form-item label="?????" prop="rentalOrderId">
          <el-select v-model="formModel.rentalOrderId" placeholder="?????" filterable style="width: 100%" @change="handleOrderChange">
            <el-option
              v-for="item in orderOptions"
              :key="item.id"
              :label="`${item.orderNo} / ${item.carCode || '-'} / ${item.licensePlate || '-'}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="????" prop="returnRegionId">
          <el-select v-model="formModel.returnRegionId" placeholder="???????" filterable style="width: 100%">
            <el-option v-for="item in regionOptions" :key="item.id" :label="`${item.regionName} (${item.regionCode})`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="????" prop="vehicleCondition">
          <el-select v-model="formModel.vehicleCondition" placeholder="???????" style="width: 100%">
            <el-option label="??" value="??" />
            <el-option label="????" value="????" />
            <el-option label="????" value="????" />
          </el-select>
        </el-form-item>
        <el-form-item label="????">
          <div class="dialog-tip" v-if="selectedOrder">
            <div>???{{ selectedOrder.carCode || '-' }} / {{ selectedOrder.licensePlate || '-' }}</div>
            <div>????{{ selectedOrder.pickupRegionName || '-' }}</div>
            <div>?????{{ selectedOrder.expectedReturnTime || '-' }}</div>
          </div>
          <span v-else class="table-sub-copy">????????</span>
        </el-form-item>
        <el-form-item label="??">
          <el-input v-model="formModel.remark" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="????????????" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">??</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">????</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createUserReturn, fetchUserReturnFormOptions, fetchUserReturnPage } from '../../api/userReturn'

const returnStatusOptions = [
  { label: '???', value: 'PENDING' },
  { label: '???', value: 'CONFIRMED' },
  { label: '???', value: 'SETTLED' },
  { label: '???', value: 'COMPLETED' },
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
  rentalOrderId: [{ required: true, message: '?????', trigger: 'change' }],
  returnRegionId: [{ required: true, message: '???????', trigger: 'change' }],
  vehicleCondition: [{ required: true, message: '???????', trigger: 'change' }],
}

const selectedOrder = computed(() => orderOptions.value.find((item) => item.id === formModel.rentalOrderId))

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchUserReturnPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...queryForm,
    })
    const pageData = data?.data || {}
    tableData.value = pageData.records || []
    pagination.total = Number(pageData.total || 0)
    pagination.pageNum = Number(pageData.pageNum || pagination.pageNum)
    pagination.pageSize = Number(pageData.pageSize || pagination.pageSize)
  } finally {
    loading.value = false
  }
}

async function loadFormOptions() {
  const { data } = await fetchUserReturnFormOptions()
  const payload = data?.data || {}
  orderOptions.value = payload.orders || []
  regionOptions.value = payload.regions || []
}

function openCreateDialog() {
  loadFormOptions().then(() => {
    dialogVisible.value = true
    resetForm()
  })
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
    ElMessage.success('???????')
    dialogVisible.value = false
    await Promise.all([loadData(), loadFormOptions()])
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
    return '?0.00'
  }
  return `?${Number(value).toFixed(2)}`
}

onMounted(() => {
  loadData()
  loadFormOptions()
})
</script>
