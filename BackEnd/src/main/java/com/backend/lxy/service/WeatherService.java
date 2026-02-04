package com.backend.lxy.service;

import com.backend.lxy.client.MockDataFactory;
import com.backend.lxy.client.WeatherClient;
import com.backend.lxy.config.AppProperties;
import com.backend.lxy.config.WeatherProperties;
import com.backend.lxy.domain.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final WeatherClient weatherClient;
    private final MockDataFactory mockDataFactory;
    private final AppProperties appProperties;
    private final WeatherProperties weatherProperties;

    public CurrentWeatherDTO getCurrentWeather(Double lat, Double lon, String city) {
        if (shouldUseMock()) {
            log.debug("Using mock data for current weather");
            return mockDataFactory.mockCurrentWeather(city, lat, lon);
        }

        try {
            return weatherClient.getCurrentWeather(lat, lon, city);
        } catch (IOException e) {
            log.warn("Failed to fetch current weather, falling back to mock: {}", e.getMessage());
            return mockDataFactory.mockCurrentWeather(city, lat, lon);
        }
    }

    public List<HourlyForecastDTO> getHourlyForecast(Double lat, Double lon, String city) {
        if (shouldUseMock()) {
            log.debug("Using mock data for hourly forecast");
            return mockDataFactory.mockHourlyForecast(city, lat, lon);
        }

        try {
            return weatherClient.getHourlyForecast(lat, lon, city);
        } catch (IOException e) {
            log.warn("Failed to fetch hourly forecast, falling back to mock: {}", e.getMessage());
            return mockDataFactory.mockHourlyForecast(city, lat, lon);
        }
    }

    public List<DailyForecastDTO> getDailyForecast(Double lat, Double lon, String city) {
        if (shouldUseMock()) {
            log.debug("Using mock data for daily forecast");
            return mockDataFactory.mockDailyForecast(city, lat, lon);
        }

        try {
            return weatherClient.getDailyForecast(lat, lon, city);
        } catch (IOException e) {
            log.warn("Failed to fetch daily forecast, falling back to mock: {}", e.getMessage());
            return mockDataFactory.mockDailyForecast(city, lat, lon);
        }
    }

    public List<AlertDTO> getAlerts(Double lat, Double lon, String city) {
        if (shouldUseMock()) {
            log.debug("Using mock data for alerts");
            return mockDataFactory.mockAlerts(city, lat, lon);
        }

        try {
            return weatherClient.getAlerts(lat, lon, city);
        } catch (IOException e) {
            log.warn("Failed to fetch alerts, falling back to mock: {}", e.getMessage());
            return mockDataFactory.mockAlerts(city, lat, lon);
        }
    }

    private boolean shouldUseMock() {
        return appProperties.isMockEnabled() || !weatherProperties.hasApiKey();
    }
}
