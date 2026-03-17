<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { adminStats } from '../../../services/admin'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { formatAssessmentLevel } from '../../../utils/format'
import dayjs from 'dayjs'

// 注册 ECharts 必须的组件
use([
  CanvasRenderer,
  PieChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent
])

const loading = ref(false)
const days = ref(30)
const rawData = ref<any>(null)
const chartKey = ref(0)

const rangeStart = computed(() => dayjs().subtract(Math.max(days.value - 1, 0), 'day').startOf('day'))
const rangeEnd = computed(() => dayjs().startOf('day'))

const rangeLabel = computed(() => {
  const end = rangeEnd.value.format('YYYY-MM-DD')
  const start = rangeStart.value.format('YYYY-MM-DD')
  return `${start} ~ ${end}`
})

function normalizeDayValue(v: any) {
  if (v === null || v === undefined) return ''
  if (typeof v === 'string') return v
  return String(v)
}

function normalizeCountValue(v: any) {
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

// 测评结果分布饼图配置
const levelDistOption = computed(() => {
  if (!rawData.value || !rawData.value.assessmentLevelDist) return {}
  
  return {
    title: { text: '测评结果严重程度分布', subtext: `统计范围：${rangeLabel.value}`, left: 'center' },
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        name: '测评结果',
        type: 'pie',
        radius: '50%',
        data: (rawData.value.assessmentLevelDist || [])
          .map((item: any) => ({
            name: formatAssessmentLevel(String(item.level ?? 'UNKNOWN')),
            value: normalizeCountValue(item.cnt ?? item.count)
          }))
          .filter((x: any) => x.value > 0),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
})

// 趋势折线图配置
const trendOption = computed(() => {
  if (!rawData.value) return {}
  
  const apptData: Record<string, number> = {}
  const threadData: Record<string, number> = {}
  const assessData: Record<string, number> = {}
  
  ;(rawData.value.appointmentsDaily || []).forEach((item: any) => {
    const day = normalizeDayValue(item.day ?? item.date)
    if (!day) return
    apptData[day] = normalizeCountValue(item.cnt ?? item.count)
  })
  ;(rawData.value.threadsDaily || []).forEach((item: any) => {
    const day = normalizeDayValue(item.day ?? item.date)
    if (!day) return
    threadData[day] = normalizeCountValue(item.cnt ?? item.count)
  })
  ;(rawData.value.assessmentsDaily || []).forEach((item: any) => {
    const day = normalizeDayValue(item.day ?? item.date)
    if (!day) return
    assessData[day] = normalizeCountValue(item.cnt ?? item.count)
  })

  const xDays: string[] = []
  for (let i = 0; i < days.value; i++) {
    xDays.push(rangeStart.value.add(i, 'day').format('YYYY-MM-DD'))
  }

  const hasAny = Object.keys(apptData).length || Object.keys(threadData).length || Object.keys(assessData).length
  if (!hasAny) {
    return {
      title: { text: '系统业务趋势', subtext: `统计范围：${rangeLabel.value}`, left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value' },
      series: []
    }
  }
  
  return {
    title: { text: '系统业务趋势', subtext: `统计范围：${rangeLabel.value}`, left: 'center' },
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增预约', '新增咨询', '完成测评'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: xDays },
    yAxis: { type: 'value' },
    dataZoom: [{ type: 'inside' }, { type: 'slider' }],
    series: [
      {
        name: '新增预约',
        type: 'line',
        smooth: true,
        data: xDays.map(d => apptData[d] || 0)
      },
      {
        name: '新增咨询',
        type: 'line',
        smooth: true,
        data: xDays.map(d => threadData[d] || 0)
      },
      {
        name: '完成测评',
        type: 'line',
        smooth: true,
        data: xDays.map(d => assessData[d] || 0)
      }
    ]
  }
})

async function refresh() {
  loading.value = true
  try {
    const res = await adminStats(days.value)
    if (res.code === 0) {
      rawData.value = res.data
      chartKey.value++
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } finally {
    loading.value = false
  }
}

watch(days, () => {
  refresh()
})

onMounted(refresh)
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <span class="label">统计周期 (天)：</span>
      <el-input-number v-model="days" :min="7" :max="365" :step="7" />
      <el-button type="primary" @click="refresh" :loading="loading">刷新统计</el-button>
      <span class="range">统计范围：{{ rangeLabel }}</span>
    </div>

    <div class="charts-container" v-loading="loading">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card shadow="hover">
            <template v-if="rawData && (rawData.appointmentsDaily?.length || rawData.threadsDaily?.length || rawData.assessmentsDaily?.length)">
              <v-chart :key="`trend-${chartKey}`" class="chart" :option="trendOption" :update-options="{ notMerge: true }" autoresize />
            </template>
            <template v-else>
              <el-empty description="暂无趋势数据" />
            </template>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <template v-if="rawData && rawData.assessmentLevelDist?.length">
              <v-chart :key="`dist-${chartKey}`" class="chart" :option="levelDistOption" :update-options="{ notMerge: true }" autoresize />
            </template>
            <template v-else>
              <el-empty description="暂无分布数据" />
            </template>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  background: #fff;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}
.label {
  font-size: 14px;
  color: var(--el-text-color-regular);
}
.range {
  margin-left: auto;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.charts-container {
  min-height: 400px;
}
.chart {
  height: 400px;
  width: 100%;
}
</style>
