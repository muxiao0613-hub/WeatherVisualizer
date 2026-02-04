package com.backend.lxy.client;

import com.backend.lxy.config.QwenProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class QwenClient {

    private final QwenProperties qwenProperties;
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String chat(String prompt) throws IOException {
        String requestBody = String.format("""
            {
                "model": "%s",
                "messages": [
                    {
                        "role": "user",
                        "content": "%s"
                    }
                ]
            }
            """, qwenProperties.getModel(), prompt.replace("\"", "\\\"").replace("\n", "\\n"));

        log.info("📤 Qwen API request URL: {}", qwenProperties.getBaseUrl() + "/chat/completions");
        log.info("📤 Qwen API request body: {}", requestBody);

        Request request = new Request.Builder()
                .url(qwenProperties.getBaseUrl() + "/chat/completions")
                .addHeader("Authorization", "Bearer " + qwenProperties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                log.error("❌ Qwen API error response - Status: {}, Body: {}", response.code(), errorBody);
                throw new IOException("Unexpected response code: " + response.code() + ", body: " + errorBody);
            }

            String responseBody = response.body().string();
            log.info("✅ Qwen API response received successfully: {}", responseBody);
            JsonNode root = objectMapper.readTree(responseBody);
            return root.get("choices").get(0).get("message").get("content").asText();
        } catch (java.net.SocketTimeoutException e) {
            log.error("⏱️ Qwen API timeout error: {}", e.getMessage());
            throw new IOException("Qwen API timeout: " + e.getMessage(), e);
        } catch (java.net.UnknownHostException e) {
            log.error("🌐 Qwen API DNS resolution error: {}", e.getMessage());
            throw new IOException("Qwen API DNS resolution failed: " + e.getMessage(), e);
        } catch (java.net.ConnectException e) {
            log.error("🔌 Qwen API connection error: {}", e.getMessage());
            throw new IOException("Qwen API connection failed: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("💥 Qwen API IO error: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            throw new IOException("Qwen API error: " + e.getMessage(), e);
        }
    }
}
