package com.backend.lxy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Boolean mockEnabled;

    public boolean isMockEnabled() {
        if (mockEnabled != null) {
            return mockEnabled;
        }
        return false;
    }
}
