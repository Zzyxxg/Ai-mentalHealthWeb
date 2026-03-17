<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { adminPageThreads, adminHideThread } from '../../../services/admin'
import { getConsultThread, type ConsultThread, type ConsultMessage } from '../../../services/consult'
import { formatConsultThreadStatus, formatDateTime, formatRole } from '../../../utils/format'
import { Warning } from '@element-plus/icons-vue'

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const threads = ref<any[]>([])

const detailVisible = ref(false)
const detailLoading = ref(false)
const currentThread = ref<ConsultThread | null>(null)

const hideDialogVisible = ref(false)
const hideFormRef = ref<FormInstance>()
const hideForm = reactive({
  id: 0,
  reason: ''
})

const hideRules = reactive<FormRules>({
  reason: [
    { required: true, message: '请输入下架原因', trigger: 'blur' },
    { min: 2, max: 200, message: '原因长度在 2 到 200 个字符', trigger: 'blur' }
  ]
})

async function fetchThreads() {
  loading.value = true
  try {
    const res = await adminPageThreads(pageNum.value, pageSize.value)
    if (res.code === 0) {
      threads.value = res.data.list || []
      total.value = res.data.total
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } finally {
    loading.value = false
  }
}

async function showDetail(row: any) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getConsultThread(row.id)
    if (res.code === 0) {
      currentThread.value = res.data
    } else {
      ElMessage.error(res.msg || '获取详情失败')
    }
  } finally {
    detailLoading.value = false
  }
}

function openHideDialog(id: number) {
  hideForm.id = id
  hideForm.reason = ''
  hideDialogVisible.value = true
}

async function handleHide() {
  if (!hideFormRef.value) return
  await hideFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const res = await adminHideThread(hideForm.id, hideForm.reason)
      if (res.code === 0) {
        ElMessage.success('下架成功')
        hideDialogVisible.value = false
        if (currentThread.value && currentThread.value.id === hideForm.id) {
          currentThread.value.hidden = true
        }
        await fetchThreads()
      } else {
        ElMessage.error(res.msg || '下架失败')
      }
    } catch (e: any) {
      ElMessage.error(e?.message || '网络错误')
    }
  })
}

onMounted(fetchThreads)
</script>

<template>
  <div class="page">
    <div class="header">
      <h2 class="title">咨询会话管理</h2>
    </div>

    <el-card>
      <el-table :data="threads" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="topic" label="主题" min-width="200" />
        <el-table-column prop="counselorName" label="咨询师" width="120" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ formatConsultThreadStatus(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否隐藏" width="100">
          <template #default="{ row }">
            <el-tag :type="row.hidden ? 'danger' : 'success'">
              {{ row.hidden ? '已下架' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="showDetail(row)">查看详情</el-button>
            <el-button 
              size="small" 
              type="danger" 
              link 
              @click="openHideDialog(row.id)"
              :disabled="row.hidden"
            >
              下架
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchThreads"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-drawer v-model="detailVisible" title="咨询详情" size="600px" destroy-on-close>
      <div v-loading="detailLoading" class="detail-container">
        <template v-if="currentThread">
          <div class="thread-info">
            <div class="info-item"><span class="label">主题：</span>{{ currentThread.topic }}</div>
            <div class="info-item">
              <span class="label">状态：</span>
              <el-tag size="small">{{ formatConsultThreadStatus(currentThread.status) }}</el-tag>
              <el-tag v-if="currentThread.hidden" type="danger" size="small" style="margin-left: 8px">已下架</el-tag>
            </div>
            <div v-if="currentThread.hidden" class="info-item hide-reason">
              <span class="label">下架原因：</span>{{ currentThread.hiddenReason || '违规内容' }}
            </div>
          </div>

          <el-divider>咨询记录</el-divider>

          <div class="message-list">
            <div 
              v-for="msg in currentThread.messages" 
              :key="msg.id" 
              class="message-item"
              :class="{ 'is-hidden': msg.hidden }"
            >
              <div class="msg-header">
                <span class="sender">{{ formatRole(msg.senderRole) }}</span>
                <span class="time">{{ formatDateTime(msg.createTime) }}</span>
                <el-tag v-if="msg.hidden" type="danger" size="tiny">已隐藏</el-tag>
              </div>
              <div class="msg-content">
                <template v-if="!msg.hidden">
                  {{ msg.content }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 该内容已下架 (原因: {{ msg.hiddenReason || '违规' }})
                </template>
              </div>
            </div>
          </div>
        </template>
      </div>
      <template #footer>
        <div style="flex: auto">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button 
            v-if="currentThread && !currentThread.hidden" 
            type="danger" 
            @click="openHideDialog(currentThread.id)"
          >
            下架整个会话
          </el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 下架原因弹窗 -->
    <el-dialog v-model="hideDialogVisible" title="下架咨询会话" width="400px">
      <el-form ref="hideFormRef" :model="hideForm" :rules="hideRules" label-position="top">
        <el-form-item label="下架原因" prop="reason">
          <el-input 
            v-model="hideForm.reason" 
            type="textarea" 
            placeholder="请输入下架原因，将展示给相关人员" 
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="hideDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleHide">确认下架</el-button>
      </template>
    </el-dialog>
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
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.detail-container {
  padding: 0 20px;
}
.thread-info {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.info-item {
  font-size: 14px;
}
.info-item .label {
  color: var(--el-text-color-secondary);
  font-weight: 600;
}
.hide-reason {
  color: var(--el-color-danger);
  background-color: var(--el-color-danger-light-9);
  padding: 8px;
  border-radius: 4px;
}
.message-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.message-item {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 12px;
  background-color: #fafafa;
}
.message-item.is-hidden {
  opacity: 0.7;
  border-color: var(--el-color-danger-light-5);
}
.msg-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
}
.msg-header .sender {
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.msg-header .time {
  color: var(--el-text-color-secondary);
}
.msg-content {
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
}
.is-hidden .msg-content {
  color: var(--el-color-danger);
  font-style: italic;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>