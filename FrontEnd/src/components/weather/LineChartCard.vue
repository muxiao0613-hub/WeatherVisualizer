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
const isInitialized = ref(false)
const retryCount = ref(0)

const initChart = () => {
  if (!chartRef.value) {
    console.warn('LineChartCard: chartRef is null, retrying...')
    if (retryCount.value < 3) {
      retryCount.value++
      setTimeout(() => initChart(), 100)
    }
    return
  }

  const containerRect = chartRef.value.getBoundingClientRect()
  if (containerRect.width === 0 || containerRect.height === 0) {
    console.warn('LineChartCard: Container has zero size, waiting...')
    setTimeout(() => initChart(), 200)
    return
  }

  if (chartInstance.value) {
    chartInstance.value.dispose()
  }

  try {
    chartInstance.value = echarts.init(chartRef.value)
    isInitialized.value = true
    retryCount.value = 0
    console.log('LineChartCard: Chart initialized successfully')

    if (props.data.length > 0) {
      updateChart()
    }

    window.addEventListener('resize', handleResize)
  } catch (error) {
    console.error('LineChartCard: Failed to initialize chart:', error)
  }
}

const updateChart = () => {
  if (!isInitialized.value || !chartInstance.value) {
    console.warn('LineChartCard: Chart not initialized, skipping update')
    return
  }

  if (!props.data || props.data.length === 0) {
    console.warn('LineChartCard: No data available, clearing chart')
    chartInstance.value.clear()
    return
  }

  console.log('LineChartCard: Updating chart with', props.data.length, 'data points')

  const times = props.data.map(item => dayjs(item.time).format('HH:mm'))
  let values: number[] = []
  let unit = ''
  let seriesName = ''

  switch (activeTab.value) {
    case 'temperature':
      values = props.data.map(item => {
        const temp = Number(item.temp)
        if (preferenceStore.preferences.temperatureUnit === 'F') {
          return Math.round((temp * 9/5) + 32)
        }
        return Math.round(temp)
      })
      unit = '°' + preferenceStore.preferences.temperatureUnit
      seriesName = '温度'
      break
    case 'humidity':
      values = props.data.map(item => Math.round(Number(item.humidity)))
      unit = '%'
      seriesName = '湿度'
      break
    case 'windSpeed':
      values = props.data.map(item => {
        const speed = Number(item.windSpeed)
        if (preferenceStore.preferences.windSpeedUnit === 'km/h') {
          return Math.round(speed * 3.6 * 10) / 10
        }
        return Math.round(speed * 10) / 10
      })
      unit = preferenceStore.preferences.windSpeedUnit
      seriesName = '风速'
      break
  }

  console.log('LineChartCard: Chart data:', { times, values, unit, seriesName })

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
        name: seriesName,
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

  chartInstance.value.setOption(option, true)
  console.log('LineChartCard: Chart option set successfully')
}

const handleResize = () => {
  if (chartInstance.value && isInitialized.value) {
    chartInstance.value.resize()
  }
}

watch(activeTab, () => {
  console.log('LineChartCard: Active tab changed to', activeTab.value)
  if (isInitialized.value) {
    updateChart()
  }
})

watch(() => props.data, (newData) => {
  console.log('LineChartCard: Data changed, length:', newData?.length)
  if (!isInitialized.value && newData && newData.length > 0) {
    nextTick(() => {
      setTimeout(() => initChart(), 100)
    })
  } else if (isInitialized.value) {
    nextTick(() => {
      updateChart()
    })
  }
}, { deep: true })

onMounted(() => {
  console.log('LineChartCard: Component mounted')
  nextTick(() => {
    setTimeout(() => initChart(), 100)
  })
})

onBeforeUnmount(() => {
  console.log('LineChartCard: Component unmounting')
  window.removeEventListener('resize', handleResize)
  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = undefined
  }
  isInitialized.value = false
})
</script>

<style scoped>
.line-chart-card {
  border-radius: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.line-chart-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px 20px 20px 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
}

.chart-container {
  width: 100%;
  height: 300px;
  flex: 1;
  min-height: 300px;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .chart-container {
    height: 250px;
    min-height: 250px;
  }
}
</style>