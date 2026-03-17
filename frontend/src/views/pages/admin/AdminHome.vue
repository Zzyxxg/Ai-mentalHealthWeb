<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminListUsers, adminPageAppointments, adminPageAssessments, adminPageThreads, adminStats } from '../../../services/admin'

const router = useRouter()
const loading = ref(false)

const totals = ref({
  users: 0,
  appointments: 0,
  threads: 0,
  assessments: 0
})

const statsDays = ref(7)
const stats = ref<any>(null)

function sumDaily(list: any[] | undefined) {
  if (!Array.isArray(list)) return 0
  return list.reduce((acc, x) => acc + Number(x?.cnt ?? x?.count ?? 0), 0)
}

const lastDays = computed(() => ({
  appointments: sumDaily(stats.value?.appointmentsDaily),
  threads: sumDaily(stats.value?.threadsDaily),
  assessments: sumDaily(stats.value?.assessmentsDaily)
}))

async function refresh() {
  loading.value = true
  try {
    const [usersRes, apptRes, threadRes, assessRes, statsRes] = await Promise.all([
      adminListUsers(1, 1),
      adminPageAppointments(1, 1),
      adminPageThreads(1, 1),
      adminPageAssessments(1, 1),
      adminStats(statsDays.value)
    ])

    if (usersRes.code === 0) totals.value.users = usersRes.data.total
    if (apptRes.code === 0) totals.value.appointments = apptRes.data.total
    if (threadRes.code === 0) totals.value.threads = threadRes.data.total
    if (assessRes.code === 0) totals.value.assessments = assessRes.data.total
    if (statsRes.code === 0) stats.value = statsRes.data

    const errMsg = usersRes.msg || apptRes.msg || threadRes.msg || assessRes.msg || statsRes.msg
    const anyErr = [usersRes, apptRes, threadRes, assessRes, statsRes].some(r => r.code !== 0)
    if (anyErr) {
      ElMessage.error(errMsg || '加载首页数据失败')
    }
  } finally {
    loading.value = false
  }
}

function go(path: string) {
  router.push(path)
}

onMounted(refresh)
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="header">
      <div class="title">管理端概览</div>
      <div class="actions">
        <el-button type="primary" plain @click="refresh">刷新</el-button>
        <el-button type="success" @click="go('/admin/stats')">查看统计图表</el-button>
      </div>
    </div>

    <div class="grid">
      <el-card class="card" shadow="hover" @click="go('/admin/users')">
        <div class="card-label">用户总数</div>
        <div class="card-value">{{ totals.users }}</div>
      </el-card>
      <el-card class="card" shadow="hover" @click="go('/admin/consultations')">
        <div class="card-label">咨询会话总数</div>
        <div class="card-value">{{ totals.threads }}</div>
      </el-card>
      <el-card class="card" shadow="hover" @click="go('/admin/appointments')">
        <div class="card-label">预约记录总数</div>
        <div class="card-value">{{ totals.appointments }}</div>
      </el-card>
      <el-card class="card" shadow="hover" @click="go('/admin/stats')">
        <div class="card-label">测评记录总数</div>
        <div class="card-value">{{ totals.assessments }}</div>
      </el-card>
    </div>

    <el-card class="section" shadow="never">
      <div class="section-title">近 {{ statsDays }} 天新增</div>
      <div class="mini-grid">
        <div class="mini-item">
          <div class="mini-label">新增预约</div>
          <div class="mini-value">{{ lastDays.appointments }}</div>
        </div>
        <div class="mini-item">
          <div class="mini-label">新增咨询</div>
          <div class="mini-value">{{ lastDays.threads }}</div>
        </div>
        <div class="mini-item">
          <div class="mini-label">完成测评</div>
          <div class="mini-value">{{ lastDays.assessments }}</div>
        </div>
      </div>
      <div class="section-actions">
        <el-input-number v-model="statsDays" :min="1" :max="365" :step="7" />
        <el-button type="primary" @click="refresh">应用</el-button>
        <el-button plain @click="go('/admin/audit-logs')">查看审计日志</el-button>
        <el-button plain @click="go('/admin/notifications')">通知中心</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: 700;
}

.actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.card {
  cursor: pointer;
}

.card-label {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  margin-bottom: 8px;
}

.card-value {
  font-size: 28px;
  font-weight: 700;
}

.section {
  border-radius: 8px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
}

.mini-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.mini-item {
  background: var(--el-fill-color-light);
  border-radius: 8px;
  padding: 12px;
}

.mini-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}

.mini-value {
  font-size: 22px;
  font-weight: 700;
}

.section-actions {
  margin-top: 12px;
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}
</style>
