<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminPageAppointments } from '../../../services/admin'
import { formatAppointmentStatus, formatDateTime } from '../../../utils/format'

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

onMounted(refresh)
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <el-select v-model="status" placeholder="状态" clearable style="width: 160px">
        <el-option label="已提交" value="CREATED" />
        <el-option label="已预约" value="CONFIRMED" />
        <el-option label="已取消" value="CANCELED" />
        <el-option label="已完成" value="COMPLETED" />
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
          <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ formatAppointmentStatus(row.status) }}</el-tag>
          </template>
        </el-table-column>
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

