package com.backend.lxy.service;

import com.backend.lxy.client.MockDataFactory;
import com.backend.lxy.client.QwenClient;
import com.backend.lxy.config.AppProperties;
import com.backend.lxy.config.QwenProperties;
import com.backend.lxy.domain.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final QwenClient qwenClient;
    private final MockDataFactory mockDataFactory;
    private final AppProperties appProperties;
    private final QwenProperties qwenProperties;

    public ChatResponseDTO chat(ChatRequestDTO request) {
        String city = request.getCity() != null ? request.getCity().getName() : "Unknown";
        String question = request.getQuestion();

        if (shouldUseMock()) {
            log.info("🤖 Using mock AI response for city: {}, question: {}", city, question);
            String answer = mockDataFactory.mockAiResponse(question, city);
            return ChatResponseDTO.builder()
                    .answer(answer)
                    .references(Arrays.asList("Current weather", "Forecast data"))
                    .build();
        }

        try {
            log.info("🔌 Calling Qwen API for city: {}, question: {}", city, question);
            String prompt = buildPrompt(question, city);
            String answer = qwenClient.chat(prompt);
            log.info("✅ Qwen API response received successfully");

            return ChatResponseDTO.builder()
                    .answer(answer)
                    .references(Arrays.asList("Current weather", "Forecast data", "Weather alerts"))
                    .build();
        } catch (IOException e) {
            log.warn("❌ Failed to call Qwen API, falling back to mock: {}", e.getMessage());
            String answer = mockDataFactory.mockAiResponse(question, city);
            return ChatResponseDTO.builder()
                    .answer(answer)
                    .references(Arrays.asList("Current weather", "Forecast data"))
                    .build();
        }
    }

    private String buildPrompt(String question, String city) {
        return String.format(
                "你是一个专业的天气助手。用户正在询问关于%s的天气问题。问题是：%s。请基于天气知识给出专业、实用的建议。",
                city, question
        );
    }

    private boolean shouldUseMock() {
        return appProperties.isMockEnabled() || !qwenProperties.hasApiKey();
    }
}
