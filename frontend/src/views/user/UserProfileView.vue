<template>
  <section class="module-card profile-shell">
    <div class="module-card__head">
      <div>
        <p class="page-kicker">User Profile</p>
        <h3>????</h3>
      </div>
      <el-tag type="success">??????</el-tag>
    </div>

    <div class="profile-grid">
      <div class="profile-overview">
        <div class="profile-avatar">{{ profileInitial }}</div>
        <h4>{{ formModel.realName || formModel.username || '-' }}</h4>
        <p>{{ formModel.username || '-' }}</p>
        <el-tag :type="formModel.status === 1 ? 'success' : 'danger'">
          {{ formModel.status === 1 ? '??' : '??' }}
        </el-tag>
      </div>

      <div class="profile-form-wrap">
        <el-form ref="formRef" :model="formModel" :rules="rules" label-width="100px">
          <el-form-item label="????">
            <el-input v-model="formModel.username" disabled />
          </el-form-item>
          <el-form-item label="??" prop="realName">
            <el-input v-model="formModel.realName" placeholder="?????" maxlength="50" />
          </el-form-item>
          <el-form-item label="???" prop="phone">
            <el-input v-model="formModel.phone" placeholder="??????" maxlength="11" />
          </el-form-item>
          <el-form-item label="??" prop="email">
            <el-input v-model="formModel.email" placeholder="?????" maxlength="100" />
          </el-form-item>
          <el-form-item label="????">
            <el-input v-model="formModel.idCard" placeholder="???????" maxlength="18" />
          </el-form-item>
          <el-form-item label="????">
            <el-input v-model="formModel.avatarUrl" placeholder="???????????" maxlength="255" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave">????</el-button>
            <el-button @click="loadProfile">????</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchUserProfile, updateUserProfile } from '../../api/userProfile'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()
const formRef = ref()
const saving = ref(false)

const formModel = reactive({
  userId: undefined,
  username: '',
  realName: '',
  phone: '',
  email: '',
  idCard: '',
  avatarUrl: '',
  status: 1,
})

const rules = {
  realName: [{ required: true, message: '?????', trigger: 'blur' }],
  phone: [
    { required: true, message: '??????', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '????????', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '?????', trigger: 'blur' },
    { type: 'email', message: '???????', trigger: ['blur', 'change'] },
  ],
}

const profileInitial = computed(() => {
  const source = formModel.realName || formModel.username || 'U'
  return source.slice(0, 1).toUpperCase()
})

async function loadProfile() {
  const { data } = await fetchUserProfile()
  Object.assign(formModel, data?.data || {})
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
      avatarUrl: formModel.avatarUrl,
    }
    const { data } = await updateUserProfile(payload)
    Object.assign(formModel, data?.data || {})
    authStore.setDisplayName(formModel.realName || formModel.username || '')
    ElMessage.success('???????')
  } finally {
    saving.value = false
  }
}

onMounted(loadProfile)
</script>
