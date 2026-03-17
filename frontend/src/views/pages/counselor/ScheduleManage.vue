<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { createScheduleSlots, deleteScheduleSlot, listScheduleSlots, updateScheduleSlotStatus, type ScheduleSlot } from '../../../services/consult'
import { formatScheduleSlotStatus, formatDateTime } from '../../../utils/format'
import dayjs from 'dayjs'

function getStatusType(status: string) {
  if (status === 'AVAILABLE') return 'success'
  if (status === 'OCCUPIED') return 'warning'
  return 'info'
}

function toEpochMillis(d: Date) {
  return d.getTime()
}

function startOfDay(d: Date) {
  const x = new Date(d)
  x.setHours(0, 0, 0, 0)
  return x
}

function endOfDay(d: Date) {
  const x = new Date(d)
  x.setHours(23, 59, 59, 999)
  return x
}

const range = ref<[Date, Date] | null>([startOfDay(new Date()), endOfDay(new Date())])
const loading = ref(false)
const slots = ref<ScheduleSlot[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const createDialogOpen = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = reactive({
  dates: [] as Date[], // 修改为多选
  startTime: '09:00',
  endTime: '10:00',
})

const createRules = reactive<FormRules>({
  dates: [{ type: 'array', required: true, message: '请选择日期', trigger: 'change' }],
  startTime: [
    { required: true, message: '请输入开始时间', trigger: 'blur' }
  ],
  endTime: [
    { required: true, message: '请输入结束时间', trigger: 'blur' }
  ]
})

const requestParams = computed(() => {
  if (range.value && range.value.length === 2) {
    return {
      startDate: toEpochMillis(startOfDay(range.value[0])),
      endDate: toEpochMillis(endOfDay(range.value[1])),
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
  }
  // 默认查询很大范围的排班
  return {
    startDate: 0,
    endDate: 4102444800000, // 2100-01-01
    pageNum: pageNum.value,
    pageSize: pageSize.value
  }
})

async function refresh() {
  loading.value = true
  try {
    const res = await listScheduleSlots(requestParams.value as any)
    if (res.code === 0) {
      if (Array.isArray(res.data)) {
        slots.value = res.data
        total.value = res.data.length
      } else {
        const data = res.data as any
        slots.value = data.list || []
        total.value = data.total || 0
      }
    } else {
      ElMessage.error(res.msg || '加载排班失败')
    }
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  pageNum.value = page
  refresh()
}

function handleSizeChange(size: number) {
  pageSize.value = size
  pageNum.value = 1
  refresh()
}

function handleRangeChange() {
  pageNum.value = 1
  refresh()
}

async function onCreate() {
  if (!createFormRef.value) return
  
  await createFormRef.value.validate(async (valid) => {
    if (!valid) return

    // 校验结束时间是否晚于开始时间
    if (createForm.endTime <= createForm.startTime) {
      ElMessage.warning('结束时间必须晚于开始时间')
      return
    }

    // 校验时间是否是过去的时间
    const now = dayjs()
    const isPastTime = createForm.dates.some(date => {
      const dateTimeStr = `${dayjs(date).format('YYYY-MM-DD')} ${createForm.startTime}`
      return dayjs(dateTimeStr).isBefore(now)
    })

    if (isPastTime) {
      ElMessage.warning('不能添加过去的排班时间段')
      return
    }

    const payload = createForm.dates.map(date => ({
      date: dayjs(date).format('YYYY-MM-DD'),
      startTime: createForm.startTime,
      endTime: createForm.endTime
    }))

    const res = await createScheduleSlots(payload)
    if (res.code === 0) {
      ElMessage.success('批量创建成功')
      createDialogOpen.value = false
      // 重置表单
      createForm.dates = []
      createForm.startTime = '09:00'
      createForm.endTime = '10:00'
      await refresh()
    } else {
      ElMessage.error(res.msg || '创建失败')
    }
  })
}

async function onClose(slot: ScheduleSlot) {
  if (slot.status === 'OCCUPIED') {
    try {
      await ElMessageBox.confirm(
        '该时段已有学生预约，关闭该时段可能导致预约冲突，确认继续吗？',
        '风险提示',
        {
          confirmButtonText: '强制关闭',
          cancelButtonText: '取消',
          type: 'error'
        }
      )
    } catch {
      return
    }
  }

  const res = await updateScheduleSlotStatus(slot.id, 'UNAVAILABLE')
  if (res.code === 0) {
    ElMessage.success('已关闭')
    await refresh()
  } else {
    ElMessage.error(res.msg || '操作失败')
  }
}

async function onOpen(slot: ScheduleSlot) {
  if (slot.status === 'OCCUPIED') {
    try {
      await ElMessageBox.confirm(
        '该时段已有学生预约，强制开启将释放占用状态，确认继续吗？',
        '提示',
        {
          confirmButtonText: '强制开启',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    } catch {
      return
    }
  }

  const res = await updateScheduleSlotStatus(slot.id, 'AVAILABLE')
  if (res.code === 0) {
    ElMessage.success('已开启')
    await refresh()
  } else {
    ElMessage.error(res.msg || '操作失败')
  }
}

async function onDelete(slot: ScheduleSlot) {
  let message = '确认删除该时段？'
  let title = '提示'
  let type: any = 'warning'

  if (slot.status === 'OCCUPIED') {
    message = '该时段已有学生预约，删除排班将导致关联预约数据异常，请谨慎操作！确认删除吗？'
    title = '严重警告'
    type = 'error'
  }

  try {
    await ElMessageBox.confirm(message, title, { 
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type 
    })
    const res = await deleteScheduleSlot(slot.id)
    if (res.code === 0) {
      ElMessage.success('已删除')
      await refresh()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch {
    // cancel
  }
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <el-date-picker 
        v-model="range" 
        type="daterange" 
        range-separator="-" 
        start-placeholder="开始日期" 
        end-placeholder="结束日期" 
        clearable
        @change="handleRangeChange"
      />
      <el-button type="primary" @click="refresh" :loading="loading">刷新</el-button>
      <el-button type="success" @click="createDialogOpen = true">新增时段</el-button>
    </div>

    <el-card>
      <el-table :data="slots" v-loading="loading" style="width: 100%">
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="startTime" label="开始" width="120" />
        <el-table-column prop="endTime" label="结束" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ formatScheduleSlotStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="220">
          <template #default="{ row }">
            <el-button size="small" type="warning" plain @click="onClose(row)" :disabled="row.status === 'UNAVAILABLE'">
              关闭
            </el-button>
            <el-button size="small" type="success" plain @click="onOpen(row)" :disabled="row.status === 'AVAILABLE'">
              开启
            </el-button>
            <el-button size="small" type="danger" plain @click="onDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager" v-if="total > 0">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, sizes"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="createDialogOpen" title="新增排班时段" width="450px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="90px">
        <el-form-item label="选择日期" prop="dates">
          <el-date-picker 
            v-model="createForm.dates" 
            type="dates" 
            placeholder="支持多选日期批量添加" 
            style="width: 100%"
            :disabled-date="(time: Date) => time.getTime() < Date.now() - 86400000"
          />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-select
            v-model="createForm.startTime"
            start="08:00"
            step="00:15"
            end="22:00"
            placeholder="开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-select
            v-model="createForm.endTime"
            start="08:00"
            step="00:15"
            end="22:00"
            placeholder="结束时间"
            style="width: 100%"
            :min-time="createForm.startTime"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogOpen = false">取消</el-button>
        <el-button type="primary" @click="onCreate">确定批量添加</el-button>
      </template>
    </el-dialog>
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
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>

