<template>
  <el-card class="current-weather-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <div class="city-info">
          <span class="city-name">{{ weather?.city || '-' }}</span>
          <span class="update-time">更新于 {{ weather ? updateTime : '-' }}</span>
        </div>
        <el-icon :size="24" color="#409EFF"><Sunny /></el-icon>
      </div>
    </template>

    <el-skeleton v-if="loading" :loading="loading" animated>
      <template #template>
        <el-skeleton-item variant="text" style="width: 60%; height: 60px;" />
        <el-skeleton-item variant="text" style="width: 40%; margin-top: 16px;" />
        <el-row :gutter="16" style="margin-top: 24px;">
          <el-col :span="8" v-for="i in 4" :key="i">
            <el-skeleton-item variant="text" style="width: 100%;" />
          </el-col>
        </el-row>
      </template>
    </el-skeleton>

    <div v-else-if="weather" class="weather-content">
      <div class="main-info">
        <div class="temperature">
          <span class="temp-value">{{ formatTemp(weather.temp) }}</span>
          <span class="temp-unit">°{{ temperatureUnit }}</span>
        </div>
        <div class="description">
          <span class="weather-desc">{{ weather.description }}</span>
          <span class="feels-like">体感 {{ formatTemp(weather.feelsLike) }}°</span>
        </div>
      </div>

      <el-divider />

      <el-row :gutter="16" class="details">
        <el-col :span="8" class="detail-item">
          <div class="detail-label">风向</div>
          <div class="detail-value">{{ weather.windDir || '-' }}</div>
        </el-col>
        <el-col :span="8" class="detail-item">
          <div class="detail-label">风力</div>
          <div class="detail-value">{{ weather.windScale !== undefined ? weather.windScale + '级' : '-' }}</div>
        </el-col>
        <el-col :span="8" class="detail-item">
          <div class="detail-label">湿度</div>
          <div class="detail-value">{{ Math.round(weather.humidity) }}%</div>
        </el-col>
        <el-col :span="8" class="detail-item">
          <div class="detail-label">气压</div>
          <div class="detail-value">{{ Math.round(weather.pressure) }}hPa</div>
        </el-col>
        <el-col :span="8" class="detail-item">
          <div class="detail-label">能见度</div>
          <div class="detail-value">{{ Math.round(weather.visibility * 10) / 10 }}km</div>
        </el-col>
        <el-col :span="8" class="detail-item">
          <div class="detail-label">降水</div>
          <div class="detail-value">{{ weather.precip !== undefined ? Math.round(weather.precip * 10) / 10 : '-' }}mm</div>
        </el-col>
        <el-col :span="8" class="detail-item">
          <div class="detail-label">云量</div>
          <div class="detail-value">{{ weather.cloud !== undefined && weather.cloud !== null ? weather.cloud + '%' : '-' }}</div>
        </el-col>
        <el-col :span="8" class="detail-item">
          <div class="detail-label">露点</div>
          <div class="detail-value">{{ weather.dew !== undefined && weather.dew !== null ? Math.round(weather.dew) + '°' : '-' }}</div>
        </el-col>
        <el-col :span="8" class="detail-item">
          <div class="detail-label">AQI</div>
          <div class="detail-value" :class="getAqiClass(weather.aqi || 0)">
            {{ weather.aqi ? Math.round(weather.aqi) : '-' }}
          </div>
        </el-col>
      </el-row>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Sunny } from '@element-plus/icons-vue'
import { usePreferenceStore } from '@/stores/preference'
import type { CurrentWeatherDTO } from '@/types'
import dayjs from 'dayjs'

interface Props {
  weather: CurrentWeatherDTO | null
  loading: boolean
}

const props = defineProps<Props>()

const preferenceStore = usePreferenceStore()

const temperatureUnit = computed(() => preferenceStore.preferences.temperatureUnit)
const windSpeedUnit = computed(() => preferenceStore.preferences.windSpeedUnit)

const updateTime = computed(() => {
  if (!props.weather?.timestamp) return '-'
  return dayjs.unix(props.weather.timestamp).format('HH:mm')
})

const formatTemp = (temp: number) => {
  const numTemp = Number(temp)
  if (temperatureUnit.value === 'F') {
    return Math.round((numTemp * 9/5) + 32)
  }
  return Math.round(numTemp)
}

const formatWindSpeed = (speed: number) => {
  const numSpeed = Number(speed)
  if (windSpeedUnit.value === 'km/h') {
    return Math.round(numSpeed * 3.6 * 10) / 10
  }
  return Math.round(numSpeed * 10) / 10
}

const getAqiClass = (aqi: number) => {
  const numAqi = Number(aqi)
  if (numAqi <= 50) return 'aqi-good'
  if (numAqi <= 100) return 'aqi-moderate'
  if (numAqi <= 150) return 'aqi-unhealthy'
  return 'aqi-very-unhealthy'
}
</script>

<style scoped>
.current-weather-card {
  border-radius: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.city-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.city-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.update-time {
  font-size: 12px;
  color: #909399;
}

.weather-content {
  padding: 8px 0;
}

.main-info {
  text-align: center;
  margin-bottom: 16px;
}

.temperature {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 8px;
  margin-bottom: 8px;
}

.temp-value {
  font-size: 56px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}

.temp-unit {
  font-size: 24px;
  color: #909399;
}

.description {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
}

.weather-desc {
  font-size: 16px;
  color: #606266;
}

.feels-like {
  font-size: 14px;
  color: #909399;
}

.details {
  margin-top: 16px;
}

.detail-item {
  text-align: center;
  padding: 12px 0;
}

.detail-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.detail-value {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.aqi-good { color: #67c23a; }
.aqi-moderate { color: #e6a23c; }
.aqi-unhealthy { color: #f56c6c; }
.aqi-very-unhealthy { color: #8b0000; }
</style>