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
  console.log('LineChartCard initChart called', {
    chartRef: !!chartRef.value,
    chartRefElement: chartRef.value,
    chartRefWidth: chartRef.value?.offsetWidth,
    chartRefHeight: chartRef.value?.offsetHeight,
    parentElement: chartRef.value?.parentElement,
    parentWidth: chartRef.value?.parentElement?.offsetWidth,
    parentHeight: chartRef.value?.parentElement?.offsetHeight,
    hasData: props.data.length > 0,
    existingInstance: !!chartInstance.value
  })

  if (!chartRef.value) return

  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = undefined
    console.log('LineChartCard: Old ECharts instance disposed')
  }

  nextTick(() => {
    chartInstance.value = echarts.init(chartRef.value, null, { renderer: 'canvas' })
    console.log('LineChartCard: ECharts instance created', !!chartInstance.value)
    const domElement = chartInstance.value.getDom()
    const firstChild = domElement?.children?.[0]
    console.log('LineChartCard: ECharts instance details', {
      id: chartInstance.value.id,
      group: chartInstance.value.group,
      width: chartInstance.value.getWidth(),
      height: chartInstance.value.getHeight(),
      dom: domElement,
      domChildren: domElement?.children?.length,
      domChildrenArray: Array.from(domElement?.children || []).map(child => ({
        tagName: child.tagName,
        className: child.className,
        innerHTML: child.innerHTML?.substring(0, 100),
        childCount: child.children?.length,
        childTags: Array.from(child.children || []).map(c => c.tagName)
      })),
      zr: chartInstance.value.getZr(),
      zrPainter: chartInstance.value.getZr()?.painter,
      zrPainterType: chartInstance.value.getZr()?.painter?.type,
      firstChildCanvas: firstChild?.querySelector('canvas'),
      firstChildCanvasCount: firstChild?.querySelectorAll('canvas')?.length
    })

    if (props.data.length > 0) {
      updateChart()
    }

    window.addEventListener('resize', handleResize)
  })
}

const updateChart = () => {
  console.log('LineChartCard updateChart called', {
    chartInstance: !!chartInstance.value,
    dataLength: props.data.length,
    activeTab: activeTab.value
  })

  if (!chartInstance.value) return

  if (!props.data.length) {
    console.log('LineChartCard: No data, clearing chart')
    chartInstance.value.clear()
    return
  }

  console.log('LineChartCard: Updating chart with', props.data.length, 'items')

  const times = props.data.map(item => dayjs(item.time).format('HH:mm'))
  let values: number[] = []
  let unit = ''

  switch (activeTab.value) {
    case 'temperature':
      values = props.data.map(item => {
        if (preferenceStore.preferences.temperatureUnit === 'F') {
          return parseFloat(((item.temp * 9/5) + 32).toFixed(3))
        }
        return parseFloat(item.temp.toFixed(3))
      })
      unit = preferenceStore.preferences.temperatureUnit
      break
    case 'humidity':
      values = props.data.map(item => parseFloat(item.humidity.toFixed(3)))
      unit = '%'
      break
    case 'windSpeed':
      values = props.data.map(item => {
        if (preferenceStore.preferences.windSpeedUnit === 'km/h') {
          return parseFloat((item.windSpeed * 3.6).toFixed(3))
        }
        return parseFloat(item.windSpeed.toFixed(3))
      })
      unit = preferenceStore.preferences.windSpeedUnit
      break
  }

  console.log('LineChartCard: Data details', {
    times: times,
    values: values,
    unit: unit,
    activeTab: activeTab.value,
    firstItem: props.data[0],
    lastItem: props.data[props.data.length - 1]
  })

  const option = {
    tooltip: {
      trigger: 'axis'
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
      data: times
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: activeTab.value === 'temperature' ? '温度' : activeTab.value === 'humidity' ? '湿度' : '风速',
        type: 'line',
        smooth: true,
        data: values
      }
    ]
  }
  
  console.log('LineChartCard: Chart option details', {
    option: option,
    timesLength: times.length,
    valuesLength: values.length,
    hasValidData: values.every(v => typeof v === 'number' && !isNaN(v))
  })

  chartInstance.value.setOption(option)
  console.log('LineChartCard: Chart option set', option)
  console.log('LineChartCard: Chart instance state', {
    width: chartInstance.value.getWidth(),
    height: chartInstance.value.getHeight()
  })
  
  console.log('LineChartCard: DOM element state', {
    chartRef: chartRef.value,
    chartRefWidth: chartRef.value?.offsetWidth,
    chartRefHeight: chartRef.value?.offsetHeight,
    chartRefDisplay: window.getComputedStyle(chartRef.value).display,
    chartRefVisibility: window.getComputedStyle(chartRef.value).visibility,
    chartRefOpacity: window.getComputedStyle(chartRef.value).opacity,
    parentElement: chartRef.value?.parentElement,
    parentDisplay: chartRef.value?.parentElement ? window.getComputedStyle(chartRef.value.parentElement).display : null
  })
  
  console.log('LineChartCard: Canvas element', {
    canvas: chartRef.value?.querySelector('canvas'),
    canvasWidth: chartRef.value?.querySelector('canvas')?.width,
    canvasHeight: chartRef.value?.querySelector('canvas')?.height,
    canvasDisplay: chartRef.value?.querySelector('canvas') ? window.getComputedStyle(chartRef.value.querySelector('canvas')).display : null
  })
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
