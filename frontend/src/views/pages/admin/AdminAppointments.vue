<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminPageAppointments } from '../../../services/admin'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<any[]>([])
const status = ref<string>('')

async function refresh() {
  loading.value = true
  try {
    const res = await adminPageAppointments(pageNum.value, pageSize.value, status.value || undefined)
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
    <div class="toolbar">
      <el-select v-model="status" placeholder="状态" clearable style="width: 160px">
        <el-option label="CONFIRMED" value="CONFIRMED" />
        <el-option label="CANCELED" value="CANCELED" />
        <el-option label="COMPLETED" value="COMPLETED" />
      </el-select>
      <el-button type="primary" @click="refresh" :loading="loading">查询</el-button>
    </div>

    <el-card>
      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="userId" label="学生ID" width="100" />
        <el-table-column prop="counselorUserId" label="咨询师ID" width="110" />
        <el-table-column prop="slotId" label="slotId" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="{ row }">{{ new Date(row.startTime).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="note" label="备注" min-width="200" />
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
.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>

