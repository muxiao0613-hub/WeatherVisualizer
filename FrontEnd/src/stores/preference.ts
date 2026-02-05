import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { PreferenceDTO } from '@/types'

export const usePreferenceStore = defineStore('preference', () => {
  const preferences = ref<PreferenceDTO>({
    defaultCity: '北京',
    temperatureUnit: 'C',
    windSpeedUnit: 'm/s',
    showCurrentCard: true,
    showLineChart: true,
    showBarChart: true,
    showGaugeCard: true,
    showAlertsCard: true,
    showAiAssistant: true
  })

  const setPreferences = (data: PreferenceDTO) => {
    console.log('Preference store: 更新设置', data)
    preferences.value = data
    console.log('Preference store: 更新后的设置', preferences.value)
  }

  return {
    preferences,
    setPreferences
  }
})
