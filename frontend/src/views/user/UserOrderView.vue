<template>
  <section class="crud-page">
    <div class="crud-card search-card">
      <div class="crud-card__header">
        <div>
          <p class="page-kicker">My Orders</p>
          <h3>订单管理</h3>
        </div>
        <el-button type="primary" @click="openCreate">新增订单</el-button>
      </div>

      <el-form :model="searchForm" inline label-width="80px" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.orderStatus" placeholder="全部状态" clearable style="width: 160px">
            <el-option v-for="item in orderStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
        <el-table-column label="订单状态" width="120">
          <template #default="scope">
            <el-tag :type="orderTagType(scope.row.orderStatus)">{{ orderLabel(scope.row.orderStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付状态" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.paymentStatus === 'PAID' ? 'success' : 'info'">{{ scope.row.paymentStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额信息" min-width="150">
          <template #default="scope">
            <div>订单 ￥{{ scope.row.orderAmount }}</div>
            <div class="table-secondary">押金 ￥{{ scope.row.depositAmount }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" min-width="180" />
        <el-table-column prop="expectedReturnTime" label="预计归还" min-width="180" />
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

    <el-dialog v-model="dialog.visible" title="新增订单" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="form-grid">
          <el-form-item label="车辆" prop="carId" class="full-row">
            <el-select v-model="form.carId" placeholder="请选择车辆" filterable style="width: 100%">
              <el-option v-for="item in carOptions" :key="item.id" :label="item.displayName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="归还区域">
            <el-select v-model="form.returnRegionId" placeholder="请选择归还区域" clearable style="width: 100%">
              <el-option v-for="item in regionOptions" :key="item.id" :label="item.regionName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="预计归还" prop="expectedReturnTime">
            <el-date-picker v-model="form.expectedReturnTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择预计归还时间" style="width: 100%" />
          </el-form-item>
          <el-form-item label="备注" class="full-row">
            <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交订单</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createUserOrder, fetchUserOrderFormOptions, fetchUserOrderPage } from '@/api/userOrder'

const orderStatusOptions = [
  { label: '使用中', value: 'USING' },
  { label: '归还中', value: 'RETURNING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
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
  carId: [{ required: true, message: '请选择车辆', trigger: 'change' }],
  expectedReturnTime: [{ required: true, message: '请选择预计归还时间', trigger: 'change' }]
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
    ElMessage.error(error.message || '订单选项加载失败')
  }
}

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchUserOrderPage({ ...searchForm, pageNum: pagination.pageNum, pageSize: pagination.pageSize })
    tableData.value = data.records
    pagination.total = data.total
  } catch (error) {
    ElMessage.error(error.message || '我的订单加载失败')
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
    ElMessage.success('订单创建成功')
    dialog.visible = false
    loadData()
    loadFormOptions()
  } catch (error) {
    ElMessage.error(error.message || '下单失败')
  } finally {
    submitting.value = false
  }
}

function orderLabel(status) {
  return orderStatusOptions.find((item) => item.value === status)?.label || status
}

function orderTagType(status) {
  if (status === 'USING') return 'warning'
  if (status === 'RETURNING') return 'primary'
  if (status === 'COMPLETED') return 'success'
  if (status === 'CANCELLED') return 'info'
  return 'info'
}

onMounted(async () => {
  await loadFormOptions()
  loadData()
})
</script>