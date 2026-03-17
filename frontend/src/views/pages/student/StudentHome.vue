<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { listConsultants, createAppointment, createConsultThread, listScheduleSlots, type Counselor, type ScheduleSlot } from '../../../services/consult'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const consultants = ref<Counselor[]>([])
const keyword = ref('')

// 预约相关
const bookingDialogVisible = ref(false)
const bookingFormRef = ref<FormInstance>()
const bookingForm = reactive({
  counselorUserId: 0,
  slotId: 0,
  startTime: '',
  durationMinutes: 0,
  note: ''
})
const selectedConsultant = ref<Counselor | null>(null)
const submitting = ref(false)
const slotsLoading = ref(false)
const availableSlots = ref<ScheduleSlot[]>([])
const maxDuration = ref(240)

const canSubmitBooking = computed(() => {
  return availableSlots.value.length > 0 && bookingForm.slotId > 0 && Boolean(bookingForm.startTime)
})

const bookingRules = reactive<FormRules>({
  slotId: [{ required: true, message: '请选择可预约时段', trigger: 'change' }],
  durationMinutes: [
    { required: true, message: '请输入预约时长', trigger: 'blur' },
    { type: 'number', min: 15, message: '时长至少 15 分钟', trigger: 'blur' }
  ]
})

// 咨询相关
const consultDialogVisible = ref(false)
const consultFormRef = ref<FormInstance>()
const consultForm = reactive({
  counselorUserId: 0,
  topic: '',
  content: ''
})
const submittingConsult = ref(false)

const consultRules = reactive<FormRules>({
  topic: [
    { required: true, message: '请输入咨询主题', trigger: 'blur' },
    { min: 2, max: 100, message: '主题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入咨询内容', trigger: 'blur' },
    { min: 10, message: '内容长度至少 10 个字符', trigger: 'blur' }
  ]
})

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

function normalizeTimeStr(t: string) {
  const s = (t || '').trim()
  if (!s) return s
  if (s.length === 5) return `${s}:00`
  return s
}

function buildSlotLabel(slot: ScheduleSlot) {
  const start = normalizeTimeStr(slot.startTime)
  const end = normalizeTimeStr(slot.endTime)
  return `${slot.date} ${start.slice(0, 5)}-${end.slice(0, 5)}`
}

function applySlot(slot: ScheduleSlot) {
  const start = `${slot.date} ${normalizeTimeStr(slot.startTime)}`
  const end = `${slot.date} ${normalizeTimeStr(slot.endTime)}`
  const startAt = dayjs(start)
  const endAt = dayjs(end)
  bookingForm.startTime = startAt.format('YYYY-MM-DD HH:mm:ss')
  
  const slotDuration = Math.max(0, endAt.diff(startAt, 'minute'))
  // 设置时长上限，最大不超过240分钟（4小时），且不超过该时段的全长
  maxDuration.value = Math.min(240, slotDuration)
  // 默认时长为60分钟，或该时段的最大可用时长（如果最大时长小于60分钟）
  bookingForm.durationMinutes = Math.min(60, maxDuration.value)
}

async function openBookingDialog(c: Counselor) {
  selectedConsultant.value = c
  bookingForm.counselorUserId = c.userId
  bookingForm.slotId = 0
  bookingForm.startTime = ''
  bookingForm.durationMinutes = 0
  bookingForm.note = ''
  bookingDialogVisible.value = true

  slotsLoading.value = true
  availableSlots.value = []
  try {
    const startDate = dayjs().startOf('day').valueOf()
    const endDate = dayjs().add(30, 'day').endOf('day').valueOf()
    const res = await listScheduleSlots({ counselorUserId: c.userId, startDate, endDate })
    if (res.code === 0) {
      const now = dayjs()
      const slots = (res.data || [])
        .filter(s => s.status === 'AVAILABLE')
        .filter(s => dayjs(`${s.date} ${normalizeTimeStr(s.startTime)}`).isAfter(now))
        .sort((a, b) => buildSlotLabel(a).localeCompare(buildSlotLabel(b)))

      availableSlots.value = slots

      if (slots.length > 0) {
        bookingForm.slotId = slots[0].id
        applySlot(slots[0])
      }
    } else {
      ElMessage.error(res.msg || '加载可预约时段失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '网络错误')
  } finally {
    slotsLoading.value = false
  }
}

function onSlotChange(slotId: number) {
  const slot = availableSlots.value.find(s => s.id === slotId)
  if (!slot) return
  applySlot(slot)
}

async function handleBooking() {
  if (!bookingFormRef.value) return
  
  await bookingFormRef.value.validate(async (valid) => {
    if (!valid) return

    if (!canSubmitBooking.value) {
      ElMessage.warning('该咨询师暂未开放预约（未配置排班或无可用时段）')
      return
    }

    submitting.value = true
    try {
      const res = await createAppointment({
        counselorUserId: bookingForm.counselorUserId,
        durationMinutes: bookingForm.durationMinutes,
        note: bookingForm.note,
        startTime: dayjs(bookingForm.startTime).valueOf()
      })
      if (res.code === 0) {
        ElMessage.success('预约成功！')
        window.dispatchEvent(new Event('mh:notifications-updated'))
        bookingDialogVisible.value = false
      } else {
        ElMessage.error(res.msg || '预约失败')
      }
    } catch (e: any) {
      ElMessage.error(e?.message || '网络错误')
    } finally {
      submitting.value = false
    }
  })
}

function openConsultDialog(c: Counselor) {
  selectedConsultant.value = c
  consultForm.counselorUserId = c.userId
  consultForm.topic = ''
  consultForm.content = ''
  consultDialogVisible.value = true
}

async function handleConsult() {
  if (!consultFormRef.value) return

  await consultFormRef.value.validate(async (valid) => {
    if (!valid) return

    submittingConsult.value = true
    try {
      const res = await createConsultThread(consultForm)
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
  })
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
      <el-form ref="consultFormRef" :model="consultForm" :rules="consultRules" label-position="top">
        <el-form-item label="咨询主题" prop="topic">
          <el-input v-model="consultForm.topic" placeholder="请输入咨询主题" />
        </el-form-item>
        <el-form-item label="咨询内容" prop="content">
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
      <el-form ref="bookingFormRef" :model="bookingForm" :rules="bookingRules" label-position="top">
        <el-form-item label="可预约时段" prop="slotId">
          <el-select
            v-model="bookingForm.slotId"
            style="width: 100%"
            placeholder="请选择可预约时段"
            :loading="slotsLoading"
            :disabled="slotsLoading || availableSlots.length === 0"
            @change="onSlotChange"
          >
            <el-option
              v-for="s in availableSlots"
              :key="s.id"
              :label="buildSlotLabel(s)"
              :value="s.id"
            />
          </el-select>
          <el-alert
            v-if="!slotsLoading && availableSlots.length === 0"
            title="该咨询师暂未开放预约（未配置排班或无可用时段）"
            type="warning"
            show-icon
            :closable="false"
            style="margin-top: 10px"
          />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-input v-model="bookingForm.startTime" disabled />
        </el-form-item>
        <el-form-item label="时长(分钟)" prop="durationMinutes">
          <el-input-number
            v-model="bookingForm.durationMinutes"
            :min="15"
            :max="maxDuration"
            :step="15"
            style="width: 100%"
          />
          <div class="duration-hint">可选范围: 15 - {{ maxDuration }} 分钟</div>
        </el-form-item>
        <el-form-item label="备注说明" prop="note">
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
          <el-button type="primary" :loading="submitting" :disabled="!canSubmitBooking" @click="handleBooking">确认预约</el-button>
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

.duration-hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}
</style>
