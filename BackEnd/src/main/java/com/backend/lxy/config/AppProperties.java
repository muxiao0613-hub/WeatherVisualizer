package com.backend.lxy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Boolean mockEnabled;
    private Boolean forceRealApi;
    private String heWeatherApiKey;
    private String heWeatherBaseUrl;

    public boolean isMockEnabled() {
        if (mockEnabled != null) {
            return mockEnabled;
        }
        return false;
    }
    
    public boolean isForceRealApi() {
        if (forceRealApi != null) {
            return forceRealApi;
        }
        return false;
    }
}
