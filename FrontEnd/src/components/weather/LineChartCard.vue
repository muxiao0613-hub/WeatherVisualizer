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
  console.log('LineChart: initChart called, isInitialized:', isInitialized.value, 'chartRef:', chartRef.value)
  
  if (!chartRef.value) {
    console.warn('LineChart: chartRef is null')
    if (retryCount.value < 3) {
      retryCount.value++
      setTimeout(() => initChart(), 100)
    }
    return
  }

  const containerRect = chartRef.value.getBoundingClientRect()
  console.log('LineChart: Container rect:', containerRect)
  
  if (containerRect.width === 0 || containerRect.height === 0) {
    console.warn('LineChart: Container size is 0')
    if (retryCount.value < 10) {
      retryCount.value++
      setTimeout(() => initChart(), 200)
    } else {
      console.error('LineChart: Failed to initialize after 10 retries')
    }
    return
  }

  if (chartInstance.value) {
    console.log('LineChart: Disposing old chart instance')
    chartInstance.value.dispose()
  }

  try {
    console.log('LineChart: Initializing ECharts instance')
    chartInstance.value = echarts.init(chartRef.value)
    isInitialized.value = true
    retryCount.value = 0
    console.log('LineChart: ECharts initialized successfully, isInitialized:', isInitialized.value)

    if (props.data.length > 0) {
      console.log('LineChart: Data available, updating chart')
      updateChart()
    }

    window.addEventListener('resize', handleResize)
  } catch (error) {
    console.error('LineChart: 初始化失败:', error)
  }
}

const updateChart = () => {
  if (!chartRef.value) {
    console.warn('LineChart: chartRef is null, cannot update chart')
    return
  }

  if (!props.data || props.data.length === 0) {
    console.warn('LineChart: No data available, clearing chart')
    if (chartInstance.value) {
      chartInstance.value.clear()
    }
    return
  }

  console.log('LineChart: 更新图表，数据量:', props.data.length)
  console.log('LineChart: 图表容器:', chartRef.value)
  console.log('LineChart: 图表实例:', chartInstance.value)
  console.log('LineChart: 容器尺寸:', chartRef.value ? {
    width: chartRef.value.offsetWidth,
    height: chartRef.value.offsetHeight
  } : 'null')

  if (!isInitialized.value || !chartInstance.value) {
    console.warn('LineChart: Chart not initialized, reinitializing...')
    isInitialized.value = false
    if (chartInstance.value) {
      chartInstance.value.dispose()
      chartInstance.value = undefined
    }
    nextTick(() => {
      setTimeout(() => initChart(), 100)
    })
    return
  }

  const containerInstance = chartRef.value.getAttribute('_echarts_instance_')
  if (!containerInstance) {
    console.warn('LineChart: Container has no echarts instance, reinitializing...')
    isInitialized.value = false
    if (chartInstance.value) {
      chartInstance.value.dispose()
      chartInstance.value = undefined
    }
    nextTick(() => {
      setTimeout(() => initChart(), 100)
    })
    return
  }

  const times = props.data.map(item => {
    if (!item.time) {
      console.warn('LineChart: Missing time in item', item)
      return 'Unknown'
    }
    return dayjs(item.time).format('HH:mm')
  })
  
  let values: number[] = []
  let unit = ''
  let seriesName = ''

  switch (activeTab.value) {
    case 'temperature':
      values = props.data.map(item => {
        const temp = Number(item.temp)
        if (isNaN(temp)) {
          console.warn('LineChart: Invalid temp', item.temp)
          return 0
        }
        if (preferenceStore.preferences.temperatureUnit === 'F') {
          return Math.round((temp * 9/5) + 32)
        }
        return Math.round(temp)
      })
      unit = '°' + preferenceStore.preferences.temperatureUnit
      seriesName = '温度'
      break
    case 'humidity':
      values = props.data.map(item => {
        const humidity = Number(item.humidity)
        if (isNaN(humidity)) {
          console.warn('LineChart: Invalid humidity', item.humidity)
          return 0
        }
        return Math.round(humidity)
      })
      unit = '%'
      seriesName = '湿度'
      break
    case 'windSpeed':
      values = props.data.map(item => {
        const speed = Number(item.windSpeed)
        if (isNaN(speed)) {
          console.warn('LineChart: Invalid windSpeed', item.windSpeed)
          return 0
        }
        if (preferenceStore.preferences.windSpeedUnit === 'km/h') {
          return Math.round(speed * 3.6 * 10) / 10
        }
        return Math.round(speed * 10) / 10
      })
      unit = preferenceStore.preferences.windSpeedUnit
      seriesName = '风速'
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

  try {
    chartInstance.value.setOption(option, true)
    console.log('LineChart: 图表更新完成')
  } catch (error) {
    console.error('LineChart: 图表更新失败:', error)
  }
}

const handleResize = () => {
  if (chartInstance.value && isInitialized.value) {
    chartInstance.value.resize()
  }
}

watch(activeTab, () => {
  if (isInitialized.value) {
    updateChart()
  }
})

watch(() => props.data, (newData) => {
  console.log('LineChart: Data watch triggered')
  console.log('  - isInitialized:', isInitialized.value)
  console.log('  - newData length:', newData?.length)
  console.log('  - loading:', props.loading)
  
  if (!isInitialized.value && newData && newData.length > 0 && !props.loading) {
    console.log('LineChart: Condition met - calling initChart')
    nextTick(() => {
      setTimeout(() => initChart(), 100)
    })
  } else if (isInitialized.value) {
    console.log('LineChart: Chart already initialized - calling updateChart')
    nextTick(() => {
      updateChart()
    })
  }
}, { deep: true })

watch(() => props.loading, (newLoading, oldLoading) => {
  if (oldLoading && !newLoading && props.data.length > 0 && !isInitialized.value) {
    nextTick(() => {
      setTimeout(() => initChart(), 100)
    })
  }
})

onMounted(() => {
  if (props.data.length > 0 && !props.loading) {
    nextTick(() => {
      setTimeout(() => initChart(), 100)
    })
  }
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
