import http from './http'
import type {
  CityDTO,
  CurrentWeatherDTO,
  HourlyForecastDTO,
  DailyForecastDTO,
  AlertDTO,
  PreferenceDTO,
  ChatRequestDTO,
  ChatResponseDTO,
  FavoriteCreateDTO
} from '@/types'

export const weatherApi = {
  getCurrentWeather: (lat: number, lon: number, city: string) => {
    return http.get<CurrentWeatherDTO>('/api/weather/current', {
      params: { lat, lon, city }
    })
  },

  getHourlyForecast: (lat: number, lon: number, city: string) => {
    return http.get<HourlyForecastDTO[]>('/api/weather/forecast/hourly', {
      params: { lat, lon, city }
    })
  },

  getDailyForecast: (lat: number, lon: number, city: string) => {
    return http.get<DailyForecastDTO[]>('/api/weather/forecast/daily', {
      params: { lat, lon, city }
    })
  },

  getAlerts: (lat: number, lon: number, city: string) => {
    return http.get<AlertDTO[]>('/api/weather/alerts', {
      params: { lat, lon, city }
    })
  }
}

export const cityApi = {
  searchCities: (keyword: string) => {
    return http.get<CityDTO[]>('/api/cities/search', {
      params: { keyword }
    })
  }
}

export const preferenceApi = {
  getPreferences: () => {
    return http.get<PreferenceDTO>('/api/preferences')
  },

  updatePreferences: (data: PreferenceDTO) => {
    return http.put<PreferenceDTO>('/api/preferences', data)
  }
}

export const favoriteApi = {
  getAllFavorites: () => {
    return http.get<CityDTO[]>('/api/favorites')
  },

  addFavorite: (data: FavoriteCreateDTO) => {
    return http.post<CityDTO>('/api/favorites', data)
  },

  removeFavorite: (name: string, country: string, lat: number, lon: number) => {
    return http.delete<void>('/api/favorites', {
      params: { name, country, lat, lon }
    })
  }
}

export const aiApi = {
  chat: (data: ChatRequestDTO) => {
    return http.post<ChatResponseDTO>('/api/ai/chat', data)
  }
}

export const healthApi = {
  check: () => {
    return http.get<{ status: string; service: string; version: string }>('/api/health')
  }
}
