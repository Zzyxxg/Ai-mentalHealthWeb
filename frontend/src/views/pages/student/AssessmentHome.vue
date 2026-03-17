<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listScales, type AssessmentScale } from '../../../services/assessment'

const router = useRouter()
const loading = ref(false)
const scales = ref<AssessmentScale[]>([])

async function refresh() {
  loading.value = true
  try {
    const res = await listScales()
    if (res.code === 0) {
      scales.value = res.data || []
    } else {
      ElMessage.error(res.msg || '加载量表失败')
    }
  } finally {
    loading.value = false
  }
}

function go(type: string) {
  router.push({ name: 'student-assessment-form', params: { type } })
}

function goHistory() {
  router.push({ name: 'student-assessment-history' })
}

onMounted(refresh)
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <el-button type="primary" @click="refresh" :loading="loading">刷新</el-button>
      <el-button type="success" plain @click="goHistory">历史记录</el-button>
    </div>

    <el-row :gutter="12">
      <el-col v-for="s in scales" :key="s.type" :xs="24" :sm="12" :md="12" :lg="8">
        <el-card shadow="hover" class="card" @click="go(s.type)">
          <div class="title">{{ s.name }}</div>
          <div class="sub">点击开始测评</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.toolbar {
  display: flex;
  gap: 12px;
}
.card {
  cursor: pointer;
}
.title {
  font-weight: 600;
  margin-bottom: 6px;
}
.sub {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>

