<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listConsultThreads, type ConsultThread } from '../../../services/consult'
import { ElMessage } from 'element-plus'

const router = useRouter()
const unhandledCount = ref(0)
const loading = ref(false)

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
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
}

function goToConsultations() {
  router.push({ name: 'counselor-consultations' })
}

onMounted(() => {
  fetchStats()
})
</script>

<template>
  <div class="container">
    <h2 class="welcome">欢迎回来，咨询师</h2>
    
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
      
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-label">今日预约</div>
          <div class="stat-value">0</div>
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
        <el-button type="success" plain disabled>排班管理 (M3)</el-button>
        <el-button type="warning" plain disabled>预约管理 (M3)</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  padding: 20px;
}

.welcome {
  margin-bottom: 24px;
  font-size: 20px;
  font-weight: 600;
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
