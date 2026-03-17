<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { AssessmentRecord } from '../../../services/assessment'

const router = useRouter()
const state = (history.state && (history.state as any).result) as AssessmentRecord | undefined
const result = ref<AssessmentRecord | null>(state || null)

const scoreText = computed(() => (result.value ? `${result.value.totalScore}` : '-'))

function goHistory() {
  router.replace({ name: 'student-assessment-history' })
}
</script>

<template>
  <div class="page">
    <el-card v-if="result">
      <div class="title">测评结果</div>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="量表">{{ result.scaleType }}</el-descriptions-item>
        <el-descriptions-item label="总分">{{ scoreText }}</el-descriptions-item>
        <el-descriptions-item label="等级">{{ result.level }}</el-descriptions-item>
        <el-descriptions-item label="建议">{{ result.suggestion }}</el-descriptions-item>
      </el-descriptions>
      <div class="actions">
        <el-button type="primary" @click="goHistory">查看历史记录</el-button>
      </div>
    </el-card>
    <el-empty v-else description="暂无结果（请从测评入口提交后查看）" />
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}
.actions {
  margin-top: 12px;
}
</style>

