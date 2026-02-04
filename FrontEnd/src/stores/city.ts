import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { CityDTO } from '@/types'

export const useCityStore = defineStore('city', () => {
  const currentCity = ref<CityDTO>({
    name: 'Beijing',
    country: 'CN',
    lat: 39.9042,
    lon: 116.4074
  })

  const setCurrentCity = (city: CityDTO) => {
    currentCity.value = city
  }

  return {
    currentCity,
    setCurrentCity
  }
})
