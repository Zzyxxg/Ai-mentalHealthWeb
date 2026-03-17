<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createScheduleSlots, deleteScheduleSlot, listScheduleSlots, updateScheduleSlotStatus, type ScheduleSlot } from '../../../services/consult'

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

const range = ref<[Date, Date]>([startOfDay(new Date()), endOfDay(new Date())])
const loading = ref(false)
const slots = ref<ScheduleSlot[]>([])

const createDialogOpen = ref(false)
const createForm = ref({
  date: new Date(),
  startTime: '09:00',
  endTime: '10:00',
})

const requestParams = computed(() => ({
  startDate: toEpochMillis(startOfDay(range.value[0])),
  endDate: toEpochMillis(endOfDay(range.value[1])),
}))

async function refresh() {
  loading.value = true
  try {
    const res = await listScheduleSlots(requestParams.value)
    if (res.code === 0) {
      slots.value = res.data || []
    } else {
      ElMessage.error(res.msg || '加载排班失败')
    }
  } finally {
    loading.value = false
  }
}

async function onCreate() {
  const date = createForm.value.date
  const yyyyMmDd = date.toISOString().slice(0, 10)
  const payload = [{ date: yyyyMmDd, startTime: createForm.value.startTime, endTime: createForm.value.endTime }]
  const res = await createScheduleSlots(payload)
  if (res.code === 0) {
    ElMessage.success('创建成功')
    createDialogOpen.value = false
    await refresh()
  } else {
    ElMessage.error(res.msg || '创建失败')
  }
}

async function onClose(slot: ScheduleSlot) {
  const res = await updateScheduleSlotStatus(slot.id, 'UNAVAILABLE')
  if (res.code === 0) {
    ElMessage.success('已关闭')
    await refresh()
  } else {
    ElMessage.error(res.msg || '操作失败')
  }
}

async function onOpen(slot: ScheduleSlot) {
  const res = await updateScheduleSlotStatus(slot.id, 'AVAILABLE')
  if (res.code === 0) {
    ElMessage.success('已开启')
    await refresh()
  } else {
    ElMessage.error(res.msg || '操作失败')
  }
}

async function onDelete(slot: ScheduleSlot) {
  await ElMessageBox.confirm('确认删除该时段？（软删）', '提示', { type: 'warning' })
  const res = await deleteScheduleSlot(slot.id)
  if (res.code === 0) {
    ElMessage.success('已删除')
    await refresh()
  } else {
    ElMessage.error(res.msg || '删除失败')
  }
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <el-date-picker v-model="range" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
      <el-button type="primary" @click="refresh" :loading="loading">刷新</el-button>
      <el-button type="success" @click="createDialogOpen = true">新增时段</el-button>
    </div>

    <el-card>
      <el-table :data="slots" v-loading="loading" style="width: 100%">
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="startTime" label="开始" width="120" />
        <el-table-column prop="endTime" label="结束" width="120" />
        <el-table-column prop="status" label="状态" width="140" />
        <el-table-column label="操作" min-width="220">
          <template #default="{ row }">
            <el-button size="small" type="warning" plain @click="onClose(row)" :disabled="row.status === 'OCCUPIED' || row.status === 'UNAVAILABLE'">
              关闭
            </el-button>
            <el-button size="small" type="success" plain @click="onOpen(row)" :disabled="row.status === 'OCCUPIED' || row.status === 'AVAILABLE'">
              开启
            </el-button>
            <el-button size="small" type="danger" plain @click="onDelete(row)" :disabled="row.status === 'OCCUPIED'">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="createDialogOpen" title="新增排班时段" width="420px">
      <el-form label-width="80px">
        <el-form-item label="日期">
          <el-date-picker v-model="createForm.date" type="date" />
        </el-form-item>
        <el-form-item label="开始">
          <el-input v-model="createForm.startTime" placeholder="09:00" />
        </el-form-item>
        <el-form-item label="结束">
          <el-input v-model="createForm.endTime" placeholder="10:00" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogOpen = false">取消</el-button>
        <el-button type="primary" @click="onCreate">确定</el-button>
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
</style>

