<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRoute } from 'vue-router'

import { router } from '../../router'
import { login, me } from '../../services/auth'
import { useAuthStore } from '../../stores/auth'
import type { UserRole } from '../../types/auth'

const authStore = useAuthStore()
const route = useRoute()

const submitting = ref(false)
const form = reactive({
  username: '',
  password: '',
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
    
    // 1. 先保存 Token，确保后续 me() 请求带上 Authorization
    authStore.setToken(res.data.token)
    
    // 2. 获取真实用户信息（角色以服务端为准）
    const userRes = await me()
    if (userRes.code !== 0) {
      authStore.logout()
      ElMessage.error('获取用户信息失败')
      return
    }
    
    const userData = userRes.data
    const actualRole = userData.role as UserRole
    
    // 3. 更新 Store（先更新状态，再进行跳转）
    authStore.setMe({
      id: userData.id,
      username: userData.username,
      role: actualRole,
      nickname: userData.nickname,
      avatarUrl: userData.avatarUrl
    })
    authStore.setRole(actualRole)

    // 4. 计算跳转路径
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    const targetPath = (actualRole === 'ADMIN' ? '/admin' : actualRole === 'CONSULTANT' ? '/counselor' : '/student')
    
    ElMessage.success('登录成功')

    // 逻辑：如果重定向地址与当前角色不匹配，则强制跳转到角色的默认首页
    let finalPath = targetPath
    if (redirect && redirect.startsWith('/')) {
      const isRedirectMatch = 
        (actualRole === 'ADMIN' && redirect.startsWith('/admin')) ||
        (actualRole === 'CONSULTANT' && redirect.startsWith('/counselor')) ||
        (actualRole === 'STUDENT' && redirect.startsWith('/student'))
      
      if (isRedirectMatch) {
        finalPath = redirect
      }
    }
    
    await router.replace(finalPath)
  } catch (e: any) {
    const msg = e?.response?.data?.msg || e?.message || '登录失败'
    ElMessage.error(msg)
    authStore.logout()
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <el-card class="login-card">
      <div class="login-header">
        <h2 class="login-title">心理健康咨询系统</h2>
        <p class="login-subtitle">请使用您的账号登录平台</p>
      </div>
      <el-form :model="form" label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="请输入账号" prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password prefix-icon="Lock" @keyup.enter="onSubmit" />
        </el-form-item>
        <el-button type="primary" :loading="submitting" class="submit-btn" @click="onSubmit">
          {{ submitting ? '登录中...' : '立即登录' }}
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  background-image: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}
.login-card {
  width: 420px;
  padding: 10px 20px 20px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}
.login-header {
  text-align: center;
  margin-bottom: 30px;
}
.login-title {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px;
}
.login-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}
.submit-btn {
  width: 100%;
  height: 40px;
  font-size: 16px;
  margin-top: 15px;
  border-radius: 8px;
}
</style>
