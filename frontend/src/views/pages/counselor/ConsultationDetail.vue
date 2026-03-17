<script setup lang="ts">
import { onMounted, ref, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getConsultThread, sendConsultMessage, finishConsultThread, type ConsultThread, type ConsultMessage } from '../../../services/consult'
import { formatDateTime } from '../../../utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { useAuthStore } from '../../../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const threadId = Number(route.params.id)
const loading = ref(false)
const thread = ref<ConsultThread | null>(null)
const messages = ref<ConsultMessage[]>([])
const messageContent = ref('')
const sending = ref(false)
const messageListRef = ref<HTMLElement>()

async function fetchDetail() {
  loading.value = true
  try {
    const res = await getConsultThread(threadId)
    if (res.code === 0) {
      thread.value = res.data
      messages.value = res.data.messages || []
      scrollToBottom()
    } else {
      ElMessage.error(res.msg || '获取详情失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '网络错误')
  } finally {
    loading.value = false
  }
}

async function handleSend() {
  const content = messageContent.value.trim()
  if (!content) {
    ElMessage.warning('请输入回复内容')
    return
  }
  if (content.length > 1000) {
    ElMessage.warning('回复长度不能超过 1000 个字符')
    return
  }

  sending.value = true
  try {
    const res = await sendConsultMessage({ 
      threadId: threadId,
      content: content 
    })
    if (res.code === 0) {
      messages.value.push(res.data)
      messageContent.value = ''
      scrollToBottom()
      // 如果之前是 UNPROCESSED，发送消息后会自动变成 PROCESSING，前端手动更新状态以避免刷新
      if (thread.value && thread.value.status === 'UNPROCESSED') {
        thread.value.status = 'PROCESSING'
      }
    } else {
      ElMessage.error(res.msg || '发送失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '网络错误')
  } finally {
    sending.value = false
  }
}

async function handleFinish() {
  try {
    await ElMessageBox.confirm('确定要结束本次咨询吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await finishConsultThread(threadId)
    if (res.code === 0) {
      ElMessage.success('咨询已结束')
      if (thread.value) {
        thread.value.status = 'CLOSED'
      }
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    // 用户取消
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

function isMyMessage(msg: ConsultMessage) {
  return msg.senderRole === 'CONSULTANT'
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="container">
    <div class="header">
      <div class="header-left">
        <el-button @click="router.back()" icon="el-icon-back">返回</el-button>
        <div class="title-info" v-if="thread">
          <h2 class="title">{{ thread.topic }}</h2>
          <span class="status-tag" :class="thread.status.toLowerCase()">
            {{ thread.status === 'CLOSED' ? '已结束' : (thread.status === 'PROCESSING' ? '进行中' : '未处理') }}
          </span>
        </div>
      </div>
      <div class="header-right" v-if="thread && thread.status !== 'CLOSED'">
        <el-button type="danger" plain size="small" @click="handleFinish">结束咨询</el-button>
      </div>
    </div>

    <div class="chat-window">
      <div class="message-list" ref="messageListRef">
        <div v-if="messages.length === 0" class="empty-tip">暂无消息</div>
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-item"
          :class="{ 'my-message': isMyMessage(msg) }"
        >
          <div class="message-content">
            <div class="sender-info">
              <span class="sender-name">{{ msg.senderRole === 'CONSULTANT' ? '我' : (thread?.studentName || '学生') }}</span>
              <span class="time">{{ formatDateTime(msg.createTime) }}</span>
            </div>
            <div class="bubble" :class="{ 'is-hidden': msg.hidden }">
              <template v-if="!msg.hidden">
                {{ msg.content }}
              </template>
              <template v-else>
                <el-icon><Warning /></el-icon> 该内容已因违规被下架
              </template>
            </div>
          </div>
        </div>
      </div>

      <div class="input-area" v-if="thread && thread.status !== 'CLOSED'">
        <el-input
          v-model="messageContent"
          type="textarea"
          :rows="3"
          placeholder="请输入回复内容..."
          @keyup.enter.ctrl="handleSend"
        />
        <div class="action-bar">
          <span class="tip">按 Ctrl + Enter 发送</span>
          <el-button type="primary" :loading="sending" @click="handleSend">回复</el-button>
        </div>
      </div>
      <div v-else class="finished-tip">
        咨询已结束，无法发送消息
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.title-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title {
  margin: 0;
  font-size: 18px;
}

.status-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  background-color: #f0f2f5;
  color: #909399;
}

.status-tag.processing {
  background-color: #ecf5ff;
  color: #409eff;
}

.status-tag.closed {
  background-color: #f0f9eb;
  color: #67c23a;
}

.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  overflow: hidden;
  background-color: #f5f7fa;
}

.message-list {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty-tip {
  text-align: center;
  color: var(--el-text-color-secondary);
  margin-top: 40px;
}

.message-item {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message-item.my-message {
  align-self: flex-end;
  align-items: flex-end;
}

.sender-info {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 4px;
}

.my-message .sender-info {
  text-align: right;
}

.bubble {
  padding: 10px 14px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  line-height: 1.5;
  white-space: pre-wrap;
}

.bubble.is-hidden {
  background-color: #f4f4f5;
  color: #909399;
  font-style: italic;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.my-message .bubble {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary-dark-2);
}

.input-area {
  padding: 16px;
  background-color: #fff;
  border-top: 1px solid var(--el-border-color);
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 8px;
  gap: 12px;
}

.tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.finished-tip {
  padding: 16px;
  text-align: center;
  color: var(--el-text-color-secondary);
  background-color: #fff;
  border-top: 1px solid var(--el-border-color);
}
</style>
