<template>
  <el-card class="gauge-card" shadow="hover">
    <template #header>
      <span class="card-title">空气质量指数</span>
    </template>

    <el-skeleton v-if="loading" :loading="loading" animated>
      <template #template>
        <el-skeleton-item variant="circle" style="width: 200px; height: 200px; margin: 0 auto;" />
      </template>
    </el-skeleton>

    <div v-else class="card-content">
      <div ref="chartRef" class="chart-container"></div>
      <div class="aqi-info">
        <div class="aqi-value">{{ aqi }}</div>
        <div class="aqi-level" :class="aqiLevelClass">{{ aqiLevel }}</div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import type { CurrentWeatherDTO } from '@/types'

interface Props {
  weather: CurrentWeatherDTO | null
  loading: boolean
}

const props = defineProps<Props>()

const chartRef = ref<HTMLElement>()
const chartInstance = ref<ECharts>()
const isInitialized = ref(false)
const retryCount = ref(0)

const aqi = computed(() => {
  const value = props.weather?.aqi || 50
  return Math.round(Number(value))
})

const aqiLevel = computed(() => {
  const value = aqi.value
  if (value <= 50) return '优'
  if (value <= 100) return '良'
  if (value <= 150) return '轻度污染'
  if (value <= 200) return '中度污染'
  return '重度污染'
})

const aqiLevelClass = computed(() => {
  const value = aqi.value
  if (value <= 50) return 'level-good'
  if (value <= 100) return 'level-moderate'
  if (value <= 150) return 'level-unhealthy'
  if (value <= 200) return 'level-very-unhealthy'
  return 'level-hazardous'
})

const initChart = () => {
  console.log('Gauge: initChart called, isInitialized:', isInitialized.value, 'chartRef:', chartRef.value)
  
  if (!chartRef.value) {
    console.warn('Gauge: chartRef is null')
    if (retryCount.value < 3) {
      retryCount.value++
      setTimeout(() => initChart(), 100)
    }
    return
  }

  const containerRect = chartRef.value.getBoundingClientRect()
  console.log('Gauge: Container rect:', containerRect)
  
  if (containerRect.width === 0 || containerRect.height === 0) {
    console.warn('Gauge: Container size is 0')
    setTimeout(() => initChart(), 200)
    return
  }

  if (chartInstance.value) {
    console.log('Gauge: Disposing old chart instance')
    chartInstance.value.dispose()
  }

  try {
    console.log('Gauge: Initializing ECharts instance')
    chartInstance.value = echarts.init(chartRef.value)
    isInitialized.value = true
    retryCount.value = 0
    console.log('Gauge: ECharts initialized successfully, isInitialized:', isInitialized.value)

    updateChart()

    window.addEventListener('resize', handleResize)
  } catch (error) {
    console.error('Gauge: 初始化失败:', error)
  }
}

const updateChart = () => {
  if (!chartRef.value) {
    console.warn('Gauge: chartRef is null, cannot update chart')
    return
  }

  console.log('Gauge: 更新图表，AQI:', aqi.value)
  console.log('Gauge: 图表容器:', chartRef.value)
  console.log('Gauge: 图表实例:', chartInstance.value)
  console.log('Gauge: 容器尺寸:', chartRef.value ? {
    width: chartRef.value.offsetWidth,
    height: chartRef.value.offsetHeight
  } : 'null')

  if (!isInitialized.value || !chartInstance.value) {
    console.warn('Gauge: Chart not initialized, reinitializing...')
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
    console.warn('Gauge: Container has no echarts instance, reinitializing...')
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

  const option = {
    series: [
      {
        type: 'gauge',
        startAngle: 180,
        endAngle: 0,
        min: 0,
        max: 300,
        splitNumber: 6,
        radius: '90%',
        center: ['50%', '55%'],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#67c23a' },
            { offset: 0.33, color: '#e6a23c' },
            { offset: 0.66, color: '#f56c6c' },
            { offset: 1, color: '#8b0000' }
          ])
        },
        progress: {
          show: true,
          roundCap: true,
          width: 16
        },
        pointer: {
          icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
          length: '10%',
          width: 16,
          offsetCenter: [0, '-55%'],
          itemStyle: {
            color: 'inherit'
          }
        },
        axisLine: {
          roundCap: true,
          lineStyle: {
            width: 16
          }
        },
        axisTick: {
          splitNumber: 2,
          lineStyle: {
            width: 2,
            color: '#999'
          }
        },
        splitLine: {
          length: 10,
          lineStyle: {
            width: 2,
            color: '#999'
          }
        },
        axisLabel: {
          distance: 25,
          color: '#999',
          fontSize: 11
        },
        title: {
          show: false
        },
        detail: {
          show: false
        },
        data: [
          {
            value: aqi.value
          }
        ]
      }
    ]
  }

  chartInstance.value.setOption(option, true)
}

const handleResize = () => {
  if (chartInstance.value && isInitialized.value) {
    chartInstance.value.resize()
  }
}

watch(() => props.weather, () => {
  if (!isInitialized.value && props.weather) {
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
  nextTick(() => {
    setTimeout(() => initChart(), 100)
  })
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
.gauge-card {
  border-radius: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.gauge-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 32px 28px;
  min-height: 0;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 40px;
  min-height: 0;
}

.chart-container {
  width: 100%;
  height: 220px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.aqi-info {
  text-align: center;
  padding: 32px 20px 24px 20px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.aqi-value {
  font-size: 64px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 20px;
  line-height: 1;
}

.aqi-level {
  font-size: 20px;
  font-weight: 600;
  padding: 12px 32px;
  border-radius: 28px;
  display: inline-block;
  letter-spacing: 0.5px;
}

.level-good { background: #f0f9ff; color: #67c23a; }
.level-moderate { background: #fdf6ec; color: #e6a23c; }
.level-unhealthy { background: #fef0f0; color: #f56c6c; }
.level-very-unhealthy { background: #fde2e2; color: #c0392b; }
.level-hazardous { background: #e8d5d5; color: #8b0000; }

@media (max-width: 1200px) {
  .gauge-card :deep(.el-card__body) {
    padding: 28px 24px;
  }

  .card-content {
    gap: 32px;
  }

  .chart-container {
    height: 200px;
  }

  .aqi-info {
    padding: 28px 16px 20px 16px;
  }

  .aqi-value {
    font-size: 56px;
    margin-bottom: 18px;
  }

  .aqi-level {
    font-size: 18px;
    padding: 10px 28px;
  }
}

@media (max-width: 768px) {
  .gauge-card :deep(.el-card__body) {
    padding: 24px 20px;
  }

  .card-content {
    gap: 28px;
  }

  .chart-container {
    height: 180px;
  }

  .aqi-info {
    padding: 24px 12px 20px 12px;
  }

  .aqi-value {
    font-size: 48px;
    margin-bottom: 16px;
  }

  .aqi-level {
    font-size: 16px;
    padding: 10px 24px;
  }
}
</style>
