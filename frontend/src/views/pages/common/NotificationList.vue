<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listNotifications, readAllNotifications, readNotification, type NotificationItem } from '../../../services/notification'
import { formatNotificationType, formatDateTime } from '../../../utils/format'
import { useAuthStore } from '../../../stores/auth'

const loading = ref(false)
const router = useRouter()
const authStore = useAuthStore()
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<NotificationItem[]>([])
const filter = ref<'ALL' | 'UNREAD' | 'READ'>('ALL')

watch(filter, () => {
  pageNum.value = 1
  refresh()
})
const options = [
  { label: '全部', value: 'ALL' },
  { label: '未读', value: 'UNREAD' },
  { label: '已读', value: 'READ' }
]

const readFlagParam = computed<0 | 1 | undefined>(() => {
  if (filter.value === 'UNREAD') return 0
  if (filter.value === 'READ') return 1
  return undefined
})

async function refresh() {
  loading.value = true
  try {
    const res = await listNotifications(pageNum.value, pageSize.value, readFlagParam.value)
    if (res.code === 0) {
      total.value = res.data.total
      list.value = res.data.list || []
    } else {
      ElMessage.error(res.msg || '加载通知失败')
    }
  } finally {
    loading.value = false
  }
}

async function onRead(item: NotificationItem) {
  if (item.readFlag) return
  const res = await readNotification(item.id)
  if (res.code === 0) {
    item.readFlag = true
    window.dispatchEvent(new Event('mh:notifications-updated'))
  } else {
    ElMessage.error(res.msg || '标记已读失败')
  }
}

function extractThreadId(text: string) {
  const m = /threadId\s*=\s*(\d+)/i.exec(text || '')
  if (!m) return null
  const id = Number(m[1])
  return Number.isFinite(id) ? id : null
}

async function handleRowClick(item: NotificationItem) {
  await onRead(item)

  const threadId = extractThreadId(`${item.title} ${item.content}`)
  if (!threadId) return

  if (authStore.role === 'CONSULTANT') {
    router.push({ name: 'counselor-consultation-detail', params: { id: threadId } })
  } else if (authStore.role === 'STUDENT') {
    router.push({ name: 'student-consultation-detail', params: { id: threadId } })
  }
}

async function onReadAll() {
  const res = await readAllNotifications()
  if (res.code === 0) {
    ElMessage.success('已全部标记已读')
    await refresh()
    window.dispatchEvent(new Event('mh:notifications-updated'))
  } else {
    ElMessage.error(res.msg || '操作失败')
  }
}

onMounted(() => refresh())
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <el-segmented v-model="filter" :options="options" />
      <el-button type="primary" @click="refresh" :loading="loading">刷新</el-button>
      <el-button type="success" plain @click="onReadAll">全部已读</el-button>
    </div>

    <el-card>
      <el-table :data="list" v-loading="loading" style="width: 100%" @row-click="handleRowClick">
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column label="用户" width="140">
          <template #default="{ row }">
            {{ row.studentName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="咨询师" width="140">
          <template #default="{ row }">
            {{ row.counselorName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="260" />
        <el-table-column label="类型" width="180">
          <template #default="{ row }">
            {{ formatNotificationType(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="已读" width="80">
          <template #default="{ row }">
            <el-tag :type="row.readFlag ? 'success' : 'info'">
              {{ row.readFlag ? '已读' : '未读' }}
            </el-tag>
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
  flex-wrap: wrap;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
