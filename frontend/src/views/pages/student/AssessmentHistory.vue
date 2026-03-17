<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listMyAssessments, type AssessmentRecord } from '../../../services/assessment'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<AssessmentRecord[]>([])

async function refresh() {
  loading.value = true
  try {
    const res = await listMyAssessments(pageNum.value, pageSize.value)
    if (res.code === 0) {
      total.value = res.data.total
      list.value = res.data.list || []
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
    <el-card>
      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="scaleType" label="量表" width="120" />
        <el-table-column prop="totalScore" label="总分" width="100" />
        <el-table-column prop="level" label="等级" width="180" />
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.createTime).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="suggestion" label="建议" min-width="260" />
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, sizes"
          @current-change="refresh"
          @size-change="refresh"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>

