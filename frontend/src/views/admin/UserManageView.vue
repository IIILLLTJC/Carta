<template>
  <section class="crud-page">
    <div class="crud-card search-card">
      <div class="crud-card__header">
        <div>
          <p class="page-kicker">User Management</p>
          <h3>用户管理</h3>
        </div>
        <el-button type="primary" @click="openCreate">新增用户</el-button>
      </div>

      <el-form :model="searchForm" inline label-width="80px" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入真实姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
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
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="真实姓名" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="idCard" label="身份证号" min-width="180" show-overflow-tooltip />
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

    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新增用户' : '编辑用户'" width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="form-grid">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              :placeholder="dialog.mode === 'create' ? '请输入登录密码' : '不填写则保持原密码'"
            />
          </el-form-item>
          <el-form-item label="真实姓名">
            <el-input v-model="form.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="身份证号">
            <el-input v-model="form.idCard" placeholder="请输入身份证号" />
          </el-form-item>
          <el-form-item label="头像地址" class="full-row">
            <el-input v-model="form.avatarUrl" placeholder="请输入头像 URL" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
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
import { createUser, deleteUser, fetchUserDetail, fetchUserPage, updateUser, updateUserStatus } from '@/api/user'

const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const tableData = ref([])
const searchForm = reactive({
  username: '',
  realName: '',
  phone: '',
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

const validatePassword = (_, value, callback) => {
  if (dialog.mode === 'create' && !value) {
    callback(new Error('请输入密码'))
    return
  }
  callback()
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

function createDefaultForm() {
  return {
    username: '',
    password: '',
    realName: '',
    phone: '',
    email: '',
    idCard: '',
    avatarUrl: '',
    status: 1
  }
}

function resetForm() {
  Object.assign(form, createDefaultForm())
}

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchUserPage({
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    tableData.value = data.records
    pagination.total = data.total
  } catch (error) {
    ElMessage.error(error.message || '用户列表加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadData()
}

function handleReset() {
  searchForm.username = ''
  searchForm.realName = ''
  searchForm.phone = ''
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
    const { data } = await fetchUserDetail(id)
    dialog.visible = true
    dialog.mode = 'edit'
    dialog.id = id
    Object.assign(form, {
      username: data.username,
      password: '',
      realName: data.realName || '',
      phone: data.phone || '',
      email: data.email || '',
      idCard: data.idCard || '',
      avatarUrl: data.avatarUrl || '',
      status: data.status
    })
    formRef.value?.clearValidate()
  } catch (error) {
    ElMessage.error(error.message || '用户详情加载失败')
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (dialog.mode === 'create') {
      await createUser(form)
      ElMessage.success('用户新增成功')
    } else {
      await updateUser(dialog.id, form)
      ElMessage.success('用户修改成功')
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
    await deleteUser(id)
    ElMessage.success('用户删除成功')
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
    await updateUserStatus(row.id, value)
    ElMessage.success('用户状态已更新')
  } catch (error) {
    row.status = previous
    ElMessage.error(error.message || '状态更新失败')
  }
}

onMounted(() => {
  loadData()
})
</script>