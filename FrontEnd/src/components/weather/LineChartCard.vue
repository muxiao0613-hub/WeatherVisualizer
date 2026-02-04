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
  if (!chartRef.value) {
    console.error('LineChartCard: chartRef is null')
    return
  }

  if (chartInstance.value) {
    chartInstance.value.dispose()
  }

  chartInstance.value = echarts.init(chartRef.value)
  console.log('LineChartCard: Chart initialized')

  if (props.data.length > 0) {
    updateChart()
  }

  window.addEventListener('resize', handleResize)
}

const updateChart = () => {
  if (!chartInstance.value) {
    console.error('LineChartCard: chartInstance is null')
    return
  }

  if (!props.data || props.data.length === 0) {
    console.warn('LineChartCard: No data available')
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

  chartInstance.value.setOption(option)
  console.log('LineChartCard: Chart option set successfully')
}

const handleResize = () => {
  chartInstance.value?.resize()
}

watch(activeTab, () => {
  console.log('LineChartCard: Active tab changed to', activeTab.value)
  updateChart()
})

watch(() => props.data, (newData) => {
  console.log('LineChartCard: Data changed, length:', newData?.length)
  nextTick(() => {
    updateChart()
  })
}, { deep: true })

onMounted(() => {
  console.log('LineChartCard: Component mounted')
  nextTick(() => {
    initChart()
  })
})

onBeforeUnmount(() => {
  console.log('LineChartCard: Component unmounting')
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