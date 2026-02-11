package com.backend.lxy.client;

import com.backend.lxy.config.WeatherProperties;
import com.backend.lxy.domain.dto.AlertDTO;
import com.backend.lxy.domain.dto.CityDTO;
import com.backend.lxy.domain.dto.CurrentWeatherDTO;
import com.backend.lxy.domain.dto.DailyForecastDTO;
import com.backend.lxy.domain.dto.HourlyForecastDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.Signature;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherClient {

    private final WeatherProperties weatherProperties;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    
    // Cache JWT token to avoid regenerating for each request
    private String cachedJwtToken = null;
    private long jwtTokenExpiryTime = 0;

    public List<CityDTO> searchCities(String keyword) throws IOException {
        String url = String.format("%s/geo/v2/city/lookup?location=%s&number=20&range=cn",
                weatherProperties.getBaseUrl(), java.net.URLEncoder.encode(keyword, "UTF-8"));

        log.info("City search API call - keyword: {}, URL: {}", keyword, url);
        
        try (Response response = httpClient.newCall(
                new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "WeatherVisualizer/1.0")
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "Bearer " + generateJwtToken())
                        .get()
                        .build()
        ).execute()) {
            
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                log.error("City search API error - Status: {}, Body: {}", response.code(), errorBody);
                throw new IOException("Unexpected response code: " + response.code() + ", body: " + errorBody);
            }

            String responseBody = response.body().string();
            log.info("City search API response: {}", responseBody);
            
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode locationArray = root.get("location");
            
            if (locationArray == null || locationArray.isNull() || !locationArray.isArray()) {
                log.warn("No cities found in response");
                return new ArrayList<>();
            }
            
            List<CityDTO> cities = new ArrayList<>();
            for (JsonNode locationNode : locationArray) {
                cities.add(CityDTO.builder()
                        .name(locationNode.has("name") ? locationNode.get("name").asText() : "")
                        .country(locationNode.has("country") ? locationNode.get("country").asText() : "")
                        .state(locationNode.has("adm1") ? locationNode.get("adm1").asText() : "")
                        .lat(locationNode.has("lat") ? locationNode.get("lat").asDouble() : 0.0)
                        .lon(locationNode.has("lon") ? locationNode.get("lon").asDouble() : 0.0)
                        .build());
            }
            
            log.info("Found {} cities", cities.size());
            return cities;
        }
    }

    public CurrentWeatherDTO getCurrentWeather(Double lat, Double lon, String city) throws IOException {
        String jwtToken = generateJwtToken();
        String location = getLocationId(city);
        
        String url;
        if ("USE_COORDINATES".equals(location)) {
            url = String.format("%s/v7/weather/now?location=%s,%s",
                    weatherProperties.getBaseUrl(), lat, lon);
        } else {
            url = String.format("%s/v7/weather/now?location=%s",
                    weatherProperties.getBaseUrl(), location);
        }

        log.info("Weather API call - city: {}, Location: {}, URL: {}", city, location, url);
        
        try (Response response = httpClient.newCall(
                new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "WeatherVisualizer/1.0")
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "Bearer " + jwtToken)
                        .get()
                        .build()
        ).execute()) {
            
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                log.error("Weather API error - Status: {}, Body: {}", response.code(), errorBody);
                throw new IOException("Unexpected response code: " + response.code() + ", body: " + errorBody);
            }

            String responseBody = response.body().string();
            log.info("Weather API response: {}", responseBody);
            
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode now = root.get("now");
            
            if (now == null || now.isNull()) {
                throw new IOException("Invalid response: missing 'now' object");
            }
            
            log.info("Raw weather data: {}", now.toString());
            
            Integer aqi = getAirQuality(lat, lon, location);
            
            CurrentWeatherDTO result = CurrentWeatherDTO.builder()
                    .city(city)
                    .lat(lat)
                    .lon(lon)
                    .temp(parseDouble(now.get("temp")))
                    .feelsLike(parseDouble(now.get("feelsLike")))
                    .description(now.get("text").asText())
                    .icon(now.has("icon") ? now.get("icon").asText() : "01d")
                    .windSpeed(parseDouble(now.get("windSpeed")))
                    .windDeg(now.has("wind360") ? parseInt(now.get("wind360")) : 0)
                    .windDir(now.has("windDir") ? now.get("windDir").asText() : "")
                    .windScale(now.has("windScale") ? now.get("windScale").asText() : "")
                    .humidity(parseInt(now.get("humidity")))
                    .pressure(parseDouble(now.get("pressure")))
                    .visibility(parseDouble(now.get("vis")))
                    .precip(now.has("precip") ? parseDouble(now.get("precip")) : 0.0)
                    .cloud(now.has("cloud") ? parseInt(now.get("cloud")) : null)
                    .dew(now.has("dew") ? parseDouble(now.get("dew")) : null)
                    .timestamp(System.currentTimeMillis() / 1000)
                    .aqi(aqi)
                    .build();
            
            log.info("Parsed weather data: temp={}C, description={}, windDir={}, windScale={}级, aqi={}", 
                    result.getTemp(), now.get("text").asText(), result.getWindDir(), result.getWindScale(), result.getAqi());
            return result;
        }
    }

    private Integer getAirQuality(Double lat, Double lon, String location) {
        try {
            String jwtToken = generateJwtToken();
            String url;
            if ("USE_COORDINATES".equals(location)) {
                url = String.format("%s/v7/air/now?location=%s,%s",
                        weatherProperties.getBaseUrl(), lat, lon);
            } else {
                url = String.format("%s/v7/air/now?location=%s",
                        weatherProperties.getBaseUrl(), location);
            }

            log.info("Air quality API call - URL: {}", url);
            
            try (Response response = httpClient.newCall(
                    new Request.Builder()
                            .url(url)
                            .addHeader("User-Agent", "WeatherVisualizer/1.0")
                            .addHeader("Accept", "application/json")
                            .addHeader("Authorization", "Bearer " + jwtToken)
                            .get()
                            .build()
            ).execute()) {
                
                if (!response.isSuccessful()) {
                    log.warn("Air quality API error - Status: {}", response.code());
                    return null;
                }

                String responseBody = response.body().string();
                log.info("Air quality API response: {}", responseBody);
                
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode airNow = root.get("airNow");
                
                if (airNow != null && !airNow.isNull() && airNow.isArray() && airNow.size() > 0) {
                    JsonNode airData = airNow.get(0);
                    if (airData.has("aqi")) {
                        return airData.get("aqi").asInt();
                    }
                }
                
                return null;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch air quality data: {}", e.getMessage());
            return null;
        }
    }

    public List<DailyForecastDTO> getDailyForecast(Double lat, Double lon, String city) throws IOException {
        String jwtToken = generateJwtToken();
        String location = getLocationId(city);
        
        String url;
        if ("USE_COORDINATES".equals(location)) {
            url = String.format("%s/v7/weather/7d?location=%s,%s",
                    weatherProperties.getBaseUrl(), lat, lon);
        } else {
            url = String.format("%s/v7/weather/7d?location=%s",
                    weatherProperties.getBaseUrl(), location);
        }

        log.info("Daily forecast API call - city: {}, Location: {}, URL: {}", city, location, url);
        
        try (Response response = httpClient.newCall(
                new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "WeatherVisualizer/1.0")
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "Bearer " + jwtToken)
                        .get()
                        .build()
        ).execute()) {
            
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                log.error("Daily forecast API error - Status: {}, Body: {}", response.code(), errorBody);
                throw new IOException("Unexpected response code: " + response.code() + ", body: " + errorBody);
            }

            String responseBody = response.body().string();
            log.info("Daily forecast API response: {}", responseBody);
            
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode dailyArray = root.get("daily");
            
            if (dailyArray == null || dailyArray.isNull() || !dailyArray.isArray()) {
                log.warn("No daily forecast data found in response");
                return new ArrayList<>();
            }
            
            List<DailyForecastDTO> forecasts = new ArrayList<>();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            for (JsonNode dayNode : dailyArray) {
                LocalDate date = LocalDate.parse(dayNode.get("fxDate").asText(), dateFormatter);
                
                forecasts.add(DailyForecastDTO.builder()
                        .city(city)
                        .lat(lat)
                        .lon(lon)
                        .date(date)
                        .tempMin(parseDouble(dayNode.get("tempMin")))
                        .tempMax(parseDouble(dayNode.get("tempMax")))
                        .iconDay(dayNode.has("iconDay") ? dayNode.get("iconDay").asText() : "01d")
                        .textDay(dayNode.has("textDay") ? dayNode.get("textDay").asText() : "")
                        .iconNight(dayNode.has("iconNight") ? dayNode.get("iconNight").asText() : "01n")
                        .textNight(dayNode.has("textNight") ? dayNode.get("textNight").asText() : "")
                        .wind360Day(dayNode.has("wind360Day") ? parseInt(dayNode.get("wind360Day")) : 0)
                        .windDirDay(dayNode.has("windDirDay") ? dayNode.get("windDirDay").asText() : "")
                        .windScaleDay(dayNode.has("windScaleDay") ? dayNode.get("windScaleDay").asText() : "")
                        .windSpeedDay(dayNode.has("windSpeedDay") ? parseDouble(dayNode.get("windSpeedDay")) : 0.0)
                        .wind360Night(dayNode.has("wind360Night") ? parseInt(dayNode.get("wind360Night")) : 0)
                        .windDirNight(dayNode.has("windDirNight") ? dayNode.get("windDirNight").asText() : "")
                        .windScaleNight(dayNode.has("windScaleNight") ? dayNode.get("windScaleNight").asText() : "")
                        .windSpeedNight(dayNode.has("windSpeedNight") ? parseDouble(dayNode.get("windSpeedNight")) : 0.0)
                        .humidity(parseInt(dayNode.get("humidity")))
                        .pressure(parseDouble(dayNode.get("pressure")))
                        .visibility(parseDouble(dayNode.get("vis")))
                        .precip(parseDouble(dayNode.get("precip")))
                        .cloud(dayNode.has("cloud") ? parseInt(dayNode.get("cloud")) : null)
                        .uvIndex(dayNode.has("uvIndex") ? parseInt(dayNode.get("uvIndex")) : null)
                        .sunrise(dayNode.has("sunrise") ? dayNode.get("sunrise").asText() : null)
                        .sunset(dayNode.has("sunset") ? dayNode.get("sunset").asText() : null)
                        .moonrise(dayNode.has("moonrise") ? dayNode.get("moonrise").asText() : null)
                        .moonset(dayNode.has("moonset") ? dayNode.get("moonset").asText() : null)
                        .moonPhase(dayNode.has("moonPhase") ? dayNode.get("moonPhase").asText() : null)
                        .moonPhaseIcon(dayNode.has("moonPhaseIcon") ? dayNode.get("moonPhaseIcon").asText() : null)
                        .pop(dayNode.has("pop") ? parseDouble(dayNode.get("pop")) : 0.0)
                        .build());
            }
            
            log.info("Parsed {} daily forecasts", forecasts.size());
            return forecasts;
        }
    }

    public List<HourlyForecastDTO> getHourlyForecast(Double lat, Double lon, String city) throws IOException {
        String jwtToken = generateJwtToken();
        String location = getLocationId(city);
        
        String url;
        if ("USE_COORDINATES".equals(location)) {
            url = String.format("%s/v7/weather/24h?location=%s,%s",
                    weatherProperties.getBaseUrl(), lat, lon);
        } else {
            url = String.format("%s/v7/weather/24h?location=%s",
                    weatherProperties.getBaseUrl(), location);
        }

        log.info("Hourly forecast API call - city: {}, Location: {}, URL: {}", city, location, url);
        
        try (Response response = httpClient.newCall(
                new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "WeatherVisualizer/1.0")
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "Bearer " + jwtToken)
                        .get()
                        .build()
        ).execute()) {
            
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                log.error("Hourly forecast API error - Status: {}, Body: {}", response.code(), errorBody);
                throw new IOException("Unexpected response code: " + response.code() + ", body: " + errorBody);
            }

            String responseBody = response.body().string();
            log.info("Hourly forecast API response: {}", responseBody);
            
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode hourlyArray = root.get("hourly");
            
            if (hourlyArray == null || hourlyArray.isNull() || !hourlyArray.isArray()) {
                log.warn("No hourly forecast data found in response");
                return new ArrayList<>();
            }
            
            List<HourlyForecastDTO> forecasts = new ArrayList<>();
            
            for (JsonNode hourNode : hourlyArray) {
                OffsetDateTime offsetTime = OffsetDateTime.parse(hourNode.get("fxTime").asText());
                LocalDateTime time = offsetTime.toLocalDateTime();
                
                forecasts.add(HourlyForecastDTO.builder()
                        .city(city)
                        .lat(lat)
                        .lon(lon)
                        .time(time)
                        .temp(parseDouble(hourNode.get("temp")))
                        .feelsLike(parseDouble(hourNode.get("feelsLike")))
                        .description(hourNode.get("text").asText())
                        .icon(hourNode.has("icon") ? hourNode.get("icon").asText() : "01d")
                        .windSpeed(parseDouble(hourNode.get("windSpeed")))
                        .windDeg(hourNode.has("wind360") ? parseInt(hourNode.get("wind360")) : 0)
                        .windDir(hourNode.has("windDir") ? hourNode.get("windDir").asText() : "")
                        .windScale(hourNode.has("windScale") ? hourNode.get("windScale").asText() : "")
                        .humidity(parseInt(hourNode.get("humidity")))
                        .pressure(parseDouble(hourNode.get("pressure")))
                        .visibility(parseDouble(hourNode.get("vis")))
                        .precip(hourNode.has("precip") ? parseDouble(hourNode.get("precip")) : 0.0)
                        .cloud(hourNode.has("cloud") ? parseInt(hourNode.get("cloud")) : null)
                        .dew(hourNode.has("dew") ? parseDouble(hourNode.get("dew")) : null)
                        .pop(hourNode.has("pop") ? parseDouble(hourNode.get("pop")) : 0.0)
                        .build());
            }
            
            log.info("Parsed {} hourly forecasts", forecasts.size());
            return forecasts;
        }
    }

    public List<AlertDTO> getAlerts(Double lat, Double lon, String city) throws IOException {
        String jwtToken = generateJwtToken();
        String location = getLocationId(city);
        
        String url;
        if ("USE_COORDINATES".equals(location)) {
            url = String.format("%s/v7/warning/now?location=%s,%s",
                    weatherProperties.getBaseUrl(), lat, lon);
        } else {
            url = String.format("%s/v7/warning/now?location=%s",
                    weatherProperties.getBaseUrl(), location);
        }

        log.info("Weather alerts API call - city: {}, Location: {}, URL: {}", city, location, url);
        
        try (Response response = httpClient.newCall(
                new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "WeatherVisualizer/1.0")
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "Bearer " + jwtToken)
                        .get()
                        .build()
        ).execute()) {
            
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                log.error("Weather alerts API error - Status: {}, Body: {}", response.code(), errorBody);
                throw new IOException("Unexpected response code: " + response.code() + ", body: " + errorBody);
            }

            String responseBody = response.body().string();
            log.info("Weather alerts API response: {}", responseBody);
            
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode warningArray = root.get("warning");
            
            if (warningArray == null || warningArray.isNull() || !warningArray.isArray()) {
                log.warn("No weather alerts found in response");
                return new ArrayList<>();
            }
            
            List<AlertDTO> alerts = new ArrayList<>();
            
            for (JsonNode warningNode : warningArray) {
                LocalDateTime startTime = null;
                LocalDateTime endTime = null;
                
                if (warningNode.has("startTime") && !warningNode.get("startTime").isNull()) {
                    try {
                        OffsetDateTime offsetStart = OffsetDateTime.parse(warningNode.get("startTime").asText());
                        startTime = offsetStart.toLocalDateTime();
                    } catch (Exception e) {
                        log.warn("Failed to parse startTime: {}", warningNode.get("startTime").asText());
                    }
                }
                
                if (warningNode.has("endTime") && !warningNode.get("endTime").isNull()) {
                    try {
                        OffsetDateTime offsetEnd = OffsetDateTime.parse(warningNode.get("endTime").asText());
                        endTime = offsetEnd.toLocalDateTime();
                    } catch (Exception e) {
                        log.warn("Failed to parse endTime: {}", warningNode.get("endTime").asText());
                    }
                }
                
                alerts.add(AlertDTO.builder()
                        .city(city)
                        .lat(lat)
                        .lon(lon)
                        .event(warningNode.has("event") ? warningNode.get("event").asText() : "")
                        .description(warningNode.has("text") ? warningNode.get("text").asText() : "")
                        .start(startTime)
                        .end(endTime)
                        .level(warningNode.has("level") ? warningNode.get("level").asText() : "")
                        .tags(warningNode.has("level") ? warningNode.get("level").asText() : "")
                        .build());
            }
            
            log.info("Parsed {} weather alerts", alerts.size());
            return alerts;
        }
    }

    private String getLocationId(String city) {
        // Map of Chinese cities to Location IDs
        if ("北京".equals(city) || "Beijing".equals(city)) return "101010100";
        if ("上海".equals(city) || "Shanghai".equals(city)) return "101020100";
        if ("广州".equals(city) || "Guangzhou".equals(city)) return "101280101";
        if ("深圳".equals(city) || "Shenzhen".equals(city)) return "101280601";
        if ("成都".equals(city) || "Chengdu".equals(city)) return "101270101";
        if ("杭州".equals(city) || "Hangzhou".equals(city)) return "101210101";
        if ("武汉".equals(city) || "Wuhan".equals(city)) return "101200101";
        if ("西安".equals(city) || "Xi'an".equals(city)) return "101110101";
        if ("南京".equals(city) || "Nanjing".equals(city)) return "101190101";
        if ("天津".equals(city) || "Tianjin".equals(city)) return "101030101";
        if ("重庆".equals(city) || "Chongqing".equals(city)) return "101040100";
        if ("苏州".equals(city) || "Suzhou".equals(city)) return "101190401";
        if ("抚州".equals(city) || "Fuzhou".equals(city)) return "101121001";
        
        // Return coordinates for unknown cities
        return "USE_COORDINATES";
    }
    
    // Generate JWT token for QWeather API authentication
    private String generateJwtToken() throws IOException {
        long currentTime = System.currentTimeMillis() / 1000;
        
        // Check if cached token is still valid (with 5 minute buffer)
        if (cachedJwtToken != null && currentTime < jwtTokenExpiryTime - 300) {
            log.debug("Using cached JWT token");
            return cachedJwtToken;
        }
        
        try {
            WeatherProperties.Jwt jwtConfig = weatherProperties.getJwt();
            
            // Header
            String headerJson = "{\"alg\": \"EdDSA\", \"kid\": \"" + jwtConfig.getCredentialId() + "\"}";
            
            // Payload
            long iat = currentTime - 30; // 30 seconds ago for clock skew
            long exp = iat + 900; // 15 minutes expiry
            String payloadJson = "{\"sub\": \"" + jwtConfig.getProjectId() + "\", \"iat\": " + iat + ", \"exp\": " + exp + "}";
            
            // Base64URL encode header and payload
            String headerEncoded = java.util.Base64.getUrlEncoder().encodeToString(headerJson.getBytes());
            String payloadEncoded = java.util.Base64.getUrlEncoder().encodeToString(payloadJson.getBytes());
            String data = headerEncoded + "." + payloadEncoded;
            
            // Load private key
            String privateKeyPem = jwtConfig.getPrivateKeyPem()
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            
            byte[] privateKeyBytes = java.util.Base64.getDecoder().decode(privateKeyPem);
            java.security.spec.PKCS8EncodedKeySpec keySpec = new java.security.spec.PKCS8EncodedKeySpec(privateKeyBytes);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("Ed25519");
            java.security.PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            
            // Sign the data
            java.security.Signature signer = java.security.Signature.getInstance("EdDSA");
            signer.initSign(privateKey);
            signer.update(data.getBytes());
            byte[] signature = signer.sign();
            
            String signatureEncoded = java.util.Base64.getUrlEncoder().encodeToString(signature);
            
            // Combine to form JWT
            String jwt = data + "." + signatureEncoded;
            
            // Cache the token
            cachedJwtToken = jwt;
            jwtTokenExpiryTime = exp;
            
            log.info("Generated new JWT token, expires at: {}", exp);
            return jwt;
            
        } catch (Exception e) {
            log.error("Failed to generate JWT token", e);
            throw new IOException("Failed to generate JWT token", e);
        }
    }
    
    private double parseDouble(JsonNode node) {
        if (node == null || node.isNull()) {
            return 0.0;
        }
        if (node.isTextual()) {
            try {
                return Double.parseDouble(node.asText());
            } catch (NumberFormatException e) {
                log.warn("Cannot parse double value: {}", node.asText());
                return 0.0;
            }
        }
        return node.asDouble();
    }
    
    private int parseInt(JsonNode node) {
        if (node == null || node.isNull()) {
            return 0;
        }
        if (node.isTextual()) {
            try {
                return Integer.parseInt(node.asText());
            } catch (NumberFormatException e) {
                log.warn("Cannot parse int value: {}", node.asText());
                return 0;
            }
        }
        return node.asInt();
    }
}