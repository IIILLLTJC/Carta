<template>
  <section class="crud-page">
    <div class="crud-card search-card">
      <div class="crud-card__header">
        <div>
          <p class="page-kicker">Return Management</p>
          <h3>{{ ui.pageTitle }}</h3>
        </div>
        <el-button type="primary" @click="openCreate">{{ ui.createButton }}</el-button>
      </div>

      <el-form :model="searchForm" inline label-width="80px" class="search-form">
        <el-form-item :label="ui.orderNoLabel">
          <el-input v-model="searchForm.orderNo" :placeholder="ui.orderNoPlaceholder" clearable />
        </el-form-item>
        <el-form-item :label="ui.carKeywordLabel">
          <el-input v-model="searchForm.carKeyword" :placeholder="ui.carKeywordPlaceholder" clearable />
        </el-form-item>
        <el-form-item :label="ui.userKeywordLabel">
          <el-input v-model="searchForm.userKeyword" :placeholder="ui.userKeywordPlaceholder" clearable />
        </el-form-item>
        <el-form-item :label="ui.returnRegionLabel">
          <el-select v-model="searchForm.returnRegionId" :placeholder="ui.regionAllPlaceholder" clearable style="width: 180px">
            <el-option v-for="item in regionOptions" :key="item.id" :label="item.regionName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="ui.statusLabel">
          <el-select v-model="searchForm.status" :placeholder="ui.statusAllPlaceholder" clearable style="width: 160px">
            <el-option v-for="item in returnStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ ui.searchButton }}</el-button>
          <el-button @click="handleReset">{{ ui.resetButton }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="crud-card table-card">
      <el-table v-loading="loading" :data="tableData" border class="crud-table">
        <el-table-column prop="orderNo" :label="ui.orderNoLabel" min-width="180" />
        <el-table-column :label="ui.userInfoLabel" min-width="180">
          <template #default="scope">
            <div>{{ scope.row.username }} / {{ scope.row.realName || ui.notVerified }}</div>
            <div class="table-secondary">{{ scope.row.phone || '--' }}</div>
          </template>
        </el-table-column>
        <el-table-column :label="ui.carInfoLabel" min-width="220">
          <template #default="scope">
            <div>{{ scope.row.carCode }} / {{ scope.row.licensePlate }}</div>
            <div class="table-secondary">{{ scope.row.brand }} {{ scope.row.model }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="returnRegionName" :label="ui.returnRegionLabel" min-width="140" />
        <el-table-column :label="ui.timeInfoLabel" min-width="220">
          <template #default="scope">
            <div>{{ ui.expectedReturnTimeLabel }}: {{ formatDateTime(scope.row.expectedReturnTime) }}</div>
            <div class="table-secondary">{{ ui.actualReturnTimeLabel }}: {{ formatDateTime(scope.row.actualReturnTime || scope.row.returnTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column :label="ui.settlementLabel" min-width="240">
          <template #default="scope">
            <div>{{ ui.baseRentLabel }} {{ formatMoney(scope.row.orderAmount) }} / {{ ui.depositLabel }} {{ formatMoney(scope.row.depositAmount) }}</div>
            <div class="table-secondary">{{ ui.damageCostLabel }} {{ formatMoney(scope.row.damageCost) }} / {{ ui.lateFeeLabel }} {{ formatMoney(scope.row.lateFee) }}</div>
            <div class="table-secondary">{{ ui.finalAmountLabel }} {{ formatMoney(scope.row.finalAmount) }}</div>
          </template>
        </el-table-column>
        <el-table-column :label="ui.statusLabel" width="160">
          <template #default="scope">
            <el-select :model-value="scope.row.status" size="small" @change="(value) => handleStatusChange(scope.row, value)">
              <el-option v-for="item in returnStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="processedByName" :label="ui.processedByLabel" min-width="140" />
        <el-table-column :label="ui.actionsLabel" width="180" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openEdit(scope.row.id)">{{ ui.editButton }}</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row.id)">{{ ui.deleteButton }}</el-button>
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

    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? ui.createTitle : ui.editTitle" width="860px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <div class="form-grid">
          <el-form-item :label="ui.orderLabel" prop="rentalOrderId" class="full-row">
            <el-select v-model="form.rentalOrderId" :placeholder="ui.orderSelectPlaceholder" filterable style="width: 100%">
              <el-option v-for="item in orderOptions" :key="item.id" :label="item.displayName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item :label="ui.returnRegionLabel" prop="returnRegionId">
            <el-select v-model="form.returnRegionId" :placeholder="ui.returnRegionPlaceholder" style="width: 100%">
              <el-option v-for="item in regionOptions" :key="item.id" :label="item.regionName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item :label="ui.processedByLabel">
            <el-select v-model="form.processedBy" :placeholder="ui.processedByPlaceholder" clearable style="width: 100%">
              <el-option v-for="item in adminOptions" :key="item.id" :label="(item.realName || item.username) + ' / ' + item.username" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item :label="ui.actualReturnTimeLabel" prop="returnTime">
            <el-date-picker v-model="form.returnTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" :placeholder="ui.returnTimePlaceholder" style="width: 100%" />
          </el-form-item>
          <el-form-item :label="ui.statusLabel" prop="status">
            <el-select v-model="form.status" :placeholder="ui.statusPlaceholder" style="width: 100%">
              <el-option v-for="item in returnStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item :label="ui.vehicleCheckLabel" class="full-row">
            <el-input v-model="form.vehicleCondition" :placeholder="ui.vehicleCheckPlaceholder" />
          </el-form-item>
          <el-form-item :label="ui.damageCostLabel" prop="damageCost">
            <el-input-number v-model="form.damageCost" :min="0" :precision="2" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item :label="ui.lateFeeLabel">
            <el-input :model-value="formatMoney(form.lateFee)" readonly />
          </el-form-item>
          <el-form-item :label="ui.finalAmountLabel">
            <el-input :model-value="formatMoney(form.finalAmount)" readonly />
          </el-form-item>
          <el-form-item :label="ui.settlementInfoLabel" class="full-row">
            <div class="dialog-tip settlement-tip">
              <div>{{ ui.baseRentLabel }}: {{ formatMoney(form.orderAmount) }}</div>
              <div>{{ ui.depositLabel }}: {{ formatMoney(form.depositAmount) }}</div>
              <div>{{ ui.expectedReturnTimeLabel }}: {{ formatDateTime(form.expectedReturnTime) }}</div>
              <div>{{ ui.actualReturnTimeLabel }}: {{ formatDateTime(form.returnTime || form.actualReturnTime) }}</div>
              <div class="table-secondary">{{ ui.autoCalcTip }}</div>
            </div>
          </el-form-item>
          <el-form-item :label="ui.remarkLabel" class="full-row">
            <el-input v-model="form.remark" type="textarea" :rows="3" :placeholder="ui.remarkPlaceholder" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">{{ ui.cancelButton }}</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ ui.saveButton }}</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createReturn, deleteReturn, fetchReturnDetail, fetchReturnFormOptions, fetchReturnPage, updateReturn, updateReturnStatus } from '@/api/returnRecord'

const ui = {
  pageTitle: '归还管理',
  createButton: '新增归还记录',
  orderNoLabel: '订单号',
  orderNoPlaceholder: '请输入订单号',
  carKeywordLabel: '车辆关键字',
  carKeywordPlaceholder: '车辆编号 / 车牌号',
  userKeywordLabel: '用户关键字',
  userKeywordPlaceholder: '用户名 / 姓名 / 手机号',
  returnRegionLabel: '归还区域',
  regionAllPlaceholder: '全部区域',
  statusLabel: '状态',
  statusAllPlaceholder: '全部状态',
  searchButton: '查询',
  resetButton: '重置',
  userInfoLabel: '用户信息',
  notVerified: '未实名',
  carInfoLabel: '车辆信息',
  timeInfoLabel: '时间信息',
  expectedReturnTimeLabel: '预计归还',
  actualReturnTimeLabel: '实际归还',
  settlementLabel: '结算明细',
  baseRentLabel: '订单金额',
  depositLabel: '押金',
  damageCostLabel: '损坏费',
  lateFeeLabel: '逾期费',
  finalAmountLabel: '最终结算',
  processedByLabel: '处理管理员',
  actionsLabel: '操作',
  editButton: '编辑',
  deleteButton: '删除',
  createTitle: '新增归还记录',
  editTitle: '编辑归还记录',
  orderLabel: '订单',
  orderSelectPlaceholder: '请选择订单',
  processedByPlaceholder: '请选择管理员',
  returnRegionPlaceholder: '请选择归还区域',
  returnTimePlaceholder: '请选择归还时间',
  statusPlaceholder: '请选择归还状态',
  vehicleCheckLabel: '验车结论',
  vehicleCheckPlaceholder: '正常完成可填写“无明显异常”；异常完成请填写“验车确认：xxx异常/需维修”',
  settlementInfoLabel: '结算信息',
  autoCalcTip: '逾期费与最终结算金额由系统按预计归还时间、实际归还时间和损坏费自动计算',
  remarkLabel: '备注',
  remarkPlaceholder: '请输入备注',
  cancelButton: '取消',
  saveButton: '保存'
}

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
  damageCost: [{ required: true, message: '请输入损坏费用', trigger: 'change' }]
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
    orderAmount: 0,
    depositAmount: 0,
    expectedReturnTime: '',
    actualReturnTime: '',
    processedBy: null,
    returnTime: '',
    remark: ''
  }
}

function resetForm() {
  Object.assign(form, createDefaultForm())
}

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

function handleSearch() {
  pagination.pageNum = 1
  loadData()
}

function handleReset() {
  Object.assign(searchForm, { orderNo: '', carKeyword: '', userKeyword: '', returnRegionId: undefined, status: '' })
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
      orderAmount: Number(data.orderAmount ?? 0),
      depositAmount: Number(data.depositAmount ?? 0),
      expectedReturnTime: normalizeDateTime(data.expectedReturnTime),
      actualReturnTime: normalizeDateTime(data.actualReturnTime || data.returnTime),
      processedBy: data.processedBy,
      returnTime: normalizeDateTime(data.returnTime),
      remark: data.remark || ''
    })
    formRef.value?.clearValidate()
  } catch (error) {
    ElMessage.error(error.message || '归还详情加载失败')
  }
}

function buildPayload() {
  return {
    rentalOrderId: form.rentalOrderId,
    returnRegionId: form.returnRegionId,
    status: form.status,
    vehicleCondition: form.vehicleCondition,
    damageCost: Number(form.damageCost ?? 0),
    lateFee: Number(form.lateFee ?? 0),
    finalAmount: Number(form.finalAmount ?? 0),
    processedBy: form.processedBy,
    returnTime: form.returnTime,
    remark: form.remark
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = buildPayload()
    if (dialog.mode === 'create') {
      await createReturn(payload)
      ElMessage.success('归还记录新增成功')
    } else {
      await updateReturn(dialog.id, payload)
      ElMessage.success('归还记录修改成功')
    }
    dialog.visible = false
    await loadData()
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
    await loadData()
  } catch (error) {
    row.status = previous
    ElMessage.error(error.message || '状态更新失败')
  }
}

function normalizeDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : ''
}

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

function formatMoney(value) {
  if (value === null || value === undefined || value === '') {
    return '￥0.00'
  }
  return `￥${Number(value).toFixed(2)}`
}

onMounted(async () => {
  await loadFormOptions()
  loadData()
})
</script>
