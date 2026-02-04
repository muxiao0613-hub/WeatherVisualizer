import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { CurrentWeatherDTO, HourlyForecastDTO, DailyForecastDTO, AlertDTO } from '@/types'

export const useWeatherStore = defineStore('weather', () => {
  const currentWeather = ref<CurrentWeatherDTO | null>(null)
  const hourlyForecast = ref<HourlyForecastDTO[]>([])
  const dailyForecast = ref<DailyForecastDTO[]>([])
  const alerts = ref<AlertDTO[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const setCurrentWeather = (data: CurrentWeatherDTO) => {
    currentWeather.value = data
  }

  const setHourlyForecast = (data: HourlyForecastDTO[]) => {
    hourlyForecast.value = data
  }

  const setDailyForecast = (data: DailyForecastDTO[]) => {
    dailyForecast.value = data
  }

  const setAlerts = (data: AlertDTO[]) => {
    alerts.value = data
  }

  const setLoading = (value: boolean) => {
    loading.value = value
  }

  const setError = (value: string | null) => {
    error.value = value
  }

  const clearError = () => {
    error.value = null
  }

  return {
    currentWeather,
    hourlyForecast,
    dailyForecast,
    alerts,
    loading,
    error,
    setCurrentWeather,
    setHourlyForecast,
    setDailyForecast,
    setAlerts,
    setLoading,
    setError,
    clearError
  }
})
