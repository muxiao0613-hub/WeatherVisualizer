<template>
  <el-card class="line-chart-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span class="card-title">24小时趋势</span>
        <el-radio-group v-model="activeTab" size="small">
          <el-radio-button value="temperature">温度</el-radio-button>
          <el-radio-button value="humidity">湿度</el-radio-button>
          <el-radio-button value="windSpeed">风速</el-radio-button>
        </el-radio-group>
      </div>
    </template>

    <el-skeleton v-if="loading" :loading="loading" animated>
      <template #template>
        <el-skeleton-item variant="rect" style="width: 100%; height: 300px;" />
      </template>
    </el-skeleton>

    <div v-else ref="chartRef" class="chart-container"></div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import { usePreferenceStore } from '@/stores/preference'
import type { HourlyForecastDTO } from '@/types'
import dayjs from 'dayjs'

interface Props {
  data: HourlyForecastDTO[]
  loading: boolean
}

const props = defineProps<Props>()

const preferenceStore = usePreferenceStore()
const chartRef = ref<HTMLElement>()
const chartInstance = ref<ECharts>()
const activeTab = ref('temperature')

const initChart = () => {
  if (!chartRef.value) return

  chartInstance.value = echarts.init(chartRef.value)
  updateChart()

  window.addEventListener('resize', handleResize)
}

const updateChart = () => {
  if (!chartInstance.value || !props.data.length) return

  const times = props.data.map(item => dayjs(item.time).format('HH:mm'))
  let values: number[] = []
  let unit = ''

  switch (activeTab.value) {
    case 'temperature':
      values = props.data.map(item => {
        if (preferenceStore.preferences.temperatureUnit === 'F') {
          return Math.round((item.temp * 9/5) + 32)
        }
        return Math.round(item.temp)
      })
      unit = preferenceStore.preferences.temperatureUnit
      break
    case 'humidity':
      values = props.data.map(item => item.humidity)
      unit = '%'
      break
    case 'windSpeed':
      values = props.data.map(item => {
        if (preferenceStore.preferences.windSpeedUnit === 'km/h') {
          return parseFloat((item.windSpeed * 3.6).toFixed(1))
        }
        return parseFloat(item.windSpeed.toFixed(1))
      })
      unit = preferenceStore.preferences.windSpeedUnit
      break
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const param = params[0]
        return `${param.name}<br/>${param.seriesName}: ${param.value}${unit}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: times,
      axisLine: {
        lineStyle: {
          color: '#e4e7ed'
        }
      },
      axisLabel: {
        color: '#909399',
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#909399',
        fontSize: 12,
        formatter: `{value}${unit}`
      },
      splitLine: {
        lineStyle: {
          color: '#f5f7fa'
        }
      }
    },
    series: [
      {
        name: activeTab.value === 'temperature' ? '温度' : activeTab.value === 'humidity' ? '湿度' : '风速',
        type: 'line',
        smooth: true,
        data: values,
        lineStyle: {
          width: 3,
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ])
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
            { offset: 1, color: 'rgba(118, 75, 162, 0.05)' }
          ])
        },
        itemStyle: {
          color: '#667eea'
        }
      }
    ]
  }

  chartInstance.value.setOption(option)
}

const handleResize = () => {
  chartInstance.value?.resize()
}

watch(activeTab, () => {
  updateChart()
})

watch(() => props.data, () => {
  nextTick(() => {
    updateChart()
  })
}, { deep: true })

onMounted(() => {
  nextTick(() => {
    initChart()
  })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance.value?.dispose()
})
</script>

<style scoped>
.line-chart-card {
  border-radius: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  width: 100%;
  height: 300px;
}
</style>
