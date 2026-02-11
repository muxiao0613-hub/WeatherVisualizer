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
  console.log('BarChart: initChart called, isInitialized:', isInitialized.value, 'chartRef:', chartRef.value)
  
  if (!chartRef.value) {
    console.warn('BarChart: chartRef is null')
    if (retryCount.value < 3) {
      retryCount.value++
      setTimeout(() => initChart(), 100)
    }
    return
  }

  const containerRect = chartRef.value.getBoundingClientRect()
  console.log('BarChart: Container rect:', containerRect)
  
  if (containerRect.width === 0 || containerRect.height === 0) {
    console.warn('BarChart: Container size is 0')
    if (retryCount.value < 10) {
      retryCount.value++
      setTimeout(() => initChart(), 200)
    }
    return
  }

  if (chartInstance.value) {
    console.log('BarChart: Disposing old chart instance')
    chartInstance.value.dispose()
  }

  try {
    console.log('BarChart: Initializing ECharts instance')
    chartInstance.value = echarts.init(chartRef.value)
    isInitialized.value = true
    retryCount.value = 0
    console.log('BarChart: ECharts initialized successfully, isInitialized:', isInitialized.value)

    if (props.data.length > 0) {
      console.log('BarChart: Data available, updating chart')
      updateChart()
    }

    window.addEventListener('resize', handleResize)
  } catch (error) {
    console.error('BarChart: 初始化失败:', error)
  }
}

const updateChart = () => {
  if (!chartRef.value) {
    console.warn('BarChart: chartRef is null, cannot update chart')
    return
  }

  if (!props.data || props.data.length === 0) {
    console.warn('BarChart: No data available, clearing chart')
    if (chartInstance.value) {
      chartInstance.value.clear()
    }
    return
  }

  console.log('BarChart: 更新图表，数据量:', props.data.length)
  console.log('BarChart: 图表容器:', chartRef.value)
  console.log('BarChart: 图表实例:', chartInstance.value)
  console.log('BarChart: 容器尺寸:', chartRef.value ? {
    width: chartRef.value.offsetWidth,
    height: chartRef.value.offsetHeight
  } : 'null')

  if (!isInitialized.value || !chartInstance.value) {
    console.warn('BarChart: Chart not initialized, reinitializing...')
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
    console.warn('BarChart: Container has no echarts instance, reinitializing...')
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

  const dates = props.data.map(item => {
    if (!item.date) {
      return 'Unknown'
    }
    return dayjs(item.date).format('MM-DD')
  })
  const unit = preferenceStore.preferences.temperatureUnit

  const tempMinData = props.data.map(item => {
    const temp = item.tempMin !== undefined && item.tempMin !== null ? Number(item.tempMin) : 0
    if (isNaN(temp)) {
      return 0
    }
    if (unit === 'F') {
      return Math.round((temp * 9/5) + 32)
    }
    return Math.round(temp)
  })

  const tempMaxData = props.data.map(item => {
    const temp = item.tempMax !== undefined && item.tempMax !== null ? Number(item.tempMax) : 0
    if (isNaN(temp)) {
      return 0
    }
    if (unit === 'F') {
      return Math.round((temp * 9/5) + 32)
    }
    return Math.round(temp)
  })

  const popData = props.data.map(item => {
    const pop = item.pop !== undefined && item.pop !== null ? Number(item.pop) : 0
    if (isNaN(pop)) {
      return 0
    }
    return Math.round(pop * 100)
  })

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
        data: tempMinData,
        itemStyle: {
          color: '#91cc75'
        }
      },
      {
        name: '最高温度',
        type: 'bar',
        data: tempMaxData,
        itemStyle: {
          color: '#ee6666'
        }
      }
    ] : [
      {
        name: '降水概率',
        type: 'bar',
        data: popData,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#5470c6' },
            { offset: 1, color: '#91cc75' }
          ])
        }
      }
    ]
  }

  try {
    chartInstance.value.setOption(option, true)
  } catch (error) {
    console.error('BarChart: 图表更新失败:', error)
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
  if (!isInitialized.value && newData && newData.length > 0 && !props.loading) {
    nextTick(() => {
      setTimeout(() => initChart(), 100)
    })
  } else if (isInitialized.value) {
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
