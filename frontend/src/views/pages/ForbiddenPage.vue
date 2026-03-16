<script setup lang="ts">
import { router } from '../../router'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()

async function goHome() {
  if (authStore.role === 'ADMIN') return router.replace('/admin')
  if (authStore.role === 'CONSULTANT') return router.replace('/counselor')
  if (authStore.role === 'STUDENT') return router.replace('/student')
  return router.replace('/login')
}
</script>

<template>
  <div class="page">
    <el-result icon="warning" title="无权限访问" sub-title="请确认账号权限或切换身份后重试">
      <template #extra>
        <el-button type="primary" @click="goHome">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}
</style>
