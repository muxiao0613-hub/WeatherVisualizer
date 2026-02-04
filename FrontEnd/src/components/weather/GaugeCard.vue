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

    <div v-else ref="chartRef" class="chart-container"></div>

    <div v-if="!loading" class="aqi-info">
      <div class="aqi-value">{{ aqi }}</div>
      <div class="aqi-level" :class="aqiLevelClass">{{ aqiLevel }}</div>
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

const aqi = computed(() => props.weather?.aqi || 50)

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
  if (!chartRef.value) return

  chartInstance.value = echarts.init(chartRef.value)
  updateChart()

  window.addEventListener('resize', handleResize)
}

const updateChart = () => {
  if (!chartInstance.value) return

  const option = {
    series: [
      {
        type: 'gauge',
        startAngle: 180,
        endAngle: 0,
        min: 0,
        max: 300,
        splitNumber: 6,
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
          width: 18
        },
        pointer: {
          icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
          length: '12%',
          width: 20,
          offsetCenter: [0, '-60%'],
          itemStyle: {
            color: 'inherit'
          }
        },
        axisLine: {
          roundCap: true,
          lineStyle: {
            width: 18
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
          length: 12,
          lineStyle: {
            width: 3,
            color: '#999'
          }
        },
        axisLabel: {
          distance: 30,
          color: '#999',
          fontSize: 12
        },
        title: {
          show: false
        },
        detail: {
          backgroundColor: '#fff',
          borderColor: '#999',
          borderWidth: 1,
          width: '60%',
          lineHeight: 40,
          height: 40,
          borderRadius: 8,
          offsetCenter: [0, '35%'],
          valueAnimation: true,
          formatter: function (value: number) {
            return Math.round(value)
          },
          color: 'inherit',
          fontSize: 20,
          fontWeight: 600
        },
        data: [
          {
            value: aqi.value
          }
        ]
      }
    ]
  }

  chartInstance.value.setOption(option)
}

const handleResize = () => {
  chartInstance.value?.resize()
}

watch(() => props.weather, () => {
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
.gauge-card {
  border-radius: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  width: 100%;
  height: 200px;
}

.aqi-info {
  text-align: center;
  margin-top: 16px;
}

.aqi-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
}

.aqi-level {
  font-size: 14px;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 12px;
  display: inline-block;
}

.level-good { background: #f0f9ff; color: #67c23a; }
.level-moderate { background: #fdf6ec; color: #e6a23c; }
.level-unhealthy { background: #fef0f0; color: #f56c6c; }
.level-very-unhealthy { background: #fde2e2; color: #c0392b; }
.level-hazardous { background: #e8d5d5; color: #8b0000; }
</style>
