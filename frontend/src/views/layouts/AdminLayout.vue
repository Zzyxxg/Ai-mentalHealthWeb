<script setup lang="ts">
import { router } from '../../router'
import { useAuthStore } from '../../stores/auth'
import NotificationBell from '../../components/NotificationBell.vue'

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
        <el-menu-item index="/admin">首页</el-menu-item>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
        <el-menu-item index="/admin/appointments">预约记录</el-menu-item>
        <el-menu-item index="/admin/consultations">咨询管理</el-menu-item>
        <el-menu-item index="/admin/stats">统计概览</el-menu-item>
        <el-menu-item index="/admin/audit-logs">审计日志</el-menu-item>
        <el-menu-item index="/admin/notifications">通知中心</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">管理端</div>
        <div class="header-right">
          <NotificationBell style="margin-right: 16px" />
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
