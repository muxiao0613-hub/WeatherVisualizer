<template>
  <el-card class="bar-chart-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span class="card-title">7天预报</span>
        <el-radio-group v-model="activeTab" size="small">
          <el-radio-button value="temp">温度</el-radio-button>
          <el-radio-button value="pop">降水概率</el-radio-button>
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
import type { DailyForecastDTO } from '@/types'
import dayjs from 'dayjs'

interface Props {
  data: DailyForecastDTO[]
  loading: boolean
}

const props = defineProps<Props>()

const preferenceStore = usePreferenceStore()
const chartRef = ref<HTMLElement>()
const chartInstance = ref<ECharts>()
const activeTab = ref('temp')
const isInitialized = ref(false)
const retryCount = ref(0)

const initChart = () => {
  if (!chartRef.value) {
    console.warn('BarChartCard: chartRef is null, retrying...')
    if (retryCount.value < 3) {
      retryCount.value++
      setTimeout(() => initChart(), 100)
    }
    return
  }

  const containerRect = chartRef.value.getBoundingClientRect()
  if (containerRect.width === 0 || containerRect.height === 0) {
    console.warn('BarChartCard: Container has zero size, waiting...')
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
    console.log('BarChartCard: Chart initialized successfully')

    if (props.data.length > 0) {
      updateChart()
    }

    window.addEventListener('resize', handleResize)
  } catch (error) {
    console.error('BarChartCard: Failed to initialize chart:', error)
  }
}

const updateChart = () => {
  if (!isInitialized.value || !chartInstance.value) {
    console.warn('BarChartCard: Chart not initialized, skipping update')
    return
  }

  if (!props.data || props.data.length === 0) {
    console.warn('BarChartCard: No data available, clearing chart')
    chartInstance.value.clear()
    return
  }

  console.log('BarChartCard: Updating chart with', props.data.length, 'data points')

  const dates = props.data.map(item => dayjs(item.date).format('MM-DD'))
  const unit = preferenceStore.preferences.temperatureUnit

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: activeTab.value === 'temp' ? ['最低温度', '最高温度'] : ['降水概率'],
      top: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
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
        formatter: (value: number) => {
          return activeTab.value === 'temp' ? `${Math.round(value)}°` : `${Math.round(value)}%`
        }
      },
      splitLine: {
        lineStyle: {
          color: '#f5f7fa'
        }
      }
    },
    series: activeTab.value === 'temp' ? [
      {
        name: '最低温度',
        type: 'bar',
        data: props.data.map(item => {
          const temp = Number(item.tempMin)
          if (unit === 'F') {
            return Math.round((temp * 9/5) + 32)
          }
          return Math.round(temp)
        }),
        itemStyle: {
          color: '#91cc75'
        }
      },
      {
        name: '最高温度',
        type: 'bar',
        data: props.data.map(item => {
          const temp = Number(item.tempMax)
          if (unit === 'F') {
            return Math.round((temp * 9/5) + 32)
          }
          return Math.round(temp)
        }),
        itemStyle: {
          color: '#ee6666'
        }
      }
    ] : [
      {
        name: '降水概率',
        type: 'bar',
        data: props.data.map(item => Math.round(Number(item.pop) * 100)),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#5470c6' },
            { offset: 1, color: '#91cc75' }
          ])
        }
      }
    ]
  }

  chartInstance.value.setOption(option, true)
  console.log('BarChartCard: Chart option set successfully')
}

const handleResize = () => {
  if (chartInstance.value && isInitialized.value) {
    chartInstance.value.resize()
  }
}

watch(activeTab, () => {
  console.log('BarChartCard: Active tab changed to', activeTab.value)
  if (isInitialized.value) {
    updateChart()
  }
})

watch(() => props.data, (newData) => {
  console.log('BarChartCard: Data changed, length:', newData?.length)
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
  console.log('BarChartCard: Component mounted')
  nextTick(() => {
    setTimeout(() => initChart(), 100)
  })
})

onBeforeUnmount(() => {
  console.log('BarChartCard: Component unmounting')
  window.removeEventListener('resize', handleResize)
  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = undefined
  }
  isInitialized.value = false
})
</script>

<style scoped>
.bar-chart-card {
  border-radius: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.bar-chart-card :deep(.el-card__body) {
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