import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { PreferenceDTO } from '@/types'

export const usePreferenceStore = defineStore('preference', () => {
  const preferences = ref<PreferenceDTO>({
    defaultCity: 'Beijing',
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
    preferences.value = data
  }

  return {
    preferences,
    setPreferences
  }
})
