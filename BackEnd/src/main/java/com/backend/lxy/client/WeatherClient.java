package com.backend.lxy.client;

import com.backend.lxy.config.WeatherProperties;
import com.backend.lxy.domain.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherClient {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final WeatherProperties weatherProperties;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String generateJwtToken() throws Exception {
        WeatherProperties.Jwt jwtConfig = weatherProperties.getJwt();
        String privateKeyPem = jwtConfig.getPrivateKeyPem()
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("Ed25519", "BC");
        Key privateKey = keyFactory.generatePrivate(keySpec);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + 3600 * 1000);

        return Jwts.builder()
                .subject(jwtConfig.getProjectId())
                .issuedAt(now)
                .expiration(expiration)
                .issuer(jwtConfig.getCredentialId())
                .signWith(privateKey)
                .compact();
    }

    private Request buildAuthenticatedRequest(String url) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get();

        if ("QWEATHER".equalsIgnoreCase(weatherProperties.getProvider())) {
            if (weatherProperties.hasJwtConfig()) {
                try {
                    String jwtToken = generateJwtToken();
                    requestBuilder.addHeader("Authorization", "Bearer " + jwtToken);
                } catch (Exception e) {
                    log.error("Failed to generate JWT token", e);
                    throw new IOException("Failed to generate JWT token", e);
                }
            } else if (weatherProperties.hasApiKey()) {
                requestBuilder.addHeader("X-QW-Api-Key", weatherProperties.getApiKey());
            }
        }

        return requestBuilder.build();
    }

    public CurrentWeatherDTO getCurrentWeather(Double lat, Double lon, String city) throws IOException {
        String url;

        if ("QWEATHER".equalsIgnoreCase(weatherProperties.getProvider())) {
            String token = "";
            if (weatherProperties.hasJwtConfig()) {
                try {
                    token = generateJwtToken();
                } catch (Exception e) {
                    log.error("Failed to generate JWT token", e);
                    throw new IOException("Failed to generate JWT token", e);
                }
            } else if (weatherProperties.hasApiKey()) {
                token = weatherProperties.getApiKey();
            }
            url = String.format("%s/weather/now?location=%s&token=%s",
                    weatherProperties.getBaseUrl(), String.format("%.2f,%.2f", lon, lat), token);
        } else {
            url = String.format("%s?lat=%s&lon=%s&appid=%s&units=metric",
                    weatherProperties.getBaseUrl(), lat, lon, weatherProperties.getApiKey());
        }

        log.info("🌤️ Weather API request URL: {}", url);

        try (Response response = httpClient.newCall(buildAuthenticatedRequest(url)).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                log.error("❌ Weather API error - Status: {}, Body: {}", response.code(), errorBody);
                throw new IOException("Unexpected response code: " + response.code() + ", body: " + errorBody);
            }

            String responseBody = response.body().string();
            log.info("✅ Weather API response received: {}", responseBody);
            JsonNode root = objectMapper.readTree(responseBody);

            if ("QWEATHER".equalsIgnoreCase(weatherProperties.getProvider())) {
                JsonNode now = root.get("now");
                String icon = now.has("icon") ? now.get("icon").asText() : "01d";
                int windDeg = now.has("windDir") ? parseWindDirection(now.get("windDir").asText()) : 0;
                
                return CurrentWeatherDTO.builder()
                        .city(city)
                        .lat(lat)
                        .lon(lon)
                        .temp(now.get("temp").asDouble())
                        .feelsLike(now.get("feelsLike").asDouble())
                        .description(now.get("text").asText())
                        .icon(icon)
                        .windSpeed(now.get("windSpeed").asDouble())
                        .windDeg(windDeg)
                        .humidity(now.get("humidity").asInt())
                        .pressure(now.get("pressure").asDouble())
                        .visibility(now.get("vis").asDouble())
                        .timestamp(System.currentTimeMillis() / 1000)
                        .build();
            } else {
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
    }

    public List<HourlyForecastDTO> getHourlyForecast(Double lat, Double lon, String city) throws IOException {
        String url;

        if ("QWEATHER".equalsIgnoreCase(weatherProperties.getProvider())) {
            String token = "";
            if (weatherProperties.hasJwtConfig()) {
                try {
                    token = generateJwtToken();
                } catch (Exception e) {
                    log.error("Failed to generate JWT token", e);
                    throw new IOException("Failed to generate JWT token", e);
                }
            } else if (weatherProperties.hasApiKey()) {
                token = weatherProperties.getApiKey();
            }
            url = String.format("%s/weather/24h?location=%s&token=%s",
                    weatherProperties.getBaseUrl(), String.format("%.2f,%.2f", lon, lat), token);
        } else {
            url = String.format("%s?lat=%s&lon=%s&appid=%s&units=metric",
                    weatherProperties.getBaseUrl(), lat, lon, weatherProperties.getApiKey());
        }

        log.info("🌤️ Hourly Forecast API request URL: {}", url);

        try (Response response = httpClient.newCall(buildAuthenticatedRequest(url)).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                log.error("❌ Hourly Forecast API error - Status: {}, Body: {}", response.code(), errorBody);
                throw new IOException("Unexpected response code: " + response.code() + ", body: " + errorBody);
            }

            String responseBody = response.body().string();
            log.info("✅ Hourly Forecast API response received");
            JsonNode root = objectMapper.readTree(responseBody);

            List<HourlyForecastDTO> forecasts = new ArrayList<>();

            if ("QWEATHER".equalsIgnoreCase(weatherProperties.getProvider())) {
                JsonNode hourly = root.get("hourly");
                for (int i = 0; i < 24 && i < hourly.size(); i++) {
                    JsonNode hour = hourly.get(i);
                    forecasts.add(HourlyForecastDTO.builder()
                            .city(city)
                            .lat(lat)
                            .lon(lon)
                            .time(LocalDateTime.parse(hour.get("fxTime").asText().replace("T", " ")))
                            .temp(hour.get("temp").asDouble())
                            .feelsLike(hour.get("feelsLike").asDouble())
                            .description(hour.get("text").asText())
                            .icon(hour.get("icon").asText())
                            .windSpeed(hour.get("windSpeed").asDouble())
                            .humidity(hour.get("humidity").asInt())
                            .pop(hour.has("pop") ? hour.get("pop").asDouble() : 0.0)
                            .build());
                }
            } else {
                JsonNode hourly = root.get("hourly");
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
            }
            return forecasts;
        }
    }

    public List<DailyForecastDTO> getDailyForecast(Double lat, Double lon, String city) throws IOException {
        String url;

        if ("QWEATHER".equalsIgnoreCase(weatherProperties.getProvider())) {
            String token = "";
            if (weatherProperties.hasJwtConfig()) {
                try {
                    token = generateJwtToken();
                } catch (Exception e) {
                    log.error("Failed to generate JWT token", e);
                    throw new IOException("Failed to generate JWT token", e);
                }
            } else if (weatherProperties.hasApiKey()) {
                token = weatherProperties.getApiKey();
            }
            url = String.format("%s/weather/7d?location=%s&token=%s",
                    weatherProperties.getBaseUrl(), String.format("%.2f,%.2f", lon, lat), token);
        } else {
            url = String.format("%s?lat=%s&lon=%s&appid=%s&units=metric",
                    weatherProperties.getBaseUrl(), lat, lon, weatherProperties.getApiKey());
        }

        log.info("🌤️ Daily Forecast API request URL: {}", url);

        try (Response response = httpClient.newCall(buildAuthenticatedRequest(url)).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                log.error("❌ Daily Forecast API error - Status: {}, Body: {}", response.code(), errorBody);
                throw new IOException("Unexpected response code: " + response.code() + ", body: " + errorBody);
            }

            String responseBody = response.body().string();
            log.info("✅ Daily Forecast API response received");
            JsonNode root = objectMapper.readTree(responseBody);

            List<DailyForecastDTO> forecasts = new ArrayList<>();

            if ("QWEATHER".equalsIgnoreCase(weatherProperties.getProvider())) {
                JsonNode daily = root.get("daily");
                for (int i = 0; i < 7 && i < daily.size(); i++) {
                    JsonNode day = daily.get(i);
                    forecasts.add(DailyForecastDTO.builder()
                            .city(city)
                            .lat(lat)
                            .lon(lon)
                            .date(LocalDate.parse(day.get("fxDate").asText()))
                            .tempMin(day.get("tempMin").asDouble())
                            .tempMax(day.get("tempMax").asDouble())
                            .description(day.get("textDay").asText())
                            .icon(day.get("iconDay").asText())
                            .windSpeed(day.get("windSpeedDay").asDouble())
                            .humidity(day.get("humidity").asInt())
                            .pop(day.has("pop") ? day.get("pop").asDouble() : 0.0)
                            .build());
                }
            } else {
                JsonNode daily = root.get("daily");
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
            }
            return forecasts;
        }
    }

    public List<AlertDTO> getAlerts(Double lat, Double lon, String city) throws IOException {
        String url;

        if ("QWEATHER".equalsIgnoreCase(weatherProperties.getProvider())) {
            String token = "";
            if (weatherProperties.hasJwtConfig()) {
                try {
                    token = generateJwtToken();
                } catch (Exception e) {
                    log.error("Failed to generate JWT token", e);
                    throw new IOException("Failed to generate JWT token", e);
                }
            } else if (weatherProperties.hasApiKey()) {
                token = weatherProperties.getApiKey();
            }
            url = String.format("%s/warning/1d?location=%s&token=%s",
                    weatherProperties.getBaseUrl(), String.format("%.2f,%.2f", lon, lat), token);
        } else {
            url = String.format("%s?lat=%s&lon=%s&appid=%s&units=metric",
                    weatherProperties.getBaseUrl(), lat, lon, weatherProperties.getApiKey());
        }

        log.info("🌤️ Alerts API request URL: {}", url);

        try (Response response = httpClient.newCall(buildAuthenticatedRequest(url)).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                log.error("❌ Alerts API error - Status: {}, Body: {}", response.code(), errorBody);
                throw new IOException("Unexpected response code: " + response.code() + ", body: " + errorBody);
            }

            String responseBody = response.body().string();
            log.info("✅ Alerts API response received");
            JsonNode root = objectMapper.readTree(responseBody);

            if ("QWEATHER".equalsIgnoreCase(weatherProperties.getProvider())) {
                return new ArrayList<>();
            } else {
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

    private int parseWindDirection(String windDir) {
        if (windDir == null || windDir.isEmpty()) {
            return 0;
        }
        
        switch (windDir) {
            case "北": return 0;
            case "东北": return 45;
            case "东": return 90;
            case "东南": return 135;
            case "南": return 180;
            case "西南": return 225;
            case "西": return 270;
            case "西北": return 315;
            default: return 0;
        }
    }
}
