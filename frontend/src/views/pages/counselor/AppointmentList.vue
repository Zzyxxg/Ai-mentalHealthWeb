<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { completeAppointment, listAppointments, type Appointment } from '../../../services/consult'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<Appointment[]>([])

const statusFilter = ref<string>('')

const queryStatus = computed(() => (statusFilter.value ? statusFilter.value : undefined))

async function refresh() {
  loading.value = true
  try {
    const res = await listAppointments(pageNum.value, pageSize.value, queryStatus.value)
    if (res.code === 0) {
      total.value = res.data.total
      list.value = res.data.list || []
    } else {
      ElMessage.error(res.msg || '加载预约失败')
    }
  } finally {
    loading.value = false
  }
}

async function onComplete(appt: Appointment) {
  const res = await completeAppointment(appt.id)
  if (res.code === 0) {
    ElMessage.success('已完成')
    await refresh()
  } else {
    ElMessage.error(res.msg || '操作失败')
  }
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 180px">
        <el-option label="CONFIRMED" value="CONFIRMED" />
        <el-option label="CANCELED" value="CANCELED" />
        <el-option label="COMPLETED" value="COMPLETED" />
      </el-select>
      <el-button type="primary" @click="refresh" :loading="loading">刷新</el-button>
    </div>

    <el-card>
      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="startTime" label="开始时间" min-width="180">
          <template #default="{ row }">
            {{ new Date(row.startTime).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="durationMinutes" label="时长" width="100" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="note" label="备注" min-width="200" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" type="success" plain @click="onComplete(row)" :disabled="row.status !== 'CONFIRMED'">
              标记完成
            </el-button>
          </template>
        </el-table-column>
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

