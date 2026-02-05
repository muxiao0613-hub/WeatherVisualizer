import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { CityDTO } from '@/types'

export const useCityStore = defineStore('city', () => {
  const currentCity = ref<CityDTO>({
    name: '',
    country: '',
    lat: 0,
    lon: 0
  })

  const setCurrentCity = (city: CityDTO) => {
    currentCity.value = city
  }

  return {
    currentCity,
    setCurrentCity
  }
})
