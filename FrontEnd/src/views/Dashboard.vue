<template>
  <AppShell>
    <div class="dashboard">
      <el-row :gutter="24">
        <el-col :span="24" v-show="preferenceStore.preferences.showCurrentCard">
          <CurrentWeatherCard
            :weather="weatherStore.currentWeather"
            :loading="weatherStore.loading"
          />
        </el-col>

        <el-col :span="24" :lg="12" v-show="preferenceStore.preferences.showLineChart">
          <LineChartCard
            :data="weatherStore.hourlyForecast"
            :loading="weatherStore.loading"
          />
        </el-col>

        <el-col :span="24" :lg="12" v-show="preferenceStore.preferences.showBarChart">
          <BarChartCard
            :data="weatherStore.dailyForecast"
            :loading="weatherStore.loading"
          />
        </el-col>

        <el-col :span="24" :lg="8" v-show="preferenceStore.preferences.showGaugeCard">
          <GaugeCard
            :weather="weatherStore.currentWeather"
            :loading="weatherStore.loading"
          />
        </el-col>

        <el-col :span="24" :lg="16" v-show="preferenceStore.preferences.showAlertsCard">
          <AlertsCard
            :alerts="weatherStore.alerts"
            :loading="weatherStore.loading"
          />
        </el-col>

        <el-col :span="24" v-show="preferenceStore.preferences.showAiAssistant">
          <AiChatCard />
        </el-col>
      </el-row>
    </div>
  </AppShell>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
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
import { weatherApi, preferenceApi, cityApi } from '@/services/api'
import { ElMessage } from 'element-plus'

const weatherStore = useWeatherStore()
const cityStore = useCityStore()
const preferenceStore = usePreferenceStore()

const loadWeatherData = async () => {
  weatherStore.setLoading(true)
  weatherStore.clearError()

  console.log('=== loadWeatherData 开始 ===')

  try {
    const city = cityStore.currentCity
    console.log('请求城市:', city.name, '坐标:', city.lat, city.lon)

    const [current, hourly, daily, alerts] = await Promise.all([
      weatherApi.getCurrentWeather(city.lat, city.lon, city.name),
      weatherApi.getHourlyForecast(city.lat, city.lon, city.name),
      weatherApi.getDailyForecast(city.lat, city.lon, city.name),
      weatherApi.getAlerts(city.lat, city.lon, city.name)
    ])

    console.log('API返回城市:', current.city, '请求数量:', { hourly: hourly.length, daily: daily.length, alerts: alerts.length })
    
    weatherStore.setCurrentWeather(current)
    weatherStore.setHourlyForecast(hourly)
    weatherStore.setDailyForecast(daily)
    weatherStore.setAlerts(alerts)
    
    console.log('=== loadWeatherData 完成 ===')
  } catch (error) {
    weatherStore.setError('加载天气数据失败')
    ElMessage.error('加载天气数据失败，请稍后重试')
    console.error('loadWeatherData 失败:', error)
  } finally {
    weatherStore.setLoading(false)
  }
}

const loadPreferences = async () => {
  try {
    const preferences = await preferenceApi.getPreferences()
    preferenceStore.setPreferences(preferences)
    console.log('偏好设置加载完成:', preferences.defaultCity)
  } catch (error) {
    console.error('加载偏好设置失败:', error)
  }
}

onMounted(async () => {
  console.log('=== Dashboard mounted ===')
  await loadPreferences()
  
  const defaultCityName = preferenceStore.preferences.defaultCity
  console.log('默认城市:', defaultCityName, '当前城市:', cityStore.currentCity.name)
  
  if (defaultCityName && defaultCityName !== cityStore.currentCity.name) {
    console.log('搜索默认城市...')
    try {
      const cities = await cityApi.searchCities(defaultCityName)
      if (cities && cities.length > 0) {
        console.log('找到默认城市:', cities[0].name)
        cityStore.setCurrentCity(cities[0])
        return
      }
    } catch (error) {
      console.error('搜索默认城市失败:', error)
    }
  }
  
  console.log('Dashboard初始化完成')
})

watch(() => cityStore.currentCity, (newCity, oldCity) => {
  console.log('=== Dashboard watch triggered ===')
  console.log('Old city:', oldCity?.name, 'New city:', newCity?.name)
  console.log('是否加载?', newCity && newCity.name !== oldCity?.name)
  
  if (newCity && newCity.name !== oldCity?.name) {
    console.log('✓ 触发loadWeatherData')
    loadWeatherData()
  } else {
    console.log('✗ 不触发loadWeatherData')
  }
}, { deep: true, immediate: true })

// 监听偏好设置变化
watch(() => preferenceStore.preferences, (newPrefs) => {
  console.log('偏好设置发生变化:', newPrefs)
}, { deep: true })

// 监听Weather Store变化，图表组件会自动响应数据变化
watch(() => [
  weatherStore.currentWeather,
  weatherStore.hourlyForecast,
  weatherStore.dailyForecast
], () => {
  console.log('=== Weather Store 数据变化 ===')
  console.log('城市:', weatherStore.currentWeather?.city, '小时预报:', weatherStore.hourlyForecast.length, '天预报:', weatherStore.dailyForecast.length)
}, { deep: true })
</script>

<style scoped>
.dashboard {
  padding: 0;
  min-height: 100%;
}

.el-row {
  margin: 0 !important;
}

.el-col {
  margin-bottom: 24px;
  padding: 0 12px;
}

@media (max-width: 1200px) {
  .el-col {
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .el-col {
    margin-bottom: 16px;
    padding: 0 8px;
  }
}
</style>
