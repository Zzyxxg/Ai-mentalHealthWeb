<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listConsultants, createAppointment, createConsultThread, type Counselor } from '../../../services/consult'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const consultants = ref<Counselor[]>([])
const keyword = ref('')

// 预约相关
const bookingDialogVisible = ref(false)
const bookingForm = ref({
  counselorUserId: 0,
  startTime: '',
  durationMinutes: 45,
  note: ''
})
const selectedConsultant = ref<Counselor | null>(null)
const submitting = ref(false)

// 咨询相关
const consultDialogVisible = ref(false)
const consultForm = ref({
  counselorUserId: 0,
  topic: '',
  content: ''
})
const submittingConsult = ref(false)

async function fetchConsultants() {
  loading.value = true
  try {
    const res = await listConsultants(keyword.value)
    if (res.code === 0) {
      consultants.value = res.data
    } else {
      ElMessage.error(res.msg || '获取咨询师列表失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '网络错误')
  } finally {
    loading.value = false
  }
}

function openBookingDialog(c: Counselor) {
  selectedConsultant.value = c
  bookingForm.value = {
    counselorUserId: c.userId,
    startTime: dayjs().add(1, 'day').hour(10).minute(0).second(0).format('YYYY-MM-DD HH:mm:ss'),
    durationMinutes: 45,
    note: ''
  }
  bookingDialogVisible.value = true
}

async function handleBooking() {
  if (!bookingForm.value.startTime) {
    ElMessage.warning('请选择预约时间')
    return
  }
  
  submitting.value = true
  try {
    const res = await createAppointment({
      ...bookingForm.value,
      startTime: dayjs(bookingForm.value.startTime).valueOf()
    })
    if (res.code === 0) {
      ElMessage.success('预约成功！')
      bookingDialogVisible.value = false
    } else {
      ElMessage.error(res.msg || '预约失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '网络错误')
  } finally {
    submitting.value = false
  }
}

function openConsultDialog(c: Counselor) {
  selectedConsultant.value = c
  consultForm.value = {
    counselorUserId: c.userId,
    topic: '',
    content: ''
  }
  consultDialogVisible.value = true
}

async function handleConsult() {
  if (!consultForm.value.topic.trim()) {
    ElMessage.warning('请输入咨询主题')
    return
  }
  if (!consultForm.value.content.trim()) {
    ElMessage.warning('请输入咨询内容')
    return
  }
  
  submittingConsult.value = true
  try {
    const res = await createConsultThread(consultForm.value)
    if (res.code === 0) {
      ElMessage.success('咨询发起成功！')
      consultDialogVisible.value = false
      router.push({ name: 'student-consultation-detail', params: { id: res.data.id } })
    } else {
      ElMessage.error(res.msg || '发起咨询失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '网络错误')
  } finally {
    submittingConsult.value = false
  }
}

onMounted(() => {
  fetchConsultants()
})
</script>

<template>
  <div class="container">
    <div class="header">
      <h2 class="title">寻找咨询师</h2>
      <div class="search-box">
        <el-input
          v-model="keyword"
          placeholder="搜索咨询师姓名、专长..."
          clearable
          @clear="fetchConsultants"
          @keyup.enter="fetchConsultants"
        >
          <template #append>
            <el-button @click="fetchConsultants">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <el-alert
      v-if="!consultants.length && !loading"
      title="暂无咨询师数据"
      type="info"
      show-icon
      :closable="false"
      style="margin-bottom: 20px"
    />

    <div v-else class="consultant-list" v-loading="loading">
      <el-card v-for="c in consultants" :key="c.id" class="consultant-card" shadow="hover">
        <div class="card-header">
          <el-avatar :size="50" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
          <div class="info">
            <div class="name-row">
              <span class="name">{{ c.realName }}</span>
              <el-tag size="small" effect="plain">{{ c.title }}</el-tag>
            </div>
            <div class="expertise">擅长：{{ c.expertise }}</div>
          </div>
        </div>
        <div class="intro">{{ c.intro }}</div>
        <div class="actions">
          <el-button type="success" plain size="small" @click="openConsultDialog(c)">发起咨询</el-button>
          <el-button type="primary" plain size="small" @click="openBookingDialog(c)">预约咨询</el-button>
        </div>
      </el-card>
    </div>

    <!-- 咨询弹窗 -->
    <el-dialog
      v-model="consultDialogVisible"
      :title="`发起咨询 - ${selectedConsultant?.realName}`"
      width="500px"
      destroy-on-close
    >
      <el-form :model="consultForm" label-position="top">
        <el-form-item label="咨询主题" required>
          <el-input v-model="consultForm.topic" placeholder="请输入咨询主题" />
        </el-form-item>
        <el-form-item label="咨询内容" required>
          <el-input
            v-model="consultForm.content"
            type="textarea"
            placeholder="请详细描述您的问题..."
            :rows="5"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="consultDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submittingConsult" @click="handleConsult">发送咨询</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 预约弹窗 -->
    <el-dialog
      v-model="bookingDialogVisible"
      :title="`预约咨询 - ${selectedConsultant?.realName}`"
      width="400px"
      destroy-on-close
    >
      <el-form :model="bookingForm" label-position="top">
        <el-form-item label="咨询开始时间" required>
          <el-date-picker
            v-model="bookingForm.startTime"
            type="datetime"
            placeholder="选择日期时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="咨询时长(分钟)">
          <el-select v-model="bookingForm.durationMinutes" style="width: 100%">
            <el-option label="30分钟" :value="30" />
            <el-option label="45分钟" :value="45" />
            <el-option label="60分钟" :value="60" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input
            v-model="bookingForm.note"
            type="textarea"
            placeholder="简单描述您的问题或需求..."
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="bookingDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleBooking">确认预约</el-button>
        </div>
      </template>
    </el-dialog>
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
  color: var(--el-text-color-primary);
  margin: 0;
}

.search-box {
  width: 300px;
}

.consultant-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.consultant-card {
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.name {
  font-size: 16px;
  font-weight: 600;
}

.expertise {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.intro {
  font-size: 14px;
  color: var(--el-text-color-regular);
  margin-bottom: 16px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 63px; /* 3 lines * 1.5 * 14px */
}

.actions {
  display: flex;
  justify-content: flex-end;
  margin-top: auto;
}
</style>
