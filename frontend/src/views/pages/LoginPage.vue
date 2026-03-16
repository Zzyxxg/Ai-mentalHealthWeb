<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRoute } from 'vue-router'

import { router } from '../../router'
import { login } from '../../services/auth'
import { useAuthStore } from '../../stores/auth'
import type { UserRole } from '../../types/auth'

const authStore = useAuthStore()
const route = useRoute()

const submitting = ref(false)
const form = reactive({
  username: '',
  password: '',
  role: 'STUDENT' as UserRole,
})

async function onSubmit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }

  submitting.value = true
  try {
    const res = await login({ username: form.username, password: form.password })
    if (res.code !== 0) {
      ElMessage.error(res.msg || '登录失败')
      return
    }
    authStore.setToken(res.data.accessToken)
    authStore.setRole(form.role)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    await router.replace(redirect || (form.role === 'ADMIN' ? '/admin' : form.role === 'CONSULTANT' ? '/counselor' : '/student'))
  } catch (e: any) {
    const msg = e?.response?.data?.msg || e?.message || '登录失败'
    ElMessage.error(msg)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="page">
    <el-card class="card">
      <div class="title">心理健康咨询系统</div>
      <el-form :model="form" label-position="top" @submit.prevent>
        <el-form-item label="账号">
          <el-input v-model="form.username" autocomplete="username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" autocomplete="current-password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="登录身份">
          <el-radio-group v-model="form.role">
            <el-radio-button label="STUDENT">学生</el-radio-button>
            <el-radio-button label="CONSULTANT">咨询师</el-radio-button>
            <el-radio-button label="ADMIN">管理员</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-button type="primary" :loading="submitting" style="width: 100%" @click="onSubmit">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: var(--el-bg-color-page);
}
.card {
  width: 420px;
}
.title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
}
</style>
