package com.backend.lxy.client;

import com.backend.lxy.config.QwenProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class QwenClient {

    private final QwenProperties qwenProperties;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String chat(String prompt) throws IOException {
        String url = qwenProperties.getBaseUrl();
        
        String requestBody = buildRequestBody(prompt);
        
        log.info("Qwen API request - URL: {}, Prompt length: {}", url, prompt.length());
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + qwenProperties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                log.error("Qwen API error - Status: {}, Body: {}", response.code(), errorBody);
                throw new IOException("Qwen API request failed with status: " + response.code());
            }

            String responseBody = response.body().string();
            log.info("Qwen API response received, length: {}", responseBody.length());
            
            return parseResponse(responseBody);
        }
    }

    private String buildRequestBody(String prompt) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            ObjectNode input = objectMapper.createObjectNode();
            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "user");
            message.put("content", prompt);
            
            input.set("messages", objectMapper.createArrayNode().add(message));
            requestBody.set("input", input);
            requestBody.put("model", qwenProperties.getModel());
            
            ObjectNode parameters = objectMapper.createObjectNode();
            parameters.put("result_format", "message");
            requestBody.set("parameters", parameters);

            return objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            log.error("Failed to build request body", e);
            throw new RuntimeException("Failed to build request body", e);
        }
    }

    private String parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode output = root.get("output");
            
            if (output != null && output.has("choices")) {
                JsonNode choices = output.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode choice = choices.get(0);
                    if (choice.has("message")) {
                        JsonNode message = choice.get("message");
                        if (message.has("content")) {
                            return message.get("content").asText();
                        }
                    }
                }
            }
            
            log.warn("Unexpected Qwen API response format: {}", responseBody);
            return "抱歉，我无法理解您的问题。";
        } catch (Exception e) {
            log.error("Failed to parse Qwen API response", e);
            return "抱歉，解析响应时出错。";
        }
    }
}
