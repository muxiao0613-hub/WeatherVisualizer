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
    private Jwt jwt;

    @Data
    public static class Jwt {
        private String projectId;
        private String credentialId;
        private String privateKeyPem;
    }

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

    public boolean hasJwtConfig() {
        return jwt != null && jwt.getProjectId() != null && jwt.getCredentialId() != null && jwt.getPrivateKeyPem() != null;
    }
}
