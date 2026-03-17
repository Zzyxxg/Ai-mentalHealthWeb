<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { adminListAuditLogs, type AdminAuditLog } from '../../../services/admin'
import { formatFullTime } from '../../../utils/format'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const logs = ref<AdminAuditLog[]>([])

const filter = reactive({
  userId: undefined as number | undefined,
  action: ''
})

const actions = [
  { label: '登录', value: 'LOGIN' },
  { label: '退出', value: 'LOGOUT' },
  { label: '修改用户状态', value: 'USER_UPDATE_STATUS' },
  { label: '重置密码', value: 'USER_RESET_PASSWORD' },
  { label: '取消预约', value: 'APPOINTMENT_CANCEL' },
  { label: '结束咨询', value: 'THREAD_CLOSE' },
  { label: '内容下架', value: 'CONTENT_HIDE' }
]

async function fetchLogs() {
  loading.value = true
  try {
    const res = await adminListAuditLogs(pageNum.value, pageSize.value, filter.userId, filter.action || undefined)
    if (res.code === 0) {
      logs.value = res.data.list || []
      total.value = res.data.total
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } finally {
    loading.value = false
  }
}

function getActionLabel(action: string) {
  return actions.find(a => a.value === action)?.label || action
}

onMounted(fetchLogs)
</script>

<template>
  <div class="page">
    <div class="header">
      <h2 class="title">审计日志</h2>
    </div>

    <div class="toolbar">
      <el-input-number v-model="filter.userId" placeholder="用户ID" style="width: 150px" :controls="false" clearable />
      <el-select v-model="filter.action" placeholder="操作类型" clearable style="width: 180px">
        <el-option v-for="a in actions" :key="a.value" :label="a.label" :value="a.value" />
      </el-select>
      <el-button type="primary" @click="fetchLogs" :loading="loading">查询</el-button>
    </div>

    <el-card>
      <el-table :data="logs" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="action" label="操作类型" width="150">
          <template #default="{ row }">
            <el-tag size="small">{{ getActionLabel(row.action) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标ID" width="100" />
        <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column prop="createTime" label="操作时间" width="180">
          <template #default="{ row }">{{ formatFullTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchLogs"
        />
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
.title {
  margin: 0;
  font-size: 20px;
}
.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>