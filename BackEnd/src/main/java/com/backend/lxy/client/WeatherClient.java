package com.backend.lxy.client;

import com.backend.lxy.config.WeatherProperties;
import com.backend.lxy.domain.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherClient {

    private final WeatherProperties weatherProperties;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CurrentWeatherDTO getCurrentWeather(Double lat, Double lon, String city) throws IOException {
        String url = String.format("%s?lat=%s&lon=%s&appid=%s&units=metric",
                weatherProperties.getBaseUrl(), lat, lon, weatherProperties.getApiKey());

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode current = root.get("current");

            return CurrentWeatherDTO.builder()
                    .city(city)
                    .lat(lat)
                    .lon(lon)
                    .temp(current.get("temp").asDouble())
                    .feelsLike(current.get("feels_like").asDouble())
                    .description(current.get("weather").get(0).get("description").asText())
                    .icon(current.get("weather").get(0).get("icon").asText())
                    .windSpeed(current.get("wind_speed").asDouble())
                    .windDeg(current.get("wind_deg").asInt())
                    .humidity(current.get("humidity").asInt())
                    .pressure(current.get("pressure").asDouble())
                    .visibility(current.get("visibility").asDouble() / 1000.0)
                    .timestamp(current.get("dt").asLong())
                    .build();
        }
    }

    public List<HourlyForecastDTO> getHourlyForecast(Double lat, Double lon, String city) throws IOException {
        String url = String.format("%s?lat=%s&lon=%s&appid=%s&units=metric",
                weatherProperties.getBaseUrl(), lat, lon, weatherProperties.getApiKey());

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode hourly = root.get("hourly");

            List<HourlyForecastDTO> forecasts = new ArrayList<>();
            for (int i = 0; i < 24 && i < hourly.size(); i++) {
                JsonNode hour = hourly.get(i);
                forecasts.add(HourlyForecastDTO.builder()
                        .city(city)
                        .lat(lat)
                        .lon(lon)
                        .time(LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(hour.get("dt").asLong()),
                                ZoneId.systemDefault()))
                        .temp(hour.get("temp").asDouble())
                        .feelsLike(hour.get("feels_like").asDouble())
                        .description(hour.get("weather").get(0).get("description").asText())
                        .icon(hour.get("weather").get(0).get("icon").asText())
                        .windSpeed(hour.get("wind_speed").asDouble())
                        .humidity(hour.get("humidity").asInt())
                        .pop(hour.has("pop") ? hour.get("pop").asDouble() : 0.0)
                        .build());
            }
            return forecasts;
        }
    }

    public List<DailyForecastDTO> getDailyForecast(Double lat, Double lon, String city) throws IOException {
        String url = String.format("%s?lat=%s&lon=%s&appid=%s&units=metric",
                weatherProperties.getBaseUrl(), lat, lon, weatherProperties.getApiKey());

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode daily = root.get("daily");

            List<DailyForecastDTO> forecasts = new ArrayList<>();
            for (int i = 0; i < 7 && i < daily.size(); i++) {
                JsonNode day = daily.get(i);
                forecasts.add(DailyForecastDTO.builder()
                        .city(city)
                        .lat(lat)
                        .lon(lon)
                        .date(LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(day.get("dt").asLong()),
                                ZoneId.systemDefault()).toLocalDate())
                        .tempMin(day.get("temp").get("min").asDouble())
                        .tempMax(day.get("temp").get("max").asDouble())
                        .description(day.get("weather").get(0).get("description").asText())
                        .icon(day.get("weather").get(0).get("icon").asText())
                        .windSpeed(day.get("wind_speed").asDouble())
                        .humidity(day.get("humidity").asInt())
                        .pop(day.has("pop") ? day.get("pop").asDouble() : 0.0)
                        .build());
            }
            return forecasts;
        }
    }

    public List<AlertDTO> getAlerts(Double lat, Double lon, String city) throws IOException {
        String url = String.format("%s?lat=%s&lon=%s&appid=%s&units=metric",
                weatherProperties.getBaseUrl(), lat, lon, weatherProperties.getApiKey());

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode root = objectMapper.readTree(responseBody);

            if (!root.has("alerts") || root.get("alerts").isNull()) {
                return new ArrayList<>();
            }

            JsonNode alerts = root.get("alerts");
            List<AlertDTO> alertList = new ArrayList<>();

            for (JsonNode alert : alerts) {
                alertList.add(AlertDTO.builder()
                        .city(city)
                        .lat(lat)
                        .lon(lon)
                        .event(alert.get("event").asText())
                        .description(alert.get("description").asText())
                        .start(LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(alert.get("start").asLong()),
                                ZoneId.systemDefault()))
                        .end(LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(alert.get("end").asLong()),
                                ZoneId.systemDefault()))
                        .level("warning")
                        .tags(alert.has("tags") ? alert.get("tags").toString() : "")
                        .build());
            }
            return alertList;
        }
    }
}
