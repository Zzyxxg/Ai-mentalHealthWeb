<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getScale, submitAssessment, type AssessmentScale } from '../../../services/assessment'

const route = useRoute()
const router = useRouter()
const type = computed(() => String(route.params.type || ''))

const loading = ref(false)
const scale = ref<AssessmentScale | null>(null)
const answers = ref<number[]>([])

async function load() {
  loading.value = true
  try {
    const res = await getScale(type.value)
    if (res.code === 0) {
      scale.value = res.data
      answers.value = new Array(res.data.questions.length).fill(0)
    } else {
      ElMessage.error(res.msg || '加载量表失败')
    }
  } finally {
    loading.value = false
  }
}

async function onSubmit() {
  if (!scale.value) return
  const res = await submitAssessment({ scaleType: scale.value.type, answers: answers.value })
  if (res.code === 0) {
    router.replace({ name: 'student-assessment-result', query: { id: String(res.data.id) }, state: { result: res.data } as any })
  } else {
    ElMessage.error(res.msg || '提交失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="page" v-loading="loading">
    <el-card v-if="scale">
      <div class="title">{{ scale.name }}</div>
      <el-divider />

      <div class="q" v-for="q in scale.questions" :key="q.index">
        <div class="q-title">{{ q.index }}. {{ q.title }}</div>
        <el-radio-group v-model="answers[q.index - 1]">
          <el-radio v-for="o in q.options" :key="o.score" :label="o.score">{{ o.label }}（{{ o.score }}）</el-radio>
        </el-radio-group>
      </div>

      <el-divider />
      <el-button type="primary" @click="onSubmit">提交并查看结果</el-button>
    </el-card>
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
}
.q {
  margin-bottom: 16px;
}
.q-title {
  margin-bottom: 8px;
  font-weight: 500;
}
</style>

