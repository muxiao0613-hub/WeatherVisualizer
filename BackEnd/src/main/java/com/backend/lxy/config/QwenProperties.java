package com.backend.lxy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.qwen")
public class QwenProperties {
    private String apiKey;
    private String model = "qwen-turbo";
    private String baseUrl = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isEmpty();
    }
}
