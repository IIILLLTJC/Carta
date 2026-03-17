<template>
  <section class="crud-page">
    <div class="crud-card search-card">
      <div class="crud-card__header">
        <div>
          <p class="page-kicker">Region Management</p>
          <h3>区域管理</h3>
        </div>
        <el-button type="primary" @click="openCreate">新增区域</el-button>
      </div>

      <el-form :model="searchForm" inline label-width="80px" class="search-form">
        <el-form-item label="区域名称">
          <el-input v-model="searchForm.regionName" placeholder="请输入区域名称" clearable />
        </el-form-item>
        <el-form-item label="区域编码">
          <el-input v-model="searchForm.regionCode" placeholder="请输入区域编码" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
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
        <el-table-column prop="regionName" label="区域名称" min-width="160" />
        <el-table-column prop="regionCode" label="区域编码" min-width="120" />
        <el-table-column prop="address" label="详细地址" min-width="220" show-overflow-tooltip />
        <el-table-column label="坐标" min-width="180">
          <template #default="scope">
            <span>{{ formatCoordinate(scope.row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="scope">
            <el-switch
              :model-value="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(value) => handleStatusChange(scope.row, value)"
            />
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

    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新增区域' : '编辑区域'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="区域名称" prop="regionName">
          <el-input v-model="form.regionName" placeholder="请输入区域名称" />
        </el-form-item>
        <el-form-item label="区域编码" prop="regionCode">
          <el-input v-model="form.regionCode" placeholder="请输入区域编码" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="3" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="经度">
          <el-input-number v-model="form.longitude" :precision="6" :step="0.000001" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="纬度">
          <el-input-number v-model="form.latitude" :precision="6" :step="0.000001" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>
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
import {
  createRegion,
  deleteRegion,
  fetchRegionDetail,
  fetchRegionPage,
  updateRegion,
  updateRegionStatus
} from '@/api/region'

const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const tableData = ref([])
const searchForm = reactive({
  regionName: '',
  regionCode: '',
  status: undefined
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
  regionName: [{ required: true, message: '请输入区域名称', trigger: 'blur' }],
  regionCode: [{ required: true, message: '请输入区域编码', trigger: 'blur' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

function createDefaultForm() {
  return {
    regionName: '',
    regionCode: '',
    address: '',
    longitude: null,
    latitude: null,
    status: 1,
    remark: ''
  }
}

function resetForm() {
  Object.assign(form, createDefaultForm())
}

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchRegionPage({
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    tableData.value = data.records
    pagination.total = data.total
  } catch (error) {
    ElMessage.error(error.message || '区域列表加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadData()
}

function handleReset() {
  searchForm.regionName = ''
  searchForm.regionCode = ''
  searchForm.status = undefined
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
    const { data } = await fetchRegionDetail(id)
    dialog.visible = true
    dialog.mode = 'edit'
    dialog.id = id
    Object.assign(form, {
      regionName: data.regionName,
      regionCode: data.regionCode,
      address: data.address,
      longitude: data.longitude,
      latitude: data.latitude,
      status: data.status,
      remark: data.remark || ''
    })
    formRef.value?.clearValidate()
  } catch (error) {
    ElMessage.error(error.message || '区域详情加载失败')
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (dialog.mode === 'create') {
      await createRegion(form)
      ElMessage.success('区域新增成功')
    } else {
      await updateRegion(dialog.id, form)
      ElMessage.success('区域修改成功')
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
    await ElMessageBox.confirm('删除后无法恢复，是否继续？', '删除确认', {
      type: 'warning'
    })
    await deleteRegion(id)
    ElMessage.success('区域删除成功')
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
    await updateRegionStatus(row.id, value)
    ElMessage.success('区域状态已更新')
  } catch (error) {
    row.status = previous
    ElMessage.error(error.message || '状态更新失败')
  }
}

function formatCoordinate(row) {
  if (row.longitude == null || row.latitude == null) {
    return '--'
  }
  return `${row.longitude}, ${row.latitude}`
}

onMounted(() => {
  loadData()
})
</script>