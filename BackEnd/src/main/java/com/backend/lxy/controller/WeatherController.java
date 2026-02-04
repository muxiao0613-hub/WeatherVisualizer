package com.backend.lxy.controller;

import com.backend.lxy.common.ApiResponse;
import com.backend.lxy.domain.dto.*;
import com.backend.lxy.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Tag(name = "Weather", description = "Weather data APIs")
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/current")
    @Operation(summary = "Get current weather")
    public ApiResponse<CurrentWeatherDTO> getCurrentWeather(
            @Parameter(description = "Latitude") @RequestParam Double lat,
            @Parameter(description = "Longitude") @RequestParam Double lon,
            @Parameter(description = "City name") @RequestParam(defaultValue = "Unknown") String city) {
        CurrentWeatherDTO weather = weatherService.getCurrentWeather(lat, lon, city);
        return ApiResponse.success(weather);
    }

    @GetMapping("/forecast/hourly")
    @Operation(summary = "Get hourly forecast (24h)")
    public ApiResponse<List<HourlyForecastDTO>> getHourlyForecast(
            @Parameter(description = "Latitude") @RequestParam Double lat,
            @Parameter(description = "Longitude") @RequestParam Double lon,
            @Parameter(description = "City name") @RequestParam(defaultValue = "Unknown") String city) {
        List<HourlyForecastDTO> forecast = weatherService.getHourlyForecast(lat, lon, city);
        return ApiResponse.success(forecast);
    }

    @GetMapping("/forecast/daily")
    @Operation(summary = "Get daily forecast (7d)")
    public ApiResponse<List<DailyForecastDTO>> getDailyForecast(
            @Parameter(description = "Latitude") @RequestParam Double lat,
            @Parameter(description = "Longitude") @RequestParam Double lon,
            @Parameter(description = "City name") @RequestParam(defaultValue = "Unknown") String city) {
        List<DailyForecastDTO> forecast = weatherService.getDailyForecast(lat, lon, city);
        return ApiResponse.success(forecast);
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get weather alerts")
    public ApiResponse<List<AlertDTO>> getAlerts(
            @Parameter(description = "Latitude") @RequestParam Double lat,
            @Parameter(description = "Longitude") @RequestParam Double lon,
            @Parameter(description = "City name") @RequestParam(defaultValue = "Unknown") String city) {
        List<AlertDTO> alerts = weatherService.getAlerts(lat, lon, city);
        return ApiResponse.success(alerts);
    }
}
