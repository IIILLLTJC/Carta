<template>
  <section class="module-card profile-shell">
    <div class="module-card__head">
      <div>
        <p class="page-kicker">User Profile</p>
        <h3>个人中心</h3>
      </div>
      <el-tag type="success">资料维护</el-tag>
    </div>

    <div class="profile-grid">
      <div class="profile-overview">
        <div class="profile-avatar">
          <el-image v-if="formModel.avatarUrl" :src="formModel.avatarUrl" fit="cover" class="profile-avatar-image" />
          <span v-else>{{ profileInitial }}</span>
        </div>
        <h4>{{ formModel.realName || formModel.username || '-' }}</h4>
        <p>{{ formModel.username || '-' }}</p>
        <el-tag :type="formModel.status === 1 ? 'success' : 'danger'">
          {{ formModel.status === 1 ? '启用' : '停用' }}
        </el-tag>
      </div>

      <div class="profile-form-wrap">
        <el-form ref="formRef" :model="formModel" :rules="rules" label-width="100px">
          <el-form-item label="用户名">
            <el-input v-model="formModel.username" disabled />
          </el-form-item>
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="formModel.realName" placeholder="请输入真实姓名" maxlength="50" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="formModel.phone" placeholder="请输入手机号" maxlength="11" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formModel.email" placeholder="请输入邮箱" maxlength="100" />
          </el-form-item>
          <el-form-item label="身份证号">
            <el-input v-model="formModel.idCard" placeholder="请输入身份证号" maxlength="18" />
          </el-form-item>
          <el-form-item label="头像上传">
            <div class="upload-panel">
              <div class="upload-action-row">
                <el-upload :show-file-list="false" accept="image/*" :http-request="handleAvatarUpload">
                  <el-button type="primary" plain :loading="avatarUploading">本地上传头像</el-button>
                </el-upload>
                <span class="upload-hint">{{ avatarUploadHint }}</span>
              </div>
              <el-input v-model="formModel.avatarUrl" readonly placeholder="上传后自动生成头像地址" />
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave">保存资料</el-button>
            <el-button @click="loadProfile">重新加载</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchUserProfile, updateUserProfile } from '@/api/userProfile'
import { uploadImage } from '@/api/upload'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const formRef = ref()
const saving = ref(false)
const avatarUploading = ref(false)
const avatarUploadHint = '\u652f\u6301 JPG / PNG / GIF / WEBP\uff0c\u652f\u6301\u6700\u5927\u4e0a\u4f20\u5c3a\u5bf8\uff1a10MB\uff0c\u4e0a\u4f20\u540e\u81ea\u52a8\u56de\u586b\u5934\u50cf\u5730\u5740'

const formModel = reactive({
  userId: undefined,
  username: '',
  realName: '',
  phone: '',
  email: '',
  idCard: '',
  avatarUrl: '',
  status: 1
})

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ]
}

const profileInitial = computed(() => {
  const source = formModel.realName || formModel.username || 'U'
  return source.slice(0, 1).toUpperCase()
})

function resolveUploadErrorMessage(error, fallbackMessage) {
  const message = error?.message || ''
  if (/maximum upload size exceeded|\u6587\u4ef6\u8fc7\u5927|\u4e0d\u80fd\u8d85\u8fc7\s*10\s*mb|\u4e0d\u80fd\u8d85\u8fc7\s*5\s*mb/i.test(message)) {
    return '\u4e0a\u4f20\u5931\u8d25\uff1a\u6587\u4ef6\u8fc7\u5927\uff0c\u6700\u5927\u652f\u6301 10MB'
  }
  return message || fallbackMessage
}

async function loadProfile() {
  try {
    const { data } = await fetchUserProfile()
    Object.assign(formModel, data || {})
  } catch (error) {
    ElMessage.error(error.message || '个人资料加载失败')
  }
}

async function handleAvatarUpload(option) {
  avatarUploading.value = true
  try {
    const { data } = await uploadImage(option.file, 'avatar')
    formModel.avatarUrl = data?.url || ''
    option.onSuccess?.(data)
    ElMessage.success('头像上传成功')
  } catch (error) {
    option.onError?.(error)
    ElMessage.error(resolveUploadErrorMessage(error, '\u5934\u50cf\u4e0a\u4f20\u5931\u8d25'))
  } finally {
    avatarUploading.value = false
  }
}

async function handleSave() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = {
      realName: formModel.realName,
      phone: formModel.phone,
      email: formModel.email,
      idCard: formModel.idCard,
      avatarUrl: formModel.avatarUrl
    }
    const { data } = await updateUserProfile(payload)
    Object.assign(formModel, data || {})
    authStore.setDisplayName(formModel.realName || formModel.username || '')
    ElMessage.success('个人资料保存成功')
  } catch (error) {
    ElMessage.error(error.message || '个人资料保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadProfile)
</script>
