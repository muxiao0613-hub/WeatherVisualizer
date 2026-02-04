export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface CityDTO {
  name: string
  country: string
  state?: string
  lat: number
  lon: number
}

export interface CurrentWeatherDTO {
  city: string
  lat: number
  lon: number
  temp: number
  feelsLike: number
  description: string
  icon: string
  windSpeed: number
  windDeg: number
  humidity: number
  pressure: number
  visibility: number
  timestamp: number
  country?: string
  aqi?: number
}

export interface HourlyForecastDTO {
  city: string
  lat: number
  lon: number
  time: string
  temp: number
  feelsLike: number
  description: string
  icon: string
  windSpeed: number
  humidity: number
  pop: number
}

export interface DailyForecastDTO {
  city: string
  lat: number
  lon: number
  date: string
  tempMin: number
  tempMax: number
  description: string
  icon: string
  windSpeed: number
  humidity: number
  pop: number
}

export interface AlertDTO {
  city: string
  lat: number
  lon: number
  event: string
  description: string
  start: string
  end: string
  level: string
  tags: string
}

export interface PreferenceDTO {
  id?: number
  defaultCity: string
  temperatureUnit: string
  windSpeedUnit: string
  showCurrentCard: boolean
  showLineChart: boolean
  showBarChart: boolean
  showGaugeCard: boolean
  showAlertsCard: boolean
  showAiAssistant: boolean
}

export interface ChatRequestDTO {
  question: string
  city: CityDTO
}

export interface ChatResponseDTO {
  answer: string
  references: string[]
}

export interface FavoriteCreateDTO {
  name: string
  country: string
  state?: string
  lat: number
  lon: number
}
