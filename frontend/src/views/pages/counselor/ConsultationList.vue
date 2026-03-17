<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { listConsultThreads, type ConsultThread } from '../../../services/consult'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const activeTab = ref('UNPROCESSED')
const loading = ref(false)
const threads = ref<ConsultThread[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function fetchThreads() {
  loading.value = true
  try {
    const res = await listConsultThreads(pageNum.value, pageSize.value, activeTab.value)
    if (res.code === 0) {
      if (Array.isArray(res.data)) {
        threads.value = res.data
        total.value = res.data.length
      } else {
        threads.value = res.data.list
        total.value = res.data.total
      }
    } else {
      ElMessage.error(res.msg || '获取咨询列表失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '网络错误')
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  pageNum.value = 1
  fetchThreads()
}

function handlePageChange(page: number) {
  pageNum.value = page
  fetchThreads()
}

function goToDetail(id: number) {
  router.push({ name: 'counselor-consultation-detail', params: { id } })
}

function formatTime(time: number) {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  fetchThreads()
})
</script>

<template>
  <div class="container">
    <div class="header">
      <h2 class="title">咨询管理</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="custom-tabs">
      <el-tab-pane label="待处理" name="UNPROCESSED" />
      <el-tab-pane label="进行中" name="PROCESSING" />
      <el-tab-pane label="已结束" name="CLOSED" />
    </el-tabs>

    <div v-loading="loading">
      <el-alert
        v-if="!threads.length && !loading"
        title="暂无相关咨询"
        type="info"
        show-icon
        :closable="false"
        style="margin-top: 20px"
      />

      <div class="thread-list" v-else>
        <el-card
          v-for="thread in threads"
          :key="thread.id"
          class="thread-card"
          shadow="hover"
          @click="goToDetail(thread.id)"
        >
          <div class="card-header">
            <div class="title-row">
              <span class="thread-title">{{ thread.topic }}</span>
              <el-tag v-if="activeTab === 'UNPROCESSED'" type="danger" size="small" effect="plain">未回复</el-tag>
            </div>
            <div class="time">{{ formatTime(thread.createTime) }}</div>
          </div>
          <div class="content-preview">{{ thread.content }}</div>
          <div class="footer">
            <span class="student">学生：{{ thread.studentName || '匿名同学' }}</span>
          </div>
        </el-card>
      </div>

      <div class="pagination" v-if="total > 0">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          v-model:current-page="pageNum"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.header {
  margin-bottom: 24px;
}

.title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.thread-list {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.thread-card {
  cursor: pointer;
  transition: all 0.3s;
}

.thread-card:hover {
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.thread-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.content-preview {
  font-size: 14px;
  color: var(--el-text-color-regular);
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

.footer {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  text-align: right;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
