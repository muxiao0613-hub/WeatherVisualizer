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
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String chat(String prompt) throws IOException {
        String requestBody = String.format("""
            {
                "model": "%s",
                "input": {
                    "messages": [
                        {
                            "role": "user",
                            "content": "%s"
                        }
                    ]
                }
            }
            """, qwenProperties.getModel(), prompt.replace("\"", "\\\"").replace("\n", "\\n"));

        Request request = new Request.Builder()
                .url(qwenProperties.getBaseUrl())
                .addHeader("Authorization", "Bearer " + qwenProperties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode root = objectMapper.readTree(responseBody);
            return root.get("output").get("choices").get(0).get("message").get("content").asText();
        }
    }
}
