<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminStats } from '../../../services/admin'

const loading = ref(false)
const days = ref(30)
const data = ref<any>(null)

async function refresh() {
  loading.value = true
  try {
    const res = await adminStats(days.value)
    if (res.code === 0) {
      data.value = res.data
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } finally {
    loading.value = false
  }
}

onMounted(refresh)
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <el-input-number v-model="days" :min="1" :max="365" />
      <el-button type="primary" @click="refresh" :loading="loading">刷新统计</el-button>
    </div>

    <el-card v-loading="loading">
      <div class="hint">基础版：先输出原始聚合数据（后续可接图表组件）。</div>
      <pre class="pre">{{ JSON.stringify(data, null, 2) }}</pre>
    </el-card>
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
  align-items: center;
}
.hint {
  margin-bottom: 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.pre {
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 12px;
  line-height: 1.4;
}
</style>

