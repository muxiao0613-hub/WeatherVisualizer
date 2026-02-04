<template>
  <AppShell>
    <div class="dashboard">
      <el-row :gutter="24">
        <el-col :span="24" v-if="preferenceStore.preferences.showCurrentCard">
          <CurrentWeatherCard
            :weather="weatherStore.currentWeather"
            :loading="weatherStore.loading"
          />
        </el-col>

        <el-col :span="24" :lg="12" v-if="preferenceStore.preferences.showLineChart">
          <LineChartCard
            :data="weatherStore.hourlyForecast"
            :loading="weatherStore.loading"
          />
        </el-col>

        <el-col :span="24" :lg="12" v-if="preferenceStore.preferences.showBarChart">
          <BarChartCard
            :data="weatherStore.dailyForecast"
            :loading="weatherStore.loading"
          />
        </el-col>

        <el-col :span="24" :lg="8" v-if="preferenceStore.preferences.showGaugeCard">
          <GaugeCard
            :weather="weatherStore.currentWeather"
            :loading="weatherStore.loading"
          />
        </el-col>

        <el-col :span="24" :lg="16" v-if="preferenceStore.preferences.showAlertsCard">
          <AlertsCard
            :alerts="weatherStore.alerts"
            :loading="weatherStore.loading"
          />
        </el-col>

        <el-col :span="24" v-if="preferenceStore.preferences.showAiAssistant">
          <AiChatCard />
        </el-col>
      </el-row>
    </div>
  </AppShell>
</template>

<script setup lang="ts">
import { onMounted, watch } from 'vue'
import AppShell from '@/components/layout/AppShell.vue'
import CurrentWeatherCard from '@/components/weather/CurrentWeatherCard.vue'
import LineChartCard from '@/components/weather/LineChartCard.vue'
import BarChartCard from '@/components/weather/BarChartCard.vue'
import GaugeCard from '@/components/weather/GaugeCard.vue'
import AlertsCard from '@/components/weather/AlertsCard.vue'
import AiChatCard from '@/components/weather/AiChatCard.vue'
import { useWeatherStore } from '@/stores/weather'
import { useCityStore } from '@/stores/city'
import { usePreferenceStore } from '@/stores/preference'
import { weatherApi, preferenceApi } from '@/services/api'
import { ElMessage } from 'element-plus'

const weatherStore = useWeatherStore()
const cityStore = useCityStore()
const preferenceStore = usePreferenceStore()

const loadWeatherData = async () => {
  weatherStore.setLoading(true)
  weatherStore.clearError()

  try {
    const city = cityStore.currentCity

    const [current, hourly, daily, alerts] = await Promise.all([
      weatherApi.getCurrentWeather(city.lat, city.lon, city.name),
      weatherApi.getHourlyForecast(city.lat, city.lon, city.name),
      weatherApi.getDailyForecast(city.lat, city.lon, city.name),
      weatherApi.getAlerts(city.lat, city.lon, city.name)
    ])

    console.log('Received weather data:', { current, hourly: hourly.length, daily: daily.length, alerts: alerts.length })
    weatherStore.setCurrentWeather(current)
    weatherStore.setHourlyForecast(hourly)
    weatherStore.setDailyForecast(daily)
    weatherStore.setAlerts(alerts)
  } catch (error) {
    weatherStore.setError('加载天气数据失败')
    ElMessage.error('加载天气数据失败，请稍后重试')
    console.error('Failed to load weather data:', error)
  } finally {
    weatherStore.setLoading(false)
  }
}

const loadPreferences = async () => {
  try {
    const preferences = await preferenceApi.getPreferences()
    preferenceStore.setPreferences(preferences)
  } catch (error) {
    console.error('Failed to load preferences:', error)
  }
}

onMounted(async () => {
  console.log('Dashboard mounted')
  await loadPreferences()
  console.log('Preferences loaded:', preferenceStore.preferences)
  await loadWeatherData()
  console.log('Weather data loaded:', {
    current: weatherStore.currentWeather,
    hourly: weatherStore.hourlyForecast.length,
    daily: weatherStore.dailyForecast.length,
    alerts: weatherStore.alerts.length
  })
})

watch(() => cityStore.currentCity, () => {
  loadWeatherData()
}, { deep: true })
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.el-col {
  margin-bottom: 24px;
}
</style>
