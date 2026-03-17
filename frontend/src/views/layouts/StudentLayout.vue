<script setup lang="ts">
import { router } from '../../router'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()

async function onLogout() {
  authStore.logout()
  await router.replace('/login')
}
</script>

<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="brand">心理健康平台</div>
      <el-menu router :default-active="$route.path" class="menu">
        <el-menu-item index="/student">寻找咨询师</el-menu-item>
        <el-menu-item index="/student/appointments">我的预约</el-menu-item>
        <el-menu-item index="/student/consultations">我的咨询</el-menu-item>
        <el-menu-item index="/student/notifications">通知中心</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">学生端</div>
        <div class="header-right">
          <el-button type="primary" plain @click="onLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
}
.aside {
  border-right: 1px solid var(--el-border-color);
}
.brand {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  font-weight: 600;
}
.menu {
  border-right: none;
}
.header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--el-border-color);
}
.main {
  background: var(--el-bg-color-page);
}
</style>
