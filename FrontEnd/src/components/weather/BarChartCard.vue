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

const initChart = () => {
  if (!chartRef.value) return

  chartInstance.value = echarts.init(chartRef.value)
  updateChart()

  window.addEventListener('resize', handleResize)
}

const updateChart = () => {
  if (!chartInstance.value || !props.data.length) return

  const dates = props.data.map(item => dayjs(item.date).format('MM-DD'))
  const unit = preferenceStore.preferences.temperatureUnit

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params: any) => {
        const result = params.map((param: any) => {
          let value = param.value
          if (activeTab.value === 'temp') {
            value = param.value.toFixed(3)
          } else if (activeTab.value === 'pop') {
            value = (param.value * 100).toFixed(3)
          }
          return `${param.marker} ${param.seriesName}: ${value}${activeTab.value === 'temp' ? '°' : '%'}`
        })
        return `${params[0].name}<br/>${result.join('<br/>')}`
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
          if (activeTab.value === 'temp') {
            return `${value.toFixed(3)}°`
          }
          return `${value.toFixed(3)}%`
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
          if (unit === 'F') {
            return parseFloat(((item.tempMin * 9/5) + 32).toFixed(3))
          }
          return parseFloat(item.tempMin.toFixed(3))
        }),
        itemStyle: {
          color: '#91cc75'
        }
      },
      {
        name: '最高温度',
        type: 'bar',
        data: props.data.map(item => {
          if (unit === 'F') {
            return parseFloat(((item.tempMax * 9/5) + 32).toFixed(3))
          }
          return parseFloat(item.tempMax.toFixed(3))
        }),
        itemStyle: {
          color: '#ee6666'
        }
      }
    ] : [
      {
        name: '降水概率',
        type: 'bar',
        data: props.data.map(item => parseFloat((item.pop * 100).toFixed(3))),
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: '#5470c6' },
              { offset: 1, color: '#91cc75' }
            ]
          }
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
.bar-chart-card {
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
