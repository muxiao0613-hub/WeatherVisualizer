package com.backend.lxy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "weather")
public class WeatherProperties {
    private String provider = "OPENWEATHER";
    private String baseUrl;
    private String apiKey;

    public String getBaseUrl() {
        if (baseUrl != null && !baseUrl.isEmpty()) {
            return baseUrl;
        }
        if ("OPENWEATHER".equalsIgnoreCase(provider)) {
            return "https://api.openweathermap.org/data/3.0/onecall";
        }
        return baseUrl;
    }

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isEmpty();
    }
}
