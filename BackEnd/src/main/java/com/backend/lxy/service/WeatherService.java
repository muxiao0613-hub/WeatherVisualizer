package com.backend.lxy.service;

import com.backend.lxy.client.WeatherClient;
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
    private final WeatherProperties weatherProperties;

    public CurrentWeatherDTO getCurrentWeather(Double lat, Double lon, String city) throws IOException {
        return weatherClient.getCurrentWeather(lat, lon, city);
    }

    public List<HourlyForecastDTO> getHourlyForecast(Double lat, Double lon, String city) throws IOException {
        return weatherClient.getHourlyForecast(lat, lon, city);
    }

    public List<DailyForecastDTO> getDailyForecast(Double lat, Double lon, String city) throws IOException {
        return weatherClient.getDailyForecast(lat, lon, city);
    }

    public List<AlertDTO> getAlerts(Double lat, Double lon, String city) throws IOException {
        return weatherClient.getAlerts(lat, lon, city);
    }
}
