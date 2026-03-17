<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listNotifications, readAllNotifications, readNotification, type NotificationItem } from '../../../services/notification'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<NotificationItem[]>([])
const filter = ref<'ALL' | 'UNREAD' | 'READ'>('ALL')

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
  } else {
    ElMessage.error(res.msg || '标记已读失败')
  }
}

async function onReadAll() {
  const res = await readAllNotifications()
  if (res.code === 0) {
    ElMessage.success('已全部标记已读')
    await refresh()
  } else {
    ElMessage.error(res.msg || '操作失败')
  }
}

onMounted(() => refresh())
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <el-segmented v-model="filter" :options="['ALL', 'UNREAD', 'READ']" />
      <el-button type="primary" @click="refresh" :loading="loading">刷新</el-button>
      <el-button type="success" plain @click="onReadAll">全部已读</el-button>
    </div>

    <el-card>
      <el-table :data="list" v-loading="loading" style="width: 100%" @row-click="onRead">
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="content" label="内容" min-width="260" />
        <el-table-column prop="type" label="类型" width="180" />
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.createTime).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="readFlag" label="已读" width="80">
          <template #default="{ row }">
            {{ row.readFlag ? '是' : '否' }}
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

