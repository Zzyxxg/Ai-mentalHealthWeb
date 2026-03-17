<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'
import { listNotifications, readAllNotifications } from '../services/notification'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const unreadCount = ref(0)
let timer: any = null
let listener: any = null

async function fetchUnreadCount() {
  try {
    const res = await listNotifications(1, 1, 0) // page 1, size 1, unread only
    if (res.code === 0) {
      unreadCount.value = res.data.total
    }
  } catch (e) {
    // ignore
  }
}

async function goToNotifications() {
  if (unreadCount.value > 0) {
    try {
      const res = await readAllNotifications()
      if (res.code === 0) {
        unreadCount.value = 0
        window.dispatchEvent(new Event('mh:notifications-updated'))
      }
    } catch {
      // ignore
    }
  }

  if (authStore.role === 'ADMIN') router.push('/admin/notifications')
  else if (authStore.role === 'CONSULTANT') router.push('/counselor/notifications')
  else router.push('/student/notifications')
}

onMounted(() => {
  fetchUnreadCount()
  // 轮询未读通知，每 30 秒一次
  timer = setInterval(fetchUnreadCount, 30000)

  listener = () => fetchUnreadCount()
  window.addEventListener('mh:notifications-updated', listener)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (listener) window.removeEventListener('mh:notifications-updated', listener)
})
</script>

<template>
  <div class="notification-bell" @click="goToNotifications">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="badge">
      <el-icon :size="20"><Bell /></el-icon>
    </el-badge>
  </div>
</template>

<style scoped>
.notification-bell {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  border-radius: 50%;
  transition: background-color 0.3s;
}
.notification-bell:hover {
  background-color: var(--el-fill-color-light);
}
.badge {
  display: flex;
  align-items: center;
}
</style>
