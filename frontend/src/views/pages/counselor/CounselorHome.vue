<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listConsultThreads, getConsultant, type ConsultThread, type Counselor } from '../../../services/consult'
import { useAuthStore } from '../../../stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const unhandledCount = ref(0)
const loading = ref(false)
const profile = ref<Counselor | null>(null)

async function fetchStats() {
  loading.value = true
  try {
    const res = await listConsultThreads(1, 1, 'UNPROCESSED')
    if (res.code === 0) {
      if (Array.isArray(res.data)) {
        unhandledCount.value = res.data.length
      } else {
        unhandledCount.value = res.data.total
      }
    }
    
    // 获取咨询师个人资料
    if (authStore.me?.id) {
      const profileRes = await getConsultant(authStore.me.id)
      if (profileRes.code === 0) {
        profile.value = profileRes.data
      }
    }
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
}

function goToConsultations() {
  router.push({ name: 'counselor-consultations' })
}

function goToSchedules() {
  router.push({ name: 'counselor-schedule' })
}

function goToAppointments() {
  router.push({ name: 'counselor-appointments' })
}

onMounted(() => {
  fetchStats()
})
</script>

<template>
  <div class="container">
    <div class="welcome-section">
      <h2 class="welcome">欢迎回来，{{ profile?.realName || authStore.me?.nickname || '咨询师' }}</h2>
      <p v-if="profile" class="profile-intro">
        <el-tag size="small" type="success" class="title-tag">{{ profile.title }}</el-tag>
        <span class="expertise">擅长：{{ profile.expertise }}</span>
      </p>
      <p v-if="profile?.intro" class="profile-desc">{{ profile.intro }}</p>
    </div>
    
    <div class="stats-cards">
      <el-card class="stat-card" shadow="hover" @click="goToConsultations">
        <div class="stat-content">
          <div class="stat-label">待回复咨询</div>
          <div class="stat-value" :class="{ 'has-pending': unhandledCount > 0 }">
            {{ unhandledCount }}
          </div>
        </div>
        <div class="stat-icon">
          <el-icon><ChatLineRound /></el-icon>
        </div>
      </el-card>
      
      <el-card class="stat-card" shadow="hover" @click="goToAppointments">
        <div class="stat-content">
          <div class="stat-label">我的预约</div>
          <div class="stat-value">-</div>
        </div>
        <div class="stat-icon">
          <el-icon><Calendar /></el-icon>
        </div>
      </el-card>
    </div>

    <div class="quick-actions">
      <h3>快捷操作</h3>
      <div class="actions-grid">
        <el-button type="primary" plain @click="goToConsultations">处理咨询</el-button>
        <el-button type="success" plain @click="goToSchedules">排班管理</el-button>
        <el-button type="warning" plain @click="goToAppointments">预约管理</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  padding: 20px;
}

.welcome-section {
  margin-bottom: 32px;
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.welcome {
  margin: 0 0 12px 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.profile-intro {
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.expertise {
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.profile-desc {
  margin: 0;
  font-size: 14px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-card :deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.stat-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.stat-value.has-pending {
  color: var(--el-color-danger);
}

.stat-icon {
  font-size: 40px;
  color: var(--el-color-primary-light-5);
}

.quick-actions h3 {
  font-size: 16px;
  margin-bottom: 16px;
}

.actions-grid {
  display: flex;
  gap: 16px;
}
</style>
