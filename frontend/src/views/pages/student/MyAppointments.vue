<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listAppointments, cancelAppointment, type Appointment } from '../../../services/consult'
import { formatAppointmentStatus, formatDateTime } from '../../../utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const appointments = ref<Appointment[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const status = ref('')

async function fetchAppointments() {
  loading.value = true
  try {
    const res = await listAppointments(pageNum.value, pageSize.value, status.value)
    if (res.code === 0) {
      appointments.value = res.data.list
      total.value = res.data.total
    } else {
      ElMessage.error(res.msg || '获取预约列表失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '网络错误')
  } finally {
    loading.value = false
  }
}

async function handleCancel(id: number) {
  try {
    await ElMessageBox.confirm('确定要取消该预约吗？', '提示', { type: 'warning' })
    const res = await cancelAppointment(id)
    if (res.code === 0) {
      ElMessage.success('已取消预约')
      fetchAppointments()
    } else {
      ElMessage.error(res.msg || '取消失败')
    }
  } catch {
    // ignore cancel
  }
}

function getStatusType(s: string) {
  switch (s) {
    case 'CREATED': return 'info'
    case 'CONFIRMED': return 'success'
    case 'CANCELED':
    case 'CANCELLED': return 'danger'
    case 'COMPLETED': return 'warning'
    default: return ''
  }
}

onMounted(() => {
  fetchAppointments()
})
</script>

<template>
  <div class="container">
    <div class="header">
      <h2 class="title">我的预约</h2>
      <div class="filter">
        <el-select v-model="status" placeholder="状态筛选" clearable @change="fetchAppointments" style="width: 150px">
          <el-option label="已提交" value="CREATED" />
          <el-option label="已确认" value="CONFIRMED" />
          <el-option label="已取消" value="CANCELED" />
          <el-option label="已完成" value="COMPLETED" />
        </el-select>
      </div>
    </div>

    <el-card class="list-card" shadow="never" v-loading="loading">
      <el-table :data="appointments" style="width: 100%">
        <el-table-column label="咨询开始时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="durationMinutes" label="时长(分钟)" width="100" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ formatAppointmentStatus(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="note" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'CREATED' || row.status === 'CONFIRMED'"
              type="danger"
              link
              @click="handleCancel(row.id)"
            >取消预约</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchAppointments"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}
.list-card {
  border-radius: 8px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
