<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'
import { listNotifications } from '../services/notification'

const router = useRouter()
const route = useRoute()
const unreadCount = ref(0)
let timer: any = null

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

function goToNotifications() {
  const role = route.path.split('/')[1]
  router.push(`/${role}/notifications`)
}

onMounted(() => {
  fetchUnreadCount()
  // 轮询未读通知，每 30 秒一次
  timer = setInterval(fetchUnreadCount, 30000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
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